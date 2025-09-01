package ma.sofisoft.exceptions.address;

import jakarta.ws.rs.core.Response;
import ma.sofisoft.exceptions.BusinessException;

import java.util.UUID;

public class AddressRegionNotFoundException extends BusinessException {
    public AddressRegionNotFoundException(UUID regionId) {
        super("Impossible de créer l'adresse : la région avec l'ID " + regionId + " est introuvable",
                "ADDRESS_REGION_NOT_FOUND",
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}