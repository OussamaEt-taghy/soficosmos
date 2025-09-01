package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

/**
 *  COHÉRENCE GÉOGRAPHIQUE INVALIDE
 * Quand Country/Region/City ne correspondent pas entre eux
 */
public class AddressGeographicalMismatchException extends BusinessException {
    public AddressGeographicalMismatchException(String message) {
        super("Incohérence géographique dans l'adresse : " + message,
                "ADDRESS_GEOGRAPHICAL_MISMATCH",
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    // Ville n'appartient pas à la région
    public static AddressGeographicalMismatchException cityRegionMismatch(UUID cityId, UUID regionId) {
        return new AddressGeographicalMismatchException(
                "la ville " + cityId + " n'appartient pas à la région " + regionId);
    }

    // Région n'appartient pas au pays
    public static AddressGeographicalMismatchException regionCountryMismatch(UUID regionId, UUID countryId) {
        return new AddressGeographicalMismatchException(
                "la région " + regionId + " n'appartient pas au pays " + countryId);
    }
}
