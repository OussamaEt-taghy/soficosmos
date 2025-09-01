package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class CityNameAlreadyExistsException extends BusinessException {
    public CityNameAlreadyExistsException(String name) {
        super("Une ville avec le nom '" + name + "' existe déjà",
                "US70_RG2",
                Response.Status.CONFLICT.getStatusCode());
    }
}
