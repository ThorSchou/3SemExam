package routes;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.security.daos.SecurityDAO;
import io.javalin.Javalin;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populators.Populator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class SecurityRoutesTest {

    private static Javalin app;
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final String BASE_URL = "http://localhost:7070/api";
    private static final SecurityDAO securityDAO = new SecurityDAO(emf);
    private static final Populator populator = new Populator(securityDAO, emf);

    private String registerToken;
    private String userToken;
    private String adminToken;
    private String username;

    private static String uniqueUsername() {
        return "u_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @BeforeAll
    static void init() {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
    }

    @BeforeEach
    void setup() {
        username = uniqueUsername();
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
    void healthcheck() {
        given()
                .when()
                .get(BASE_URL + "/auth/healthcheck")
                .then()
                .statusCode(200);
    }

    @Test
    void register_login_user_and_admin_flow() {
        registerToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + username + "\",\"password\":\"password1\"}")
                        .when()
                        .post(BASE_URL + "/auth/register")
                        .then()
                        .statusCode(201)
                        .body("username", equalTo(username))
                        .extract()
                        .path("token");

        userToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + username + "\",\"password\":\"password1\"}")
                        .when()
                        .post(BASE_URL + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");

        given()
                .when()
                .get(BASE_URL + "/protected/user_demo")
                .then()
                .statusCode(anyOf(is(401), is(403)));

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get(BASE_URL + "/protected/user_demo")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get(BASE_URL + "/protected/admin_demo")
                .then()
                .statusCode(anyOf(is(401), is(403)));

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body("{\"role\":\"admin\"}")
                .when()
                .post(BASE_URL + "/auth/user/addrole")
                .then()
                .statusCode(200);

        adminToken =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"" + username + "\",\"password\":\"password1\"}")
                        .when()
                        .post(BASE_URL + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(BASE_URL + "/protected/admin_demo")
                .then()
                .statusCode(200);
    }
}
