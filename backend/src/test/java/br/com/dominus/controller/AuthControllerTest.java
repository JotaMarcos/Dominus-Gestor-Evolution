package br.com.dominus.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthControllerTest {
    private static final String ADMIN_LOGIN_PAYLOAD = "{\"login\":\"Admin\",\"senha\":\"Toor#@!1439$10\"}";

    @Test
    void deveRejeitarLoginSemCredenciais() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/api/auth/login")
                .then().statusCode(400);
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"login\":\"Admin\",\"senha\":\"senha-errada\"}")
                .when().post("/api/auth/login")
                .then().statusCode(401);
    }

    @Test
    void deveAutenticarEDevolverCookieDeSessao() {
        given()
                .contentType(ContentType.JSON)
                .body(ADMIN_LOGIN_PAYLOAD)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("mfaRequired", equalTo(false))
                .cookie("dominus_session", notNullValue());
    }

    @Test
    void deveBloquearAcessoAClientesSemSessao() {
        given()
                .when().get("/api/clientes")
                .then().statusCode(401);
    }

    @Test
    void devePermitirAcessoAClientesComSessaoValida() {
        String sessionCookie = given()
                .contentType(ContentType.JSON)
                .body(ADMIN_LOGIN_PAYLOAD)
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().cookie("dominus_session");

        given()
                .cookie("dominus_session", sessionCookie)
                .when().get("/api/clientes")
                .then().statusCode(200);
    }

    @Test
    void logoutDeveLimparCookieDeSessao() {
        given()
                .when().post("/api/auth/logout")
                .then()
                .statusCode(200)
                .cookie("dominus_session", equalTo(""));
    }
}
