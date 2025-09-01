package ma.sofisoft.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.Country;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CountryRepository implements PanacheRepositoryBase<Country, UUID> {

    /**
     * Recherche un pays par son code unique
     */
    public Optional<Country> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    /**
     * Vérifie si un pays existe avec le code donné
     */
    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    /**
     * Vérifie si un code existe pour un autre pays (excluant l'ID donné)
     * Utilisé pour validation lors de la modification
     */
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return count("code = ?1 and id != ?2", code, id) > 0;
    }

    /**
     * Vérifie si un pays peut être supprimé
     * Un pays ne peut être supprimé s'il a des régions, villes ou adresses
     */
    public boolean canBeDeleted(UUID countryId) {
        // Vérifier s'il y a des régions
        if (count("SELECT COUNT(r) FROM Region r WHERE r.country.id = :countryId",
                Parameters.with("countryId", countryId)) > 0) {
            return false;
        }
        // Vérifier s'il y a des villes
        if (count("SELECT COUNT(c) FROM City c WHERE c.country.id = :countryId",
                Parameters.with("countryId", countryId)) > 0) {
            return false;
        }
        // Vérifier s'il y a des adresses
        long addressCount = getEntityManager()
                .createQuery("SELECT COUNT(a) FROM Address a WHERE a.country.id = :countryId", Long.class)
                .setParameter("countryId", countryId)
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
