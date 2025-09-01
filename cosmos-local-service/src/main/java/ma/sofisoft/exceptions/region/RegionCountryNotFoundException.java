package ma.sofisoft.exceptions.region;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

/**
 *  PAYS NON TROUVÉ POUR LA RÉGION
 * Spécifique à Region : quand le country_id fourni n'existe pas
 */
public class RegionCountryNotFoundException extends BusinessException {
    public RegionCountryNotFoundException(UUID countryId) {
        super("Impossible de créer la région : le pays avec l'ID " + countryId + " est introuvable",
                "REGION_COUNTRY_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}