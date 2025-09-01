package ma.sofisoft.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.entities.City;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CityRepository implements PanacheRepositoryBase<City, UUID> {

    /**
     * Recherche une ville par son code, pays et région
     */
    public Optional<City> findByCodeAndCountryIdAndRegionId(String code, UUID countryId, UUID regionId) {
        return find("code = ?1 and country.id = ?2 and region.id = ?3", code, countryId, regionId).firstResultOptional();
    }

    /**
     * Vérifie si une ville existe avec le code donné dans un pays et région
     */
    public boolean existsByCodeAndCountryIdAndRegionId(String code, UUID countryId, UUID regionId) {
        return count("code = ?1 and country.id = ?2 and region.id = ?3", code, countryId, regionId) > 0;
    }

    /**
     * Vérifie si un code existe pour une autre ville dans le même pays/région
     */
    public boolean existsByCodeAndCountryIdAndRegionIdAndIdNot(String code, UUID countryId, UUID regionId, UUID id) {
        return count("code = ?1 and country.id = ?2 and region.id = ?3 and id != ?4",
                code, countryId, regionId, id) > 0;
    }

    /**
     * Récupère toutes les villes d'un pays
     */
    public List<City> findByCountryId(UUID countryId) {
        return list("country.id", countryId);
    }

    /**
     * Récupère toutes les villes d'une région
     */
    public List<City> findByRegionId(UUID regionId) {
        return list("region.id", regionId);
    }

    /**
     * Vérifie si une ville peut être supprimée
     * Une ville ne peut être supprimée si elle a des adresses
     */
    public boolean canBeDeleted(UUID cityId) {
        long addressCount = getEntityManager()
                .createQuery("SELECT COUNT(a) FROM Address a WHERE a.city.id = :cityId", Long.class)
                .setParameter("cityId", cityId)
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
