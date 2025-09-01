package ma.sofisoft.exceptions.country;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class CountryNameAlreadyExistsException extends BusinessException {
    public CountryNameAlreadyExistsException(String name) {
        super("Un pays avec le nom '" + name + "' existe déjà",
                "US9_RG2",
                Response.Status.CONFLICT.getStatusCode());
    }
}
