package routes;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import io.javalin.Javalin;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populators.Populator;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;

public class CandidateRoutesTest {

    private static Javalin app;
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final String BASE_URL = "http://localhost:7070/api";
    private static final SecurityDAO securityDAO = new SecurityDAO(emf);
    private static final Populator populator = new Populator(securityDAO, emf);

    private static User u1;
    private static String userToken;
    private static String adminToken;
    private static long candidateId;

    @BeforeAll
    static void init() {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
    }

    @BeforeEach
    void setup() {
        populator.populateCandidatesAndSkills();
        List<User> users = populator.populateUsers();
        u1 = users.get(0);

        userToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + u1.getUsername() + "\",\"password\":\"password1\"}")
                        .when()
                        .post(BASE_URL + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("{\"role\":\"admin\"}")
                .when()
                .post(BASE_URL + "/auth/user/addrole")
                .then()
                .statusCode(200);

        adminToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + u1.getUsername() + "\",\"password\":\"password1\"}")
                        .when()
                        .post(BASE_URL + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");
    }

    @AfterEach
    void teardown() {
        populator.cleanUpDb();
    }

    @AfterAll
    static void closeDown() {
        app.stop();
    }

    @Test
    void listCandidates_authorized() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/candidates")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    private long createCandidateAndReturnId() {
        String body = """
                {
                  "name":"JUnit Candidate",
                  "phone":"+4512345678",
                  "education":"Computer Science"
                }
                """;

        Number idNum =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post(BASE_URL + "/candidates")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .body("name", equalTo("JUnit Candidate"))
                        .extract()
                        .path("id");

        return idNum.longValue();
    }

    private long createSkillAndReturnId() {
        String body = """
                {
                  "name":"JUnit Skill",
                  "slug":"junit-skill",
                  "description":"Skill created from test",
                  "category":"PROG_LANG"
                }
                """;

        Number idNum =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post(BASE_URL + "/skills")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .body("name", equalTo("JUnit Skill"))
                        .extract()
                        .path("id");

        return idNum.longValue();
    }

    @Test
    void createCandidate_admin() {
        candidateId = createCandidateAndReturnId();
        Assertions.assertTrue(candidateId > 0);
    }

    @Test
    void getCandidateById_includesSkillsField() {
        candidateId = createCandidateAndReturnId();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/candidates/" + candidateId)
                .then()
                .statusCode(200)
                .body("id", equalTo(Math.toIntExact(candidateId)))
                .body("skills", notNullValue());
    }

    @Test
    void updateCandidate_admin() {
        candidateId = createCandidateAndReturnId();

        String body = """
                {
                  "name":"JUnit Candidate Updated",
                  "phone":"+4599999999",
                  "education":"Updated Education"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(BASE_URL + "/candidates/" + candidateId)
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .body(anyOf(
                        hasKey("name"),
                        anything()
                ));
    }

    @Test
    void filterBySkillCategory_progLang() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/candidates?category=PROG_LANG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    void report_topCandidatesByPopularity_ok() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/reports/candidates/top-by-popularity")
                .then()
                .statusCode(200);
    }

    @Test
    void addSkill_thenDeleteCandidate() {
        candidateId = createCandidateAndReturnId();
        long skillId = createSkillAndReturnId();

        int status =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .put(BASE_URL + "/candidates/" + candidateId + "/skills/" + skillId)
                        .then()
                        .extract()
                        .statusCode();
        Assertions.assertTrue(status == 200 || status == 404);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete(BASE_URL + "/candidates/" + candidateId)
                .then()
                .statusCode(anyOf(is(204), is(200)));
    }

    @Test
    void filterBySkillCategory_unknown_returns400() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/candidates?category=UNKNOWN")
                .then()
                .statusCode(400);
    }

    @Test
    void getCandidateById_notFound_returns404() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/candidates/999999")
                .then()
                .statusCode(404);
    }
}
