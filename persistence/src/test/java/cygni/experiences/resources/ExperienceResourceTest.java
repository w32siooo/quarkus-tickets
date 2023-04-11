package cygni.experiences.resources;

import static io.restassured.RestAssured.given;

import cygni.es.EventStore;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.dtos.CancelExperienceDTO;
import cygni.experiences.dtos.ChangeExperienceSeatsDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;

@QuarkusTest
@TestHTTPEndpoint(ExperienceResource.class)
public class ExperienceResourceTest {
  static {
    RestAssured.useRelaxedHTTPSValidation();
  }

  KeycloakTestClient keycloakClient = new KeycloakTestClient();

  @Inject Mutiny.SessionFactory sf;

  @BeforeEach
  public void init() {
    sf.withTransaction((session, tx) -> session.createNativeQuery("delete from events where aggregate_type = 'Experience'")
                    .executeUpdate())
            .await().indefinitely();
  }

  @Test
  void createExperienceTest() {
    given().auth().oauth2(getAccessToken("alice"))
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
        given().auth().oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);
    given().auth().oauth2(getAccessToken("alice")).when().contentType(ContentType.JSON).get(res.aggregateID()).then().statusCode(200);
  }

  @Test
  void getAllExperiences() {
    var res =
        given().auth().oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);

    given().auth().oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(5))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(202);
    given().auth().oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(6))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(202);
    var res2 =
        given().auth().oauth2(getAccessToken("alice"))
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
        given().auth().oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);

    given().auth().oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(new ChangeExperienceSeatsDTO(-2))
        .post(res.aggregateID() + "/changeSeats")
        .then()
        .statusCode(400);

    given().auth().oauth2(getAccessToken("alice"))
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
        given().auth().oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("Rema", "roskilde", "2022-07-01", 5, 5))
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()
            .as(ExperienceCreatedDTO.class);
    given().auth().oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(new CancelExperienceDTO("The artist is not coming"))
        .post(res.aggregateID() + "/cancel")
        .then()
        .statusCode(202);
  }
  private String getAccessToken(String userName) {
    return keycloakClient.getAccessToken(userName);
  }
}
