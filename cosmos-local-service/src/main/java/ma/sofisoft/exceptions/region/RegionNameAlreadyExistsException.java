package ma.sofisoft.exceptions.region;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class RegionNameAlreadyExistsException extends BusinessException {
    public RegionNameAlreadyExistsException(String name) {
        super("Une Region avec le nom '" + name + "' existe déjà",
                "US14_RG2",
                Response.Status.CONFLICT.getStatusCode());
    }
}
