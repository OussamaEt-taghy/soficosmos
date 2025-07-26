package ma.sofisoft.mappers;
import ma.sofisoft.dtos.permissions.CreatePermissionRequest;
import ma.sofisoft.dtos.permissions.PermissionResponse;
import ma.sofisoft.dtos.permissions.UpdatePermissionRequest;
import ma.sofisoft.entities.Autorisation;
import ma.sofisoft.entities.Permission;
import ma.sofisoft.entities.PermissionGroup;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface PermissionMapper {

    // Convertit CreatePermissionRequest en nouvelle entité Permission
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "autorisations", ignore = true)
    Permission toPermission(CreatePermissionRequest dto);

    // Met à jour une Permission existante avec les données d'UpdatePermissionRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "autorisations", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermission(@MappingTarget Permission permission, UpdatePermissionRequest dto);

// Convertit Permission en PermissionResponse avec IDs des rôles et infos du groupe
    @Mapping(target = "roleIds", expression = "java(mapRoleIdsFromAutorisations(permission))")
    @Mapping(target = "groupId", expression = "java(mapGroupId(permission.getGroup()))")
    @Mapping(target = "groupName", expression = "java(mapGroupName(permission.getGroup()))")
    PermissionResponse toPermissionResponse(Permission permission);



    // Méthode personnalisée pour extraire les noms de rôles depuis les autorisations
    default Set<String> mapRoleIdsFromAutorisations(Permission permission) {
        if (permission == null || permission.getAutorisations() == null) {
            return Set.of();
        }

        return permission.getAutorisations().stream()
                .map(Autorisation::getIdRole)
                .filter(roleId -> roleId != null && !roleId.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    default String mapGroupId(PermissionGroup group) {
        return group != null ? group.getId().toString() : null;
    }

    default String mapGroupName(PermissionGroup group) {
        return group != null ? group.getName() : null;
    }
}

