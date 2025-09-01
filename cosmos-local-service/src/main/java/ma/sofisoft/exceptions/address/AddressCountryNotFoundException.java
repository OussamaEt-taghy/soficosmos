package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class AddressCountryNotFoundException extends BusinessException {
    public AddressCountryNotFoundException(UUID countryId) {
        super("Impossible de créer l'adresse : le pays avec l'ID " + countryId + " est introuvable",
                "ADDRESS_COUNTRY_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
