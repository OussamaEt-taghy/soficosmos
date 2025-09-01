package ma.sofisoft.exceptions.region;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class RegionInUseException extends BusinessException {
    public RegionInUseException(UUID regionId) {
        super("La région ID " + regionId + " ne peut pas être supprimée car elle est utilisée ",
                " US56_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}
