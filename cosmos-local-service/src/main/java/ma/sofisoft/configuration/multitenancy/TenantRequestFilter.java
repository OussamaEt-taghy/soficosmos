package ma.sofisoft.configuration.multitenancy;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.exceptions.organizations.InvalidTenantException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;


@Provider
@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class TenantRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTenantException("Token d'authentification manquant");
        }
        try {
            String token = authHeader.substring(7);
            String organizationName = extractOrganizationName(token);
            if (organizationName == null || organizationName.trim().isEmpty()) {
                throw new InvalidTenantException("Organisation non trouvée dans le token");
            }
            TenantContext.setCurrentSchema(organizationName);
            log.debug("Schéma configuré: {} pour la requête", organizationName);
        } catch (InvalidTenantException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur extraction organisation du token: {}", e.getMessage());
            throw new InvalidTenantException("Token invalide ou corrompu");
        }
    }

    public String extractOrganizationName(String token) {
        try {
            JWSInput jwsInput = new JWSInput(token);
            AccessToken accessToken = jwsInput.readJsonContent(AccessToken.class);
            Object orgClaim = accessToken.getOtherClaims().get("organization");
            if (orgClaim == null) {
                throw new RuntimeException("The token does not contain the 'organization' claim");
            }
            return orgClaim.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while decoding the token", e);
        }
    }
}

