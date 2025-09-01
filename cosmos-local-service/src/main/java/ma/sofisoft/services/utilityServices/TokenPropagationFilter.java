package ma.sofisoft.services.utilityServices;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Provider
@Priority(1000)
public class TokenPropagationFilter implements ClientRequestFilter, ContainerRequestFilter {

    @Inject
    SecurityIdentity securityIdentity;

    // ================================================================================================
    // FILTRE ENTRANT : Capture des requêtes qui arrivent dans le service
    // ================================================================================================
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.info("🔵 =============== REQUÊTE ENTRANTE ===============");
        log.info("📥 Méthode: {} | URI: {}",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri());

        // Capture des headers d'autorisation
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("✅ Token Bearer reçu - Longueur: {} caractères", token.length());
                log.info("🔍 Token (premiers 50 chars): {}...",
                        token.length() > 50 ? token.substring(0, 50) : token);
            } else {
                log.warn("⚠️ Header Authorization présent mais pas Bearer: {}",
                        authHeader.substring(0, Math.min(authHeader.length(), 30)));
            }
        } else {
            log.warn("❌ Aucun header Authorization trouvé dans la requête entrante");
        }

        // Log des autres headers importants
        log.info("📋 Headers reçus:");
        requestContext.getHeaders().forEach((key, values) -> {
            if (key.toLowerCase().contains("auth") ||
                    key.toLowerCase().contains("token") ||
                    key.toLowerCase().contains("content") ||
                    key.toLowerCase().contains("accept")) {
                log.info("   • {}: {}", key, values);
            }
        });

        log.info("🔵 ================================================");
    }

    // ================================================================================================
    // FILTRE SORTANT : Propagation du token vers les services externes
    // ================================================================================================
    @Override
    public void filter(ClientRequestContext requestContext) {
        log.info("🔴 =============== REQUÊTE SORTANTE ===============");
        log.info("🔄 TokenPropagationFilter - Appel vers: {} {}",
                requestContext.getMethod(), requestContext.getUri());

        try {
            // Étape 1 : Vérifier la disponibilité des informations de sécurité
            if (securityIdentity == null) {
                log.error("❌ PROBLÈME: SecurityIdentity est null - Configuration OIDC incorrecte");
                return;
            }

            // Étape 2 : Vérifier l'authentification de l'utilisateur
            if (securityIdentity.isAnonymous()) {
                log.error("❌ PROBLÈME: Utilisateur non authentifié - Token manquant dans la requête originale");
                return;
            }

            log.info("✅ Utilisateur authentifié: {}", securityIdentity.getPrincipal().getName());

            // Étape 3 : Vérifier le type de token (doit être JWT OIDC)
            if (!(securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal)) {
                log.error("❌ PROBLÈME: Token n'est pas JWT OIDC - Type: {}",
                        securityIdentity.getPrincipal().getClass().getSimpleName());
                return;
            }

            // Étape 4 : Extraire le token JWT
            OidcJwtCallerPrincipal jwtPrincipal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
            String token = jwtPrincipal.getRawToken();

            // Étape 5 : Valider le token
            if (token == null || token.trim().isEmpty()) {
                log.error("❌ PROBLÈME: Token JWT est null ou vide");
                return;
            }

            // Log principal : Token trouvé et informations utiles
            log.info("✅ Token JWT récupéré - Longueur: {} caractères", token.length());
            log.info("✅ Token valide jusqu'à: {}", jwtPrincipal.getExpirationTime());
            log.info("🔍 Token à envoyer (premiers 50 chars): {}...",
                    token.length() > 50 ? token.substring(0, 50) : token);

            // Étape 6 : Ajouter le token à la requête sortante
            requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            // Log principal : Confirmation de l'ajout
            log.info("✅ TOKEN PROPAGÉ avec succès vers {}", requestContext.getUri().getHost());

            // Log des headers sortants
            log.info("📋 Headers envoyés vers le service externe:");
            requestContext.getHeaders().forEach((key, values) -> {
                if (key.toLowerCase().contains("auth") ||
                        key.toLowerCase().contains("token") ||
                        key.toLowerCase().contains("content") ||
                        key.toLowerCase().contains("accept")) {
                    log.info("   • {}: {}", key, values);
                }
            });

        } catch (Exception e) {
            // Log principal : Erreur critique
            log.error("❌ ERREUR CRITIQUE dans la propagation du token: {}", e.getMessage());
            log.debug("Stack trace détaillée:", e); // Stack trace en debug seulement
        }
        log.info("🔴 ================================================");
    }
}
