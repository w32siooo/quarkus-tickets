package cygni;

import cygni.resources.TicketResource;
import cygni.commands.TicketActivateCommand;
import cygni.commands.TicketCreateCommand;
import cygni.commands.TicketOrderCommand;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TicketResource.class)
public class TicketResourceTest {

    @Test
    public void testCreate() {

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        "{\n"
                                + "    \"userId\" : \"0d68db1b-3848-45a2-9600-d32160779ff3\",\n"
                                + "    \"eventId\" : \"create\",\n"
                                + "    \"quantity\" : 5\n"
                                + "}")
                .post("create")
                .then()
                .statusCode(201);
    }

    @Test
    public void testActivate() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId("activate")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("create")
                .then()
                .statusCode(201);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketOrderCommand.builder()
                        .eventId("activate")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("order")
                .then()
                .statusCode(201);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketActivateCommand.builder()
                        .eventId("activate")
                        .quantity(999)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("activate")
                .then()
                .statusCode(400);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketActivateCommand.builder()
                        .eventId("activate")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("activate")
                .then()
                .statusCode(201);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketActivateCommand.builder()
                        .eventId("activate")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("activate")
                .then()
                .statusCode(400);
    }

    @Test
    public void testAggregate() {

        given()
                .when()
                .param("userId", "0d68db1b-3848-45a2-9600-d32160779ff3")
                .param("eventId", "ayo")
                .get()
                .then()
                .statusCode(200);
    }

    @Test
    public void testHibernateValidator() {

        given().when().param("eventId", "ayo").get().then().statusCode(400);
    }
}
