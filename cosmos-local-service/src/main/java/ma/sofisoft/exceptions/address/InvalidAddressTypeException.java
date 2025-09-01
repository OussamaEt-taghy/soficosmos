package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class InvalidAddressTypeException extends BusinessException {
    public InvalidAddressTypeException() {
        super("Au moins un type d'adresse doit être sélectionné ",
                "INVALID_ADDRESS_TYPE",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}