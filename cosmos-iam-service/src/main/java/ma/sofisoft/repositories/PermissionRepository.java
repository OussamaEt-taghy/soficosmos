package ma.sofisoft.repositories;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.Permission;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PermissionRepository implements PanacheRepositoryBase<Permission, UUID> {

 // Récupère toutes les permissions avec leurs autorisations et groupes associés
    public List<Permission> findAllWithAuthorisations() {
        return find("SELECT DISTINCT p FROM Permission p " +
                "LEFT JOIN FETCH p.autorisations a " +
                "LEFT JOIN FETCH p.group").list();
    }

    // Récupère les noms des permissions accordées à un ensemble de rôles Keycloak
    public Set<String> findPermissionNamesByRoleIds(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return find("SELECT DISTINCT p.name FROM Permission p " +
                "JOIN p.autorisations a " +
                "WHERE a.idRole IN ?1", roleIds)
                .project(String.class)
                .list()
                .stream()
                .collect(Collectors.toSet());
    }
}

