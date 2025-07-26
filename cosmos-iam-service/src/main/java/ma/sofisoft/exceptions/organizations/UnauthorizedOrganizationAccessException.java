package ma.sofisoft.exceptions.organizations;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import ma.sofisoft.exceptions.BusinessException;


@Getter
    public class UnauthorizedOrganizationAccessException extends BusinessException {
        public UnauthorizedOrganizationAccessException(String organizationName) {
            super("Accès non autorisé  n'appartient pas à l'organisation " + organizationName + "'.",
                    "UNAUTHORIZED_ORGANIZATION_ACCESS",
                    Response.Status.FORBIDDEN.getStatusCode());
        }
    }

