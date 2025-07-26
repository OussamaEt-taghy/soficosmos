package ma.sofisoft.services.PermissionGroup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.permissionGroup.*;
import ma.sofisoft.entities.Permission;
import ma.sofisoft.entities.PermissionGroup;
import ma.sofisoft.exceptions.permissionGroups.PermissionAlreadyAssignedException;
import ma.sofisoft.exceptions.permissionGroups.PermissionGroupAlreadyExistsException;
import ma.sofisoft.exceptions.permissionGroups.PermissionGroupInUseException;
import ma.sofisoft.exceptions.permissionGroups.PermissionGroupNotFoundException;
import ma.sofisoft.exceptions.permissions.InvalidPermissionException;
import ma.sofisoft.exceptions.permissions.PermissionNotFoundException;
import ma.sofisoft.repositories.PermissionRepository;
import ma.sofisoft.repositories.PermissionGroupRepository;
import ma.sofisoft.mappers.PermissionGroupMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PermissionGroupServicesImpl implements PermissionGroupServices {
    @Inject
    PermissionGroupRepository permissionGroupRepository;

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    PermissionGroupMapper permissionGroupMapper;

    @Override
    @Transactional
    public UUID createPermissionGroup(String token, CreatePermissionGroupRequest request) {
        // Validation des données d'entrée
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidPermissionException("Le nom du groupe de permissions ne peut pas être vide");
        }
        // Vérifier l'unicité du nom
        PermissionGroup existing = permissionGroupRepository.findByName(request.getName());
        if (existing != null) {
            throw new PermissionGroupAlreadyExistsException(request.getName());
        }
        try {
            PermissionGroup group = permissionGroupMapper.toPermissionGroup(request);
            permissionGroupRepository.persist(group);
            log.info("Permission group created successfully with ID: {}", group.getId());
            return group.getId();
        } catch (Exception e) {
            log.error("Error creating permission group: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la création du groupe de permissions : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updatePermissionGroup(String token, UUID groupId, UpdatePermissionGroupRequest request) {
        // Validation des données d'entrée
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidPermissionException("Le nom du groupe de permissions ne peut pas être vide");
        }
        PermissionGroup group = permissionGroupRepository.findById(groupId);
        if (group == null) {
            throw new PermissionGroupNotFoundException(groupId.toString());
        }
        // Vérifier l'unicité du nom (si changé)
        if (!group.getName().equals(request.getName())) {
            PermissionGroup existing = permissionGroupRepository.findByName(request.getName());
            if (existing != null) {
                throw new PermissionGroupAlreadyExistsException(request.getName());
            }
        }
        try {
            permissionGroupMapper.updatePermissionGroup(group, request);
            log.info("Permission group updated successfully");
        } catch (Exception e) {
            log.error("Error updating permission group: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la mise à jour du groupe de permissions : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deletePermissionGroup(String token, UUID groupId) {
        PermissionGroup group = permissionGroupRepository.findById(groupId);
        if (group == null) {
            throw new PermissionGroupNotFoundException(groupId.toString());
        }
        // Vérifier qu'il n'y a pas de permissions liées
        if (!group.getPermissions().isEmpty()) {
            throw new PermissionGroupInUseException(groupId.toString());
        }
        try {
            permissionGroupRepository.delete(group);
            log.info("Permission group deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting permission group: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la suppression du groupe de permissions : " + e.getMessage());
        }
    }

    @Override
    public List<PermissionGroupResponse> getAllPermissionGroups(String token) {
        try {
            List<PermissionGroup> groups = permissionGroupRepository.findAllWithPermissions();
            return groups.stream()
                    .map(permissionGroupMapper::toPermissionGroupResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving permission groups: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération des groupes de permissions : " + e.getMessage());
        }
    }

    @Override
    public PermissionGroupResponse getPermissionGroupById(String token, UUID groupId) {
        PermissionGroup group = permissionGroupRepository.findById(groupId);
        if (group == null) {
            throw new PermissionGroupNotFoundException(groupId.toString());
        }
        try {
            return permissionGroupMapper.toPermissionGroupResponse(group);
        } catch (Exception e) {
            log.error("Error mapping permission group to response: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération du groupe de permissions : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void assignPermissionToGroup(String token, AssignPermissionToGroupRequest request) {
        // Validation des données d'entrée
        if (request.getPermissionId() == null || request.getPermissionId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID de la permission ne peut pas être vide");
        }
        if (request.getGroupId() == null || request.getGroupId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID du groupe ne peut pas être vide");
        }
        // Vérifier que la permission existe
        Permission permission;
        try {
            permission = permissionRepository.findById(UUID.fromString(request.getPermissionId()));
        } catch (IllegalArgumentException e) {
            throw new InvalidPermissionException("Format d'ID de permission invalide : " + request.getPermissionId());
        }

        if (permission == null) {
            throw new PermissionNotFoundException(request.getPermissionId());
        }
        // Vérifier que le groupe existe
        PermissionGroup group;
        try {
            group = permissionGroupRepository.findById(UUID.fromString(request.getGroupId()));
        } catch (IllegalArgumentException e) {
            throw new InvalidPermissionException("Format d'ID de groupe invalide : " + request.getGroupId());
        }
        if (group == null) {
            throw new PermissionGroupNotFoundException(request.getGroupId());
        }
        // Vérifier si la permission est déjà dans un autre groupe
        if (permission.getGroup() != null) {
            throw new PermissionAlreadyAssignedException(permission.getName(), permission.getGroup().getName());
        }
        try {
            // Assigner la permission au groupe
            permission.setGroup(group);
            permissionRepository.persist(permission);
            log.info("Permission assigned to group successfully");
        } catch (Exception e) {
            log.error("Error assigning permission to group: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de l'assignation de la permission au groupe : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removePermissionFromGroup(String token, RemovePermissionFromGroupRequest request) {
        // Validation des données d'entrée
        if (request.getPermissionId() == null || request.getPermissionId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID de la permission ne peut pas être vide");
        }
        if (request.getGroupId() == null || request.getGroupId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID du groupe ne peut pas être vide");
        }
        // Vérifier que la permission existe
        Permission permission;
        try {
            permission = permissionRepository.findById(UUID.fromString(request.getPermissionId()));
        } catch (IllegalArgumentException e) {
            throw new InvalidPermissionException("Format d'ID de permission invalide : " + request.getPermissionId());
        }
        if (permission == null) {
            throw new PermissionNotFoundException(request.getPermissionId());
        }
        try {
            // Retirer la permission du groupe
            permission.setGroup(null);
            permissionRepository.persist(permission);
            log.info("Permission removed from group successfully");
        } catch (Exception e) {
            log.error("Error removing permission from group: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors du retrait de la permission du groupe : " + e.getMessage());
        }
    }

}
