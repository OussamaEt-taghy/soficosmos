package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityRegionCountryMismatchException extends BusinessException {
    public CityRegionCountryMismatchException(UUID regionId, UUID countryId) {
        super("La région " + regionId + " n'appartient pas au pays " + countryId,
                "CITY_REGION_COUNTRY_MISMATCH",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
