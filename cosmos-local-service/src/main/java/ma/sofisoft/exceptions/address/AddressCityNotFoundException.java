package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class AddressCityNotFoundException extends BusinessException {
    public AddressCityNotFoundException(UUID cityId) {
        super("Impossible de créer l'adresse : la ville avec l'ID " + cityId + " est introuvable",
                "ADDRESS_CITY_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
