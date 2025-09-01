package ma.sofisoft.exceptions.country;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CountryNotFoundException extends BusinessException {
    public CountryNotFoundException(UUID countryId) {
        super("Le pays avec l'ID " + countryId + " est introuvable",
                "COUNTRY_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }

    public CountryNotFoundException(String countryCode) {
        super("Le pays avec code '" + countryCode + "' est introuvable",
                "COUNTRY_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}

