package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityCountryNotFoundException extends BusinessException {
    public CityCountryNotFoundException(UUID countryId) {
        super("Impossible de créer la ville : le pays avec l'ID " + countryId + " est introuvable",
                "CITY_COUNTRY_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}