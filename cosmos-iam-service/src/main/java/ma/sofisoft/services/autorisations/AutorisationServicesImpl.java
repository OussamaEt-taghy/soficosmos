package ma.sofisoft.services.autorisations;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.autorisations.*;
import ma.sofisoft.entities.Permission;
import ma.sofisoft.entities.Autorisation;
import ma.sofisoft.exceptions.autorisations.AutorisationAlreadyExistsException;
import ma.sofisoft.exceptions.autorisations.AutorisationNotFoundException;
import ma.sofisoft.exceptions.autorisations.PermissionAssignmentException;
import ma.sofisoft.exceptions.permissions.InvalidPermissionException;
import ma.sofisoft.exceptions.permissions.PermissionNotFoundException;
import ma.sofisoft.repositories.PermissionRepository;
import ma.sofisoft.repositories.AutorisationRepository;
import ma.sofisoft.mappers.AutorisationMapper;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class AutorisationServicesImpl implements AutorisationServices {
    @Inject
    PermissionRepository permissionRepository;

    @Inject
    AutorisationRepository autorisationRepository;

    @Inject
    AutorisationMapper autorisationMapper;

    @Override
    @Transactional
    public void assignPermissionToRole(String token, AssignPermissionToRoleRequest request) {
        log.info("=== START assignPermissionToRole SERVICE ===");
        // Validation des données d'entrée
        if (request.getPermissionId() == null || request.getPermissionId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID de la permission ne peut pas être vide");
        }
        if (request.getRoleId() == null || request.getRoleId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID du rôle ne peut pas être vide");
        }
        // Vérifier que la permission existe
        Permission permission;
        try {
            permission = permissionRepository.findById(UUID.fromString(request.getPermissionId()));
        } catch (IllegalArgumentException e) {
            throw new InvalidPermissionException("Format d'ID de permission invalide : " + request.getPermissionId());
        }
        if (permission == null) {
            log.warn("Permission not found: {}", request.getPermissionId());
            throw new PermissionNotFoundException(request.getPermissionId());
        }
        log.info("Permission found: {}", permission.getName());

        // Vérifier si l'autorisation existe déjà
        boolean exists = autorisationRepository.existsByPermissionIdAndRoleId(
                request.getPermissionId(), request.getRoleId());
        if (exists) {
            log.info("Authorization already exists");
            throw new AutorisationAlreadyExistsException(permission.getName(), request.getRoleId());
        }
        try {
            log.info("Creating new authorization...");
            Autorisation autorisation = Autorisation.builder()
                    .permission(permission)
                    .idRole(request.getRoleId())
                    .build();
            autorisationRepository.persist(autorisation);
            log.info("Authorization created successfully");
        } catch (Exception e) {
            log.error("Error creating authorization: {}", e.getMessage(), e);
            throw new PermissionAssignmentException(permission.getName(), request.getRoleId());
        }
        log.info("=== END assignPermissionToRole SERVICE ===");
    }

    @Override
    @Transactional
    public void removePermissionFromRole(String token, RemovePermissionFromRoleRequest request) {
        // Validation des données d'entrée
        if (request.getPermissionId() == null || request.getPermissionId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID de la permission ne peut pas être vide");
        }
        if (request.getRoleId() == null || request.getRoleId().trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID du rôle ne peut pas être vide");
        }
        // Vérifier que l'autorisation existe
        Autorisation autorisation = autorisationRepository.findByPermissionIdAndRoleId(
                request.getPermissionId(), request.getRoleId());
        if (autorisation == null) {
            log.warn("Permission ↔ role association does not exist");
            throw new AutorisationNotFoundException(request.getPermissionId(), request.getRoleId());
        }
        try {
            log.info("Association found, deleting...");
            autorisationRepository.delete(autorisation);
            log.info("Association successfully deleted");
        } catch (Exception e) {
            log.error("Error deleting authorization: {}", e.getMessage(), e);
            throw new PermissionAssignmentException("Permission ID: " + request.getPermissionId(),
                    "Role ID: " + request.getRoleId());
        }
    }

    @Override
    public List<AutorisationResponse> getAutorisationsByRole(String token, String roleId) {
        // Validation des données d'entrée
        if (roleId == null || roleId.trim().isEmpty()) {
            throw new InvalidPermissionException("L'ID du rôle ne peut pas être vide");
        }
        try {
            List<Autorisation> autorisations = autorisationRepository.findByRoleId(roleId);
            return autorisations.stream()
                    .map(autorisationMapper::toAutorisationResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving authorizations by role: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération des autorisations pour le rôle : " + roleId);
        }
    }


    @Override
    public List<AutorisationResponse> getAutorisationsByPermission(String token, UUID permissionId) {
        // Validation des données d'entrée
        if (permissionId == null) {
            throw new InvalidPermissionException("L'ID de la permission ne peut pas être null");
        }
        // Vérifier que la permission existe
        Permission permission = permissionRepository.findById(permissionId);
        if (permission == null) {
            throw new PermissionNotFoundException(permissionId.toString());
        }
        try {
            List<Autorisation> autorisations = autorisationRepository.findByPermissionId(permissionId.toString());
            return autorisations.stream()
                    .map(autorisationMapper::toAutorisationResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving authorizations by permission: {}", e.getMessage(), e);
            throw new InvalidPermissionException("Erreur lors de la récupération des autorisations pour la permission : " + permissionId);
        }
    }

}
