package ma.sofisoft.configuration.multitenancy;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import lombok.extern.slf4j.Slf4j;

@RequestScoped
@PersistenceUnitExtension
@Slf4j
public class CurrentTenantResolver implements TenantResolver {

    @PostConstruct
    void init() {
        log.info("CurrentTenantResolver initialisé");
    }

    @Override
    public String getDefaultTenantId() {
        log.debug("getDefaultTenantId appelée - Retour: public (démarrage app)");
        return "public";
    }

    @Override
    public String resolveTenantId() {
        String schema = TenantContext.getCurrentSchema();
        if (schema == null || schema.trim().isEmpty()) {
            log.error(" Aucun tenant trouvé dans TenantContext !");
            throw new IllegalStateException("Aucun tenant (schéma) actif trouvé dans le contexte !");
        }
        log.debug(" Tenant résolu: {}", schema);
        return schema;
    }
}
