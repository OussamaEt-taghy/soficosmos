package ma.sofisoft.exceptions.region;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class RegionNotFoundException extends BusinessException {
    public RegionNotFoundException(UUID regionId) {
        super("La région avec l'ID " + regionId + " est introuvable",
                "REGION_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}