package ma.sofisoft.mappers;

import ma.sofisoft.dtos.autorisations.AutorisationResponse;
import ma.sofisoft.entities.Autorisation;
import ma.sofisoft.entities.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface AutorisationMapper {
//Convertit une entité Autorisation en DTO AutorisationResponse avec détails de la permission
    @Mapping(target = "roleId", source = "idRole")
    @Mapping(target = "permissionId", expression = "java(mapPermissionId(autorisation.getPermission()))")
    @Mapping(target = "permissionName", expression = "java(mapPermissionName(autorisation.getPermission()))")
    @Mapping(target = "permissionDescription", expression = "java(mapPermissionDescription(autorisation.getPermission()))")
    @Mapping(target = "groupName", expression = "java(mapGroupName(autorisation.getPermission()))")
    AutorisationResponse toAutorisationResponse(Autorisation autorisation);

    default String mapPermissionId(Permission permission) {
        return permission != null ? permission.getId().toString() : null;
    }
    default String mapPermissionName(Permission permission) {
        return permission != null ? permission.getName() : null;
    }
    default String mapPermissionDescription(Permission permission) {
        return permission != null ? permission.getDescription() : null;
    }
    default String mapGroupName(Permission permission) {
        if (permission != null && permission.getGroup() != null) {
            return permission.getGroup().getName();
        }
        return null;
    }
}
