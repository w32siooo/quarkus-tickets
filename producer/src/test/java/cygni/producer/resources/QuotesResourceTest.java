package cygni.producer.resources;

import cygni.producer.commands.TicketActivateCommand;
import cygni.producer.commands.TicketOrderCommand;
import cygni.producer.model.TicketActivatedDto;
import cygni.producer.model.TicketCreatedDto;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TicketResource.class)
public class QuotesResourceTest {

    @Test
    public void testCreateRequest(){

        assert given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketOrderCommand.builder().eventId("123").build())
                .post("/create")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TicketCreatedDto.class)
                .equals(TicketCreatedDto.builder().eventId("123").build());
    }

    @Test
    public void testActivateRequest(){
        UUID eventId = UUID.randomUUID();

        assert given()
                .when()
                .contentType(ContentType.JSON)
                .body(TicketActivateCommand.builder().eventId(eventId.toString()).build())
                .post("/activate")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TicketActivatedDto.class)
                .equals(TicketActivatedDto.builder().eventId(eventId.toString()).build());
    }
}
