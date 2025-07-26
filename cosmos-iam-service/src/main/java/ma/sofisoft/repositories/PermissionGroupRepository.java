package ma.sofisoft.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.PermissionGroup;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PermissionGroupRepository implements PanacheRepositoryBase<PermissionGroup, UUID> {
 // Trouve un groupe de permissions par son nom
    public PermissionGroup findByName(String name) {
        return find("name", name).firstResult();
    }
 // Récupère tous les groupes de permissions avec leurs permissions associées
    public List<PermissionGroup> findAllWithPermissions() {
        return find("SELECT DISTINCT pg FROM PermissionGroup pg " +
                "LEFT JOIN FETCH pg.permissions").list();
    }
}
