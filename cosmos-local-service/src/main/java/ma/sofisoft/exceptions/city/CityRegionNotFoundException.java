package ma.sofisoft.exceptions.city;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityRegionNotFoundException extends BusinessException {
    public CityRegionNotFoundException(UUID regionId) {
        super("Impossible de créer la ville : la région avec l'ID " + regionId + " est introuvable",
                "CITY_REGION_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
