package ma.sofisoft.services.permissions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.permissions.*;
import ma.sofisoft.entities.Permission;
import ma.sofisoft.exceptions.permissions.InvalidPermissionException;
import ma.sofisoft.exceptions.permissions.PermissionAlreadyExistsException;
import ma.sofisoft.exceptions.permissions.PermissionInUseException;
import ma.sofisoft.exceptions.permissions.PermissionNotFoundException;
import ma.sofisoft.mappers.PermissionMapper;
import ma.sofisoft.repositories.PermissionGroupRepository;
import ma.sofisoft.repositories.PermissionRepository;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PermissionServicesImpl implements PermissionServices{
    @Inject
    PermissionRepository permissionRepository;

    @Inject
    PermissionGroupRepository permissionGroupRepository;

    @Inject
    PermissionMapper permissionMapper;

    @Override
    @Transactional
    public UUID createPermission(String token, CreatePermissionRequest request) {
        // Validation des données d'entrée
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidPermissionException("Le nom de la permission ne peut pas être vide");
        }
        // Vérifier l'unicité du nom
        Permission existing = permissionRepository.find("name", request.getName()).firstResult();
        if (existing != null) {
            throw new PermissionAlreadyExistsException(request.getName());
        }
        try {
            Permission permission = permissionMapper.toPermission(request);
            permissionRepository.persist(permission);
            log.info("Permission created successfully with ID: {}", permission.getId());
            return permission.getId();
        } catch (Exception e) {
            log.error("Error creating permission: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la création de la permission : " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void updatePermission(String token, UUID permissionId, UpdatePermissionRequest request) {
        // Validation des données d'entrée
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidPermissionException("Le nom de la permission ne peut pas être vide");
        }
        Permission permission = permissionRepository.findById(permissionId);
        if (permission == null) {
            throw new PermissionNotFoundException(permissionId.toString());
        }
        // Vérifier l'unicité du nom (si changé)
        if (!permission.getName().equals(request.getName())) {
            Permission existing = permissionRepository.find("name", request.getName()).firstResult();
            if (existing != null) {
                throw new PermissionAlreadyExistsException(request.getName());
            }
        }
        try {
            permissionMapper.updatePermission(permission, request);
            log.info("Permission updated successfully");
        } catch (Exception e) {
            log.error("Error updating permission: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la mise à jour de la permission : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deletePermission(String token, UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId);
        if (permission == null) {
            throw new PermissionNotFoundException(permissionId.toString());
        }
        // Vérifier qu'il n'y a pas d'autorisations liées
        if (!permission.getAutorisations().isEmpty()) {
            throw new PermissionInUseException(permissionId.toString());
        }
        try {
            permissionRepository.delete(permission);
            log.info("Permission deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting permission: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la suppression de la permission : " + e.getMessage());
        }
    }

    @Override
    public List<PermissionResponse> getAllPermissions(String token) {
        try {
            List<Permission> permissions = permissionRepository.findAllWithAuthorisations();
            return permissions.stream()
                    .map(permissionMapper::toPermissionResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving permissions: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération des permissions : " + e.getMessage());
        }
    }

    @Override
    public PermissionResponse getPermissionById(String token, UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId);
        if (permission == null) {
            throw new PermissionNotFoundException(permissionId.toString());
        }
        try {
            return permissionMapper.toPermissionResponse(permission);
        } catch (Exception e) {
            log.error("Error mapping permission to response: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération de la permission : " + e.getMessage());
        }
    }

}
