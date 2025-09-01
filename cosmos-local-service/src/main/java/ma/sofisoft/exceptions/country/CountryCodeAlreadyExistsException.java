package ma.sofisoft.exceptions.country;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

public class CountryCodeAlreadyExistsException extends BusinessException {
    public CountryCodeAlreadyExistsException(String code) {
        super("pays avec le code '" + code + "' deja  exists",
                "US9_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}