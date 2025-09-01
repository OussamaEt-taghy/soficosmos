package ma.sofisoft.exceptions.country;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;
import java.util.UUID;

public class CountryInUseException extends BusinessException {
    public CountryInUseException(UUID countryId) {
        super("Le pays ID " + countryId + " ne peut pas être supprimé car il est utilisé ",
                "US13_RG1",
                Response.Status.CONFLICT.getStatusCode());
    }
}
