package ma.sofisoft.configuration.multitenancy;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Priority(Priorities.USER)
@Slf4j
public class TenantResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        try {
            // Nettoyer TOUJOURS le contexte, même en cas d'exception
            String currentSchema = TenantContext.getCurrentSchema();
            if (currentSchema != null) {
                log.debug("Nettoyage contexte schéma: {}", currentSchema);
            }
        } finally {
            // Forcer le nettoyage dans tous les cas
            TenantContext.clear();
        }
    }
}
