package cygni.producer.resources;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(TicketResource.class)
public class QuotesResourceTest {

    @Test
    public void testCreateRequest(){
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("")
                .post("/request")
                .then()
                .statusCode(200)
                .body(is("emitted"));

    }
}
