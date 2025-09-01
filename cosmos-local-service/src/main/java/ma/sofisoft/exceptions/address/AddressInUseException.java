package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class AddressInUseException extends BusinessException {
    public AddressInUseException(UUID addressId) {
        super("L'adresse ID " + addressId + " ne peut pas être supprimée car elle est utilisée par d'autres services",
                "ADDRESS_IN_USE",
                Response.Status.CONFLICT.getStatusCode());
    }
}