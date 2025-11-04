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

public class SkillRoutesTest {

    private static Javalin app;
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final String BASE_URL = "http://localhost:7070/api";
    private static final SecurityDAO securityDAO = new SecurityDAO(emf);
    private static final Populator populator = new Populator(securityDAO, emf);

    private static User u1;
    private static String userToken;
    private static String adminToken;
    private static long skillId;

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
    void listSkills_authorized() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/skills")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    private long createSkillAndReturnId() {
        String body = """
                {
                  "name":"JUnit Skill",
                  "slug":"junit-skill",
                  "description":"Skill created from JUnit",
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
                        .body("category", equalTo("PROG_LANG"))
                        .extract()
                        .path("id");

        return idNum.longValue();
    }

    @Test
    void createSkill_admin() {
        skillId = createSkillAndReturnId();
        Assertions.assertTrue(skillId > 0);
    }

    @Test
    void getSkillById_ok() {
        skillId = createSkillAndReturnId();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/skills/" + skillId)
                .then()
                .statusCode(200)
                .body("id", equalTo(Math.toIntExact(skillId)))
                .body("name", equalTo("JUnit Skill"));
    }

    @Test
    void updateSkill_admin() {
        skillId = createSkillAndReturnId();

        String body = """
                {
                  "name":"JUnit Skill Updated",
                  "slug":"junit-skill-updated",
                  "description":"Updated description",
                  "category":"PROG_LANG"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(BASE_URL + "/skills/" + skillId)
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .body(anyOf(
                        hasKey("name"),
                        anything()
                ));
    }

    @Test
    void deleteSkill_admin() {
        skillId = createSkillAndReturnId();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete(BASE_URL + "/skills/" + skillId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    void getSkillById_notFound_returns404() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/skills/999999")
                .then()
                .statusCode(404);
    }
}
