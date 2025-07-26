package ma.sofisoft.mappers;

import ma.sofisoft.dtos.users.CreateUserRequest;
import ma.sofisoft.dtos.users.UpdateUserRequest;
import ma.sofisoft.dtos.users.UserResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    // Mapper CreateUserRequest -> UserRepresentation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "attributes", ignore = true)
    // rôles seront injectés à part
    UserRepresentation toUserRepresentation(CreateUserRequest dto);

    // Mapper UpdateUserRequest -> UserRepresentation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    void updateUserRepresentation(@MappingTarget UserRepresentation user, UpdateUserRequest dto);

    // Mapper UserRepresentation + roles -> UserResponse
    @Mapping(target = "roles", source = "roles")
    UserResponse toUserResponse(UserRepresentation user, List<String> roles);

}