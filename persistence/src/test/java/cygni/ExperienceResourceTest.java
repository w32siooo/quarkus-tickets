package cygni;

import cygni.legacy.aggregates.UserTicketAggregate;
import cygni.legacy.resources.TicketResource;
import cygni.legacy.commands.TicketActivateCommand;
import cygni.legacy.commands.TicketCreateCommand;
import cygni.legacy.commands.TicketOrderCommand;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TicketResource.class)

public class ExperienceResourceTest {




    @Test
    public void testCreate() {

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        TicketCreateCommand.builder()
                                .eventId(UUID.randomUUID())
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
    UUID orderTest= UUID.randomUUID();
       given()
               .when()
               .contentType(ContentType.JSON)
               .body(TicketOrderCommand.builder()
                       .eventId(orderTest)
                       .quantity(5)
                       .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                       .build()
               ).post("order")
               .then()
               .statusCode(400);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId(orderTest)
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
                        .eventId(orderTest)
                        .quantity(6)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                ).post("order")
                .then()
                .statusCode(400);
       given()
               .when()
               .contentType(ContentType.JSON)
               .body(TicketOrderCommand.builder()
                       .eventId(orderTest)
                       .quantity(5)
                       .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                       .build()
               ).post("order")
               .then()
               .statusCode(201);

    }

    public void testQueryByEventId() {
    UUID queryTest= UUID.randomUUID();
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId(queryTest)
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
                        .eventId(queryTest)
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build())
                .post("create")
                .then()
                .statusCode(201);


        UserTicketAggregate res = given()
                .when()
                .contentType(ContentType.JSON)
                .param("eventId", queryTest)
                .get("event")
                .then()
                .statusCode(200)
                .extract().body().as(UserTicketAggregate.class);
        assert res.getVersion().get() == 5;
    }
@Test
    public void testActivate() {
    UUID activateId = UUID.randomUUID();
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketCreateCommand.builder()
                        .eventId(activateId)
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
                        .eventId(activateId)
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
                        .eventId(activateId)
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
                        .eventId(activateId)
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
                        .eventId(activateId)
                        .quantity(5)
                        .userId(UUID.fromString("0d68db1b-3848-45a2-9600-d32160779ff3"))
                        .build()
                )
                .post("activate")
                .then()
                .statusCode(400);
    }

    public void testHibernateValidator() {

        given().when().param("eventId", "ayo").get("user").then().statusCode(400);
    }
}
