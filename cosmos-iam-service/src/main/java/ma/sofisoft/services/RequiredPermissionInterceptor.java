package ma.sofisoft.services;
import io.quarkus.security.identity.SecurityIdentity;
import it.oussama.PermissionProvider;
import it.oussama.RequiredPermission;
import it.oussama.Secured;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.exceptions.permissions.PermissionDeniedException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Interceptor
@Secured
@Priority(Interceptor.Priority.APPLICATION)
public class RequiredPermissionInterceptor {
    @Inject
    SecurityIdentity identity;

    @Inject
    PermissionProvider permissionProvider;

    @AroundInvoke
    public Object checkPermission(InvocationContext ctx) throws Exception {
        Method method = ctx.getMethod();
        RequiredPermission annotation = getAnnotation(method, ctx.getTarget().getClass());

        if (annotation == null || annotation.value().length == 0) {
            log.info(" [Interceptor] Aucune annotation @RequiredPermission trouvée, accès autorisé.");
            return ctx.proceed();
        }

        if (identity.isAnonymous()) {
            log.warn(" [Interceptor] Utilisateur anonyme. Accès refusé.");
            throw new SecurityException("Authentication required");
        }

        String[] requiredPermissions = annotation.value();
        log.info(" [Interceptor] Permissions requises : {}", Arrays.toString(requiredPermissions));

        Set<String> userPermissions = permissionProvider.getPermissions();
        log.debug(" [Interceptor] Permissions disponibles : {}", userPermissions);

        boolean hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);

        if (!hasPermission) {
            log.error(" [Interceptor] Aucune des permissions requises n'est présente.");
            throw new PermissionDeniedException(String.join(" OR ", requiredPermissions));
        }

        log.info(" [Interceptor] Au moins une permission correspondante trouvée.");
        return ctx.proceed();
    }

    private RequiredPermission getAnnotation(Method method, Class<?> clazz) {
        if (method.isAnnotationPresent(RequiredPermission.class)) {
            return method.getAnnotation(RequiredPermission.class);
        } else if (clazz.isAnnotationPresent(RequiredPermission.class)) {
            return clazz.getAnnotation(RequiredPermission.class);
        }
        return null;
    }
}