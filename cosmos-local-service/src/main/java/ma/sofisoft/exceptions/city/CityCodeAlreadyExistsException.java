package ma.sofisoft.exceptions.city;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class CityCodeAlreadyExistsException extends BusinessException {
    public CityCodeAlreadyExistsException(String code, UUID countryId, UUID regionId) {
        super("Le code ville '" + code + "' existe déjà dans le pays " + countryId + " et la région " + regionId,
                "US70_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}