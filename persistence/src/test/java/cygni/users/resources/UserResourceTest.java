package cygni.users.resources;

import static io.restassured.RestAssured.given;

import cygni.es.dto.RequestAcceptedDTO;
import cygni.es.dto.RequestFailedDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import cygni.users.commands.BuyTicketCommand;
import cygni.users.commands.CreateNewUserCommand;
import cygni.users.dtos.UserViewDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.http.ContentType;
import java.util.UUID;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UserResourceTest {
  static KeycloakTestClient keycloakClient = new KeycloakTestClient();
  static ExperienceCreatedDTO experienceCreatedDTO; // shared

  static UUID aliceUserId; // shared

  @Inject Mutiny.SessionFactory sf;

  private static String getAccessToken(String userName) {
    return keycloakClient.getAccessToken(userName);
  }

  @BeforeEach
  void setup() {
    sf.withTransaction(
            (session, tx) ->
                session
                    .createNativeQuery("delete from events where aggregate_type = 'User'")
                    .executeUpdate())
        .await()
        .indefinitely();
    sf.withTransaction(
            (session, tx) ->
                session
                    .createNativeQuery("delete from events where aggregate_type = 'Experience'")
                    .executeUpdate())
        .await()
        .indefinitely();
    experienceCreatedDTO =
        given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 999, 5))
            .post("api/v1/tickets/")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(ExperienceCreatedDTO.class);
    given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .post("/api/v1/users/create")
            .then()
            .statusCode(201);

  }


  @Test
  void testBuyExperience() {
    BuyTicketCommand buyCmd =
        new BuyTicketCommand(UUID.fromString(experienceCreatedDTO.aggregateID()), 1);
    given()
        .auth()
        .oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(buyCmd)
        .post("/api/v1/users/buyExperience")
        .then()
        .statusCode(201);
    UserViewDTO aliceObject =
        given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .get("/api/v1/users/")
            .then()
            .statusCode(200)
            .extract()
            .as(UserViewDTO.class);
    var failed =
        given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(buyCmd)
            .post("/api/v1/users/buyExperience" )
            .then()
            .statusCode(400)
            .extract()
            .as(RequestFailedDTO.class);

    assert failed.reason().contains("Not enough balance to book the wanted tickets");
    assert aliceObject.ownedExperiences().size() == 1;
    //hello,
  }

  @Test
  void testBuyExperienceNotEnoughAvailableSeats() {
    BuyTicketCommand buyCmd =
        new BuyTicketCommand(UUID.fromString(experienceCreatedDTO.aggregateID()), 6);
    given()
        .auth()
        .oauth2(getAccessToken("alice"))
        .when()
        .contentType(ContentType.JSON)
        .body(buyCmd)
        .post("/api/v1/users/buyExperience")
        .then()
        .statusCode(400);
  }
}
