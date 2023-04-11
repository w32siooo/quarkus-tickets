package cygni.users.resources;

import cygni.es.dto.RequestAcceptedDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import cygni.users.commands.BuyTicketCommand;
import cygni.users.commands.CreateNewUserCommand;
import cygni.users.components.UserResource;
import cygni.users.dtos.BuyTicketDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class UserResourceTest {
  static KeycloakTestClient keycloakClient = new KeycloakTestClient();
  static ExperienceCreatedDTO experienceCreatedDTO;

  static UUID aliceUserId;

  private static String getAccessToken(String userName) {
    return keycloakClient.getAccessToken(userName);
  }

  @BeforeEach
  void setup() {
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

    CreateNewUserCommand cmd = new CreateNewUserCommand("Alice", 200L);
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
  }
}
