package cygni.experiences.resources;

import com.oracle.svm.core.annotate.Inject;
import cygni.KafkaTestResourceLifecycleManager;
import cygni.experiences.dtos.CancelExperienceDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import cygni.legacy.dtos.ChangeExperienceSeatsDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(ExperienceResource.class)

public class ExperienceResourceTest {

    @Test
    void createExperienceTest() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5)
                )
                .post()
                .then()
                .statusCode(201);
    }

    @Test
    void getExperienceTest() {
        var res = given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5)
                )
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response()
                .as(ExperienceCreatedDTO.class);
        given()
                .when()
                .contentType(ContentType.JSON)
                .get(res.aggregateID())
                .then()
                .statusCode(200);
    }

    @Test
    void changeExperienceSeats() {
        var res = given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5)
                )
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response()
                .as(ExperienceCreatedDTO.class);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new ChangeExperienceSeatsDTO(5)
                )
                .post(res.aggregateID())
                .then()
                .statusCode(202);
    }

    @Test
    void cancelExperience(){
        var res = given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5)
                )
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response()
                .as(ExperienceCreatedDTO.class);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(
                        new CancelExperienceDTO("The artist is not coming")
                )
                .patch(res.aggregateID())
                .then()
                .statusCode(202);
    }
}
