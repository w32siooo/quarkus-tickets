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
import javax.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
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
            .body(new CreateExperienceRequestDTO("joeboy", "roskilde", "2021-06-01", 5, 5))
            .post("api/v1/tickets/")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(ExperienceCreatedDTO.class);

    CreateNewUserCommand cmd = new CreateNewUserCommand("Alice", 5L);
    aliceUserId =
        given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .contentType(ContentType.JSON)
            .body(cmd)
            .post("/api/v1/users/create")
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(RequestAcceptedDTO.class)
            .aggregateId();
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
        .post(String.format("/api/v1/users/%s/buyExperience", aliceUserId))
        .then()
        .statusCode(201);
    UserViewDTO aliceObject =
        given()
            .auth()
            .oauth2(getAccessToken("alice"))
            .when()
            .get(String.format("/api/v1/users/%s", aliceUserId))
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
            .post(String.format("/api/v1/users/%s/buyExperience", aliceUserId))
            .then()
            .statusCode(400)
            .extract()
            .as(RequestFailedDTO.class);

    assert failed.reason().equals("Not enough balance to book the wanted tickets");
    assert aliceObject.ownedExperiences().size() == 1;
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
        .post(String.format("/api/v1/users/%s/buyExperience", aliceUserId))
        .then()
        .statusCode(400);
  }
}