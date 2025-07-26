package ma.sofisoft.mappers;

import ma.sofisoft.dtos.permissionGroup.CreatePermissionGroupRequest;
import ma.sofisoft.dtos.permissionGroup.PermissionGroupResponse;
import ma.sofisoft.dtos.permissionGroup.UpdatePermissionGroupRequest;
import ma.sofisoft.entities.Permission;
import ma.sofisoft.entities.PermissionGroup;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface PermissionGroupMapper {
//convertit CreatePermissionGroupRequest en nouvelle entité PermissionGroup
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    PermissionGroup toPermissionGroup(CreatePermissionGroupRequest dto);

    // Met à jour un PermissionGroup existant avec les données d'UpdatePermissionGroupRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermissionGroup(@MappingTarget PermissionGroup group, UpdatePermissionGroupRequest dto);

    // Convertit PermissionGroup en PermissionGroupResponse avec liste des noms de permissions
    @Mapping(target = "permissionNames", expression = "java(mapPermissionNames(group))")
    PermissionGroupResponse toPermissionGroupResponse(PermissionGroup group);

    default List<String> mapPermissionNames(PermissionGroup group) {
        if (group == null || group.getPermissions() == null) {
            return List.of();
        }
        return group.getPermissions().stream()
                .map(Permission::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }
}
