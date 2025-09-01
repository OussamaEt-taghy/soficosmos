package ma.sofisoft.repositories;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ma.sofisoft.entities.Address;
import ma.sofisoft.enums.OwnerType;
import ma.sofisoft.services.remoteServiceChecker.AddressUsageChecker;

import java.util.*;

@ApplicationScoped
public class AddressRepository implements PanacheRepositoryBase<Address, UUID> {
    @Inject
    AddressUsageChecker usageChecker;

    /**
     * Récupère toutes les adresses d'un propriétaire
     * @param ownerType le type de propriétaire (TIER ou COMPANY)
     * @param ownerId l'ID du propriétaire
     * @return liste des adresses du propriétaire
     */
    public List<Address> findByOwnerTypeAndOwnerId(OwnerType ownerType, UUID ownerId) {
        return list("ownerType = ?1 and ownerId = ?2", ownerType, ownerId);
    }

    /**
     * Récupère toutes les adresses actives d'un propriétaire
     * @param ownerType le type de propriétaire
     * @param ownerId l'ID du propriétaire
     * @return liste des adresses actives
     */
    public List<Address> findActiveByOwnerTypeAndOwnerId(OwnerType ownerType, UUID ownerId) {
        return list("ownerType = ?1 and ownerId = ?2 and isActive = true", ownerType, ownerId);
    }

    /**
     * Récupère l'adresse par défaut d'un propriétaire
     * @param ownerType le type de propriétaire
     * @param ownerId l'ID du propriétaire
     * @return Optional contenant l'adresse par défaut ou vide
     */
    public Optional<Address> findDefaultByOwnerTypeAndOwnerId(OwnerType ownerType, UUID ownerId) {
        return find("ownerType = ?1 and ownerId = ?2 and defaultAddress = true and isActive = true",
                ownerType, ownerId).firstResultOptional();
    }

    /**
     * Récupère les adresses par type (shipping, billing, other)
     * @param ownerType le type de propriétaire
     * @param ownerId l'ID du propriétaire
     * @param isShipping true pour les adresses de livraison
     * @param isBilling true pour les adresses de facturation
     * @param isOther true pour les autres adresses
     * @return liste des adresses correspondant aux critères
     */
    public List<Address> findByOwnerAndAddressTypes(OwnerType ownerType, UUID ownerId,
                                                    Boolean isShipping, Boolean isBilling, Boolean isOther) {
        StringBuilder query = new StringBuilder("ownerType = ?1 and ownerId = ?2 and isActive = true");

        if (isShipping != null) {
            query.append(" and shippingAddress = ").append(isShipping);
        }
        if (isBilling != null) {
            query.append(" and billingAddress = ").append(isBilling);
        }
        if (isOther != null) {
            query.append(" and otherAddresses = ").append(isOther);
        }

        return list(query.toString(), ownerType, ownerId);
    }

    /**
     * Récupère les adresses par localisation
     * @param countryId l'ID du pays (optionnel)
     * @param regionId l'ID de la région (optionnel)
     * @param cityId l'ID de la ville (optionnel)
     * @return liste des adresses dans cette localisation
     */
    public List<Address> findByLocation(UUID countryId, UUID regionId, UUID cityId) {
        StringBuilder query = new StringBuilder("isActive = true");
        Map<String, Object> params = new HashMap<>();

        if (countryId != null) {
            query.append(" and country.id = :countryId");
            params.put("countryId", countryId);
        }
        if (regionId != null) {
            query.append(" and region.id = :regionId");
            params.put("regionId", regionId);
        }
        if (cityId != null) {
            query.append(" and city.id = :cityId");
            params.put("cityId", cityId);
        }
        return find(query.toString(), params).list();
    }


    /**
     * Vérifie s'il existe déjà une adresse par défaut pour un propriétaire
     * Utilisé pour validation avant de définir une nouvelle adresse par défaut
     * @param ownerType le type de propriétaire
     * @param ownerId l'ID du propriétaire
     * @param excludeId l'ID de l'adresse à exclure (pour update)
     * @return true s'il existe déjà une adresse par défaut
     */
    public boolean hasDefaultAddress(OwnerType ownerType, UUID ownerId, UUID excludeId) {
        String query = "ownerType = ?1 and ownerId = ?2 and defaultAddress = true and isActive = true";

        if (excludeId != null) {
            query += " and id != ?3";
            return count(query, ownerType, ownerId, excludeId) > 0;
        }
        return count(query, ownerType, ownerId) > 0;
    }

    /**
     * Vérifie si une adresse peut être supprimée
     * Actuellement retourne toujours true car pas de contraintes spécifiques
     * @param addressId l'ID de l'adresse à vérifier
     * @return true si l'adresse peut être supprimée
     */
    public boolean canBeDeleted(UUID addressId) {
        Address address = findById(addressId);
        return !usageChecker.isInUse(address.getOwnerType(), address.getOwnerId(), addressId);
    }
}