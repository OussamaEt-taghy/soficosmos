package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityInUseException extends BusinessException {
    public CityInUseException(UUID cityId) {
        super("La ville ID " + cityId + " ne peut pas être supprimée car elle est utilisée par des adresses",
                "US84_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}
