package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class AddressNotFoundException extends BusinessException {
    public AddressNotFoundException(UUID addressId) {
        super("L'adresse avec l'ID " + addressId + " est introuvable",
                "ADDRESS_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}
