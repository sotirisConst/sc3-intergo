/*package org.acme;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class MessageResourceTest {

    @Test
    public void testGetAllMessagesWithAuth() {
        RestAssured
            .given()
                .auth().basic("adminsc", "adminpass") // adjust if needed
                .when()
                .get("/messages/all")
            .then()
                .statusCode(200)
                .body("$", notNullValue()); // checks response is not null 
                
    }

    @Test
    public void testGetAllMessagesWithInvalidAuth() {
        RestAssured
            .given()
                .auth().basic("wronguser", "wrongpass")
            .when()
                .get("/messages/all")
            .then()
                .statusCode(401);
    }
}*/