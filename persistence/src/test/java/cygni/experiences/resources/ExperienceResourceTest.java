package cygni.experiences.resources;

import static io.restassured.RestAssured.given;

import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.dtos.CancelExperienceDTO;
import cygni.experiences.dtos.ChangeExperienceSeatsDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ExperienceResource.class)
public class ExperienceResourceTest {

  @Test
  void createExperienceTest() {
    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
        .post()
        .then()
        .statusCode(201);
  }

  @Test
  void getExperienceTest() {
    var res =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);
    given().when().contentType(ContentType.JSON).get(res.aggregateID()).then().statusCode(200);
  }

  @Test
  void getAllExperiences() {
    var res =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);

    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(5))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(202);
    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(6))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(202);
    var res2 =
        given()
            .when()
            .contentType(ContentType.JSON)
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(ExperienceAggregate[].class);
    assert res2.length == 1;
  }

  @Test
  void changeExperienceSeats() {
    var res =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);

    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(-2))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(400);

    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(5))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(202);
  }

  @Test
  void cancelExperience() {
    var res =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);
    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new CancelExperienceDTO("The artist is not coming"))
        .post(res.aggregateID() + "/cancel")
        .then()
        .statusCode(202);
  }
}
