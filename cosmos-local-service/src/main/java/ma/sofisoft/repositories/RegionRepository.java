package ma.sofisoft.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.Region;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RegionRepository implements PanacheRepositoryBase<Region, UUID> {

    /**
     * Recherche une région par son code et pays
     */
    public Optional<Region> findByCodeAndCountryId(String code, UUID countryId) {
        return find("code = ?1 and country.id = ?2", code, countryId).firstResultOptional();
    }

    /**
     * Vérifie si une région existe avec le code donné dans un pays
     */
    public boolean existsByCodeAndCountryId(String code, UUID countryId) {
        return count("code = ?1 and country.id = ?2", code, countryId) > 0;
    }

    /**
     * Vérifie si un code existe pour une autre région dans le même pays
     */
    public boolean existsByCodeAndCountryIdAndIdNot(String code, UUID countryId, UUID id) {
        return count("code = ?1 and country.id = ?2 and id != ?3", code, countryId, id) > 0;
    }

    /**
     * Récupère toutes les régions d'un pays
     */
    public List<Region> findByCountryId(UUID countryId) {
        return list("country.id", countryId);
    }

    /**
     * Vérifie si une région peut être supprimée
     * Une région ne peut être supprimée si elle a des villes ou adresses
     */
    public boolean canBeDeleted(UUID regionId) {
        // Vérifier s'il y a des villes
        if (count("SELECT COUNT(c) FROM City c WHERE c.region.id = :regionId",
                Parameters.with("regionId", regionId)) > 0) {
            return false;
        }
        // Vérifier s'il y a des adresses
        long addressCount = getEntityManager()
                .createQuery("SELECT COUNT(a) FROM Address a WHERE a.region.id = :regionId", Long.class)
                .setParameter("regionId", regionId)
                .getSingleResult();
        return addressCount == 0;
    }

    /**
     * Vérifie si un pays existe avec le nom donné
     */
    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    /**
     * Vérifie si un nom existe pour un autre pays (excluant l'ID donné)
     * Vérifie les doublons hors lui-même
     */
    public boolean existsByNameAndIdNot(String name, UUID id) {
        return count("name = ?1 and id != ?2", name, id) > 0;
    }
}
