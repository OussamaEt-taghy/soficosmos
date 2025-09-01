package ma.sofisoft.exceptions.region;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class RegionCodeAlreadyExistsException extends BusinessException {
    public RegionCodeAlreadyExistsException(String code, UUID countryId) {
        super("Le code région '" + code + "' existe déjà dans le pays " + countryId,
                "US14_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}