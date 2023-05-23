package cygni.users.components;

import cygni.es.EventStore;
import cygni.users.aggregates.UserAggregate;
import cygni.users.dtos.UserViewDTO;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserQueryHandler implements UserQueryService {

  @Inject EventStore eventStore;

  @Override
  public Uni<UserViewDTO> handle(UUID userId) {
    return eventStore.load(userId, UserAggregate.class).map(UserAggregate::toDTO);
  }
}
