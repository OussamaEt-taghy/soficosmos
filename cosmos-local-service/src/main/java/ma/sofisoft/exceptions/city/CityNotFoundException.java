package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityNotFoundException extends BusinessException {
    public CityNotFoundException(UUID cityId) {
        super("La ville avec l'ID " + cityId + " est introuvable",
                "CITY_NOT_FOUND",
                Response.Status.NOT_FOUND.getStatusCode());
    }
}