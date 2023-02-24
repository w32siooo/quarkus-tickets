package cygni;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

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
                + "    \"eventId\" : \"ayo\",\n"
                + "    \"quantity\" : 5\n"
                + "}")
        .post()
        .then()
        .statusCode(201);
  }

  @Test
  public void testAggregate() {

    given()
            .when()
            .param("userId","0d68db1b-3848-45a2-9600-d32160779ff3")
            .param("eventId","ayo")
            .get()
            .then()
            .statusCode(200);
  }
}
