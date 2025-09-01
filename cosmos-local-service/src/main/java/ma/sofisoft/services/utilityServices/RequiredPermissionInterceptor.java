package ma.sofisoft.services.utilityServices;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.PermissionProvider;
import ma.sofisoft.RequiredPermission;
import ma.sofisoft.Secured;
import ma.sofisoft.LogicalOperator;
import ma.sofisoft.exceptions.permissions.PermissionDeniedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🛡️ INTERCEPTOR DE SÉCURITÉ - VERSION CORRIGÉE
 *
 * RÔLE PRINCIPAL:
 * Cet interceptor s'exécute AVANT chaque méthode annotée avec @Secured
 * Il vérifie si l'utilisateur a le droit d'accéder à cette méthode
 *
 * NOUVELLE LOGIQUE:
 * 1. OR entre groupes : Si l'utilisateur "passe" AU MOINS UN groupe → Accès autorisé
 * 2. Operator vient de l'annotation : AND/OR s'applique entre permissions d'un même groupe
 *
 * EXEMPLE:
 * @RequiredPermission(groups = {"show_user", "admin_user"}, operator = LogicalOperator.OR)
 * → Si user satisfait "show_user" avec operator OR → ✅ OK
 * → Sinon, si user satisfait "admin_user" avec operator OR → ✅ OK
 * → Sinon → ❌ Refusé
 */
@Slf4j
@Interceptor
@Secured  // Cet interceptor s'applique aux classes/méthodes annotées @Secured
@Priority(Interceptor.Priority.APPLICATION)  // Priorité d'exécution
public class RequiredPermissionInterceptor {

    // Informations sur l'utilisateur connecté (injecté par Quarkus)
    @Inject
    SecurityIdentity identity;

    // Service pour lire les permissions du JWT (notre SofiPermissionProvider)
    @Inject
    PermissionProvider permissionProvider;

    /**
     * 🎯 MÉTHODE PRINCIPALE: S'exécute AVANT chaque méthode @Secured
     *
     * WORKFLOW:
     * 1. Récupérer l'annotation @RequiredPermission (si elle existe)
     * 2. Vérifier si l'utilisateur est connecté
     * 3. Vérifier si l'utilisateur a les droits requis
     * 4. Si OK → continuer, sinon → bloquer avec erreur HTTP
     */
    @AroundInvoke
    public Object checkPermission(InvocationContext ctx) throws Exception {
        // ÉTAPE 1: Récupérer des infos sur la méthode appelée
        Method method = ctx.getMethod();  // Quelle méthode est appelée ?

        // ÉTAPE 2: Chercher si cette méthode a une annotation @RequiredPermission
        RequiredPermission annotation = getAnnotation(method, ctx.getTarget().getClass());

        // ÉTAPE 3: Si pas d'annotation → pas de restrictions → accès libre
        if (annotation == null) {
            log.debug("[Interceptor] Aucune annotation @RequiredPermission trouvée, accès autorisé.");
            return ctx.proceed();  // Continuer l'exécution normale de la méthode
        }

        // ÉTAPE 4: Vérifier si l'utilisateur est connecté (authentifié)
        if (identity.isAnonymous()) {
            log.warn("[Interceptor] Utilisateur anonyme. Accès refusé.");
            // Retourner HTTP 401 Unauthorized
            throw new WebApplicationException("Authentication required", Response.Status.UNAUTHORIZED);
        }

        // ÉTAPE 5: Vérifier les permissions (la logique principale)
        boolean hasAccess = checkAccess(annotation);

        // ÉTAPE 6: Décision finale
        if (!hasAccess) {
            // Accès refusé → Utiliser votre exception personnalisée
            String requiredGroups = Arrays.toString(annotation.groups());
            String errorMessage = String.format("Groupes requis: %s avec operator: %s",
                    requiredGroups, annotation.operator());

            log.error("[Interceptor] Accès refusé. {}", errorMessage);

            // ➕ UTILISATION DE VOTRE EXCEPTION PERSONNALISÉE
            throw new PermissionDeniedException(errorMessage);
        }

        // ÉTAPE 7: Accès autorisé → Continuer l'exécution
        log.info("[Interceptor] ✅ Accès autorisé");
        return ctx.proceed();  // Exécuter la méthode originale
    }

    /**
     * 🧠 LOGIQUE PRINCIPALE CORRIGÉE: Implémente la nouvelle logique
     *
     * NOUVELLE LOGIQUE:
     * - OR entre groupes (si au moins un groupe "passe" → accès OK)
     * - L'operator vient de l'annotation et s'applique à TOUS les groupes
     *
     * EXEMPLE:
     * @RequiredPermission(groups = {"show_user", "admin_user"}, operator = LogicalOperator.OR)
     *
     * Pour chaque groupe ("show_user", puis "admin_user"):
     * 1. L'user satisfait-il ce groupe avec l'operator OR ?
     * Si OUI pour au moins un groupe → Accès autorisé
     */
    private boolean checkAccess(RequiredPermission annotation) {
        // DONNÉES DE L'ANNOTATION
        String[] requiredGroups = annotation.groups();  // Ex: ["show_user", "admin_user"]
        LogicalOperator operator = annotation.operator();  // Ex: AND ou OR

        // ÉTAPE 1: NORMALISER les groupes requis
        // Pourquoi ? Car "Show_User" ≠ "show_user" en informatique
        // On transforme tout en minuscules et on supprime les espaces
        List<String> normalizedRequiredGroups = Arrays.stream(requiredGroups)
                .map(g -> g != null ? g.trim().toLowerCase(Locale.ROOT) : null)  // "Show_User " → "show_user"
                .filter(g -> g != null && !g.isEmpty())  // Supprimer les valeurs nulles/vides
                .collect(Collectors.toList());

        // CAS PARTICULIER: Aucun groupe requis = accès libre
        if (normalizedRequiredGroups.isEmpty()) {
            log.debug("[Interceptor] Aucun groupe requis, accès autorisé");
            return true;
        }

        // ÉTAPE 2: LOGS DE DEBUG pour voir ce qu'on va vérifier
        log.debug("[Interceptor] 🔍 Vérification accès:");
        log.debug("[Interceptor]   - Groupes requis: {}", normalizedRequiredGroups);
        log.debug("[Interceptor]   - Opérateur pour TOUS les groupes: {}", operator);

        // ÉTAPE 3: BOUCLE SUR CHAQUE GROUPE REQUIS
        // Logique OR entre groupes : si AU MOINS UN groupe "passe" → succès
        for (String requiredGroup : normalizedRequiredGroups) {
            log.debug("[Interceptor] 🔍 Test du groupe: '{}'", requiredGroup);

            // NOUVELLE LOGIQUE: Utiliser la méthode corrigée du PermissionProvider
            // Cette méthode fait TOUT le travail :
            // 1. Vérifie si le groupe existe
            // 2. Récupère les permissions du groupe
            // 3. Applique l'operator
            // 4. Retourne true/false
            boolean groupPassed = permissionProvider.satisfiesPermissionGroupWithOperator(
                    requiredGroup,
                    operator
            );

            // LOGS DE DEBUG
            if (groupPassed) {
                log.info("[Interceptor] ✅ Accès autorisé via groupe '{}' avec operator {}",
                        requiredGroup, operator);
                return true; // OR entre groupes : un seul groupe qui passe suffit !
            } else {
                log.debug("[Interceptor] ❌ Groupe '{}' échoue avec operator {}",
                        requiredGroup, operator);
                // Continue la boucle pour essayer le groupe suivant
            }
        }

        // ÉTAPE 4: Si on arrive ici, AUCUN groupe n'a donné accès
        log.warn("[Interceptor] ❌ Aucun des groupes requis ne donne accès");
        return false; // Accès refusé
    }

    /**
     * 🔍 MÉTHODE HELPER: Récupère l'annotation @RequiredPermission
     *
     * LOGIQUE:
     * 1. D'abord chercher sur la méthode (priorité)
     * 2. Sinon chercher sur la classe
     * 3. Sinon retourner null (pas d'annotation)
     *
     * EXEMPLE:
     * @RequiredPermission(groups = {"show_user"}, operator = LogicalOperator.OR)  // ← sur la classe
     * public class TestController {
     *
     *     @RequiredPermission(groups = {"admin_user"}, operator = LogicalOperator.AND)  // ← sur la méthode (priorité)
     *     public void method1() { ... }
     *
     *     public void method2() { ... }                 // ← hérite de la classe
     * }
     */
    private RequiredPermission getAnnotation(Method method, Class<?> clazz) {
        // PRIORITÉ 1: Annotation sur la méthode
        if (method.isAnnotationPresent(RequiredPermission.class)) {
            RequiredPermission annotation = method.getAnnotation(RequiredPermission.class);
            log.debug("[Interceptor] 🎯 Annotation trouvée sur la méthode: {} - groups: {}, operator: {}",
                    method.getName(), Arrays.toString(annotation.groups()), annotation.operator());
            return annotation;
        }
        // PRIORITÉ 2: Annotation sur la classe
        else if (clazz.isAnnotationPresent(RequiredPermission.class)) {
            RequiredPermission annotation = clazz.getAnnotation(RequiredPermission.class);
            log.debug("[Interceptor] 🎯 Annotation trouvée sur la classe: {} - groups: {}, operator: {}",
                    clazz.getSimpleName(), Arrays.toString(annotation.groups()), annotation.operator());
            return annotation;
        }

        // Aucune annotation trouvée
        log.debug("[Interceptor] 🎯 Aucune annotation @RequiredPermission trouvée");
        return null;
    }
}