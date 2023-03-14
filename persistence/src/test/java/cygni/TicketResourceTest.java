package cygni;

import cygni.aggregates.TicketAggregate;
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
                        TicketCreateCommand.builder()
                                .eventId("test")
                                .quantity(5)
                                .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                                .build()
                )
                .post("create")
                .then()
                .statusCode(201);
    }

    @Test
    public void testOrder() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId("orderTest")
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
                        .eventId("orderTest")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                ).post("order")
                .then()
                .statusCode(201);

    }

    @Test
    public void testQueryByEventId() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId("ayo")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build())
                .post("create")
                .then()
                .statusCode(201);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId("ayo")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build())
                .post("create")
                .then()
                .statusCode(201);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId("ayo")
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build())
                .post("create")
                .then()
                .statusCode(201);

        TicketAggregate res = given()
                .when()
                .contentType(ContentType.JSON)
                .param("eventId", "ayo")
                .get("event")
                .then()
                .statusCode(200)
                .extract().body().as(TicketAggregate.class);
        assert res.getUnBookedTickets().get() == 10;




    }

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

        given().when().param("eventId", "ayo").get("user").then().statusCode(400);
    }
}
