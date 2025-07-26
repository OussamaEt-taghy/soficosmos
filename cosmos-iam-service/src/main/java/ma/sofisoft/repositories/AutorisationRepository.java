package ma.sofisoft.repositories;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.Autorisation;
import java.util.UUID;

import java.util.List;

@ApplicationScoped
public class AutorisationRepository implements PanacheRepositoryBase<Autorisation, UUID> {

   // Trouve une autorisation spécifique basée sur un ID de permission et un ID de rôle Keycloak
    public Autorisation findByPermissionIdAndRoleId(String permissionId, String roleId) {
        return find("permission.id = ?1 and idRole = ?2", permissionId, roleId)
                .firstResult();
    }
   // Vérifie si une autorisation existe déjà pour une combinaison permission/rôle
    public boolean existsByPermissionIdAndRoleId(String permissionId, String roleId) {
        return find("permission.id = ?1 and idRole = ?2", permissionId, roleId)
                .firstResult() != null;
    }
   // Supprime une autorisation spécifique basée sur permission et rôle
    public long deleteByPermissionIdAndRoleId(String permissionId, String roleId) {
        return delete("permission.id = ?1 and idRole = ?2", permissionId, roleId);
    }
    // Trouve toutes les autorisations accordées à un rôle Keycloak spécifique
    public List<Autorisation> findByRoleId(String roleId) {
        return find("idRole", roleId).list();
    }
    // Trouve toutes les autorisations qui concernent une permission spécifique
    public List<Autorisation> findByPermissionId(String permissionId) {
        return find("permission.id", permissionId).list();
    }
}