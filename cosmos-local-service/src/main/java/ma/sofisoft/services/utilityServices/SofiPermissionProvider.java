package ma.sofisoft.services.utilityServices;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.PermissionProvider;
import ma.sofisoft.PermissionGroupDefinition;
import ma.sofisoft.LogicalOperator;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cette classe lit les claims du JWT généré par le SPI Keycloak et fournit :
 * - Les permissions individuelles de l'utilisateur
 * - Les définitions des groupes de permissions (DYNAMIQUES depuis le token)
 * - La vérification avec operator fourni par l'annotation
 */
@Slf4j
@ApplicationScoped
public class SofiPermissionProvider implements PermissionProvider {

    @Inject
    JsonWebToken jwt;
    /**
     *  Récupère les permissions individuelles de l'utilisateur depuis le JWT
     */
    @Override
    public Set<String> getPermissions() {
        Set<String> permissions = extractClaimAsSet("autorisation");
        log.info("[SofiPermissionProvider] ✅ Permissions utilisateur: {}", permissions);
        return permissions;
    }

    /**
     * 📋 Fournit les définitions des groupes de permissions DYNAMIQUEMENT depuis le token JWT
     *
     * Cette méthode lit les groupes et leurs permissions depuis les claims JWT.
     * L'operator sera fourni par l'annotation @RequiredPermission.
     */
    @Override
    public Map<String, PermissionGroupDefinition> getPermissionGroupDefinitions() {
        Map<String, PermissionGroupDefinition> definitions = new HashMap<>();
        Map<String, Set<String>> groupPermissions = extractGroupPermissions();
        if (groupPermissions.isEmpty()) {
            log.warn("[SofiPermissionProvider]  Aucun groupe de permissions trouvé dans le token");
            return definitions;
        }
        for (Map.Entry<String, Set<String>> entry : groupPermissions.entrySet()) {
            String groupName = entry.getKey();
            Set<String> permissions = entry.getValue();
            definitions.put(groupName, new PermissionGroupDefinition(permissions));
            log.debug("[SofiPermissionProvider] 📋 Groupe '{}' avec {} permissions",
                    groupName, permissions.size());
        }
        log.info("[SofiPermissionProvider] ✅ Définitions des groupes chargées dynamiquement: {} groupes",
                definitions.size());
        return definitions;
    }

    @Override
    public boolean satisfiesPermissionGroupWithOperator(String permissionGroupName, LogicalOperator operator) {
        if (permissionGroupName == null || permissionGroupName.trim().isEmpty()) {
            log.debug("[SofiPermissionProvider] ⚠️ Nom de groupe null ou vide");
            return false;
        }
        if (operator == null) {
            log.debug("[SofiPermissionProvider] ⚠️ Operator null");
            return false;
        }
        String normalizedGroupName = permissionGroupName.trim().toLowerCase();

        // ÉTAPE 1: Récupérer les permissions du groupe depuis le token
        Map<String, Set<String>> groupPermissions = extractGroupPermissions();
        Set<String> requiredPermissions = groupPermissions.get(normalizedGroupName);

        if (requiredPermissions == null || requiredPermissions.isEmpty()) {
            log.warn("[SofiPermissionProvider] ⚠️ Groupe '{}' non trouvé ou vide", normalizedGroupName);
            return false;
        }

        // ÉTAPE 2: Récupérer les permissions de l'utilisateur
        Set<String> userPermissions = getPermissions();

        // ÉTAPE 3: Appliquer l'operator fourni en paramètre (depuis l'annotation)
        boolean result = operator == LogicalOperator.AND
                ? requiredPermissions.stream().allMatch(userPermissions::contains)  // TOUTES les permissions
                : requiredPermissions.stream().anyMatch(userPermissions::contains); // AU MOINS UNE permission

        log.debug("[SofiPermissionProvider] 🔍 Groupe '{}' avec operator {}: {} (user a: {}, requis: {})",
                normalizedGroupName, operator, result, userPermissions, requiredPermissions);

        return result;
    }

    // ========== MÉTHODES PRIVÉES D'EXTRACTION ==========

    /**
     * 🔧 Récupère le mapping groupe→permissions depuis le claim JWT
     */
    private Map<String, Set<String>> extractGroupPermissions() {
        try {
            Object claim = jwt.getClaim("group_permissions");

            log.debug("[SofiPermissionProvider] 🔧 Claim 'group_permissions' = {}", claim);

            if (claim == null) {
                log.warn("[SofiPermissionProvider] ⚠️ Claim 'group_permissions' absent du token");
                return Collections.emptyMap();
            }

            if (claim instanceof Map<?, ?> map) {
                Map<String, Set<String>> result = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String groupName = convertToString(entry.getKey());
                    if (groupName == null || groupName.isEmpty()) {
                        continue;
                    }
                    groupName = groupName.trim().toLowerCase(Locale.ROOT);

                    Set<String> permissions = extractPermissionsFromValue(entry.getValue());
                    if (!permissions.isEmpty()) {
                        result.put(groupName, permissions);
                    }
                }

                log.info("[SofiPermissionProvider] ✅ group_permissions extracted: {}", result);
                return result;
            }

            log.warn("[SofiPermissionProvider] ❌ group_permissions claim n'est pas un Map: {}",
                    claim.getClass().getSimpleName());
            return Collections.emptyMap();

        } catch (Exception e) {
            log.error("[SofiPermissionProvider] ❌ Erreur extraction group_permissions: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 🔧 Extrait les permissions d'une valeur avec gestion des types multiples
     */
    private Set<String> extractPermissionsFromValue(Object value) {
        if (value == null) {
            return Collections.emptySet();
        }

        log.debug("[SofiPermissionProvider] 🔧 extractPermissionsFromValue: type = {}",
                value.getClass().getSimpleName());

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(this::convertToString)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
        }

        if (value instanceof Set<?> set) {
            return set.stream()
                    .map(this::convertToString)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
        }

        String single = convertToString(value);
        if (single != null && !single.isEmpty()) {
            return Set.of(single.trim().toLowerCase(Locale.ROOT));
        }

        return Collections.emptySet();
    }

    /**
     * 🔧 Extrait un claim JWT comme Set<String> avec normalisation
     */
    private Set<String> extractClaimAsSet(String claimName) {
        try {
            Object claim = jwt.getClaim(claimName);

            log.debug("[SofiPermissionProvider] 🔧 extractClaimAsSet('{}') = {}", claimName, claim);

            if (claim == null) {
                log.warn("[SofiPermissionProvider] ⚠️ Claim '{}' absent du token", claimName);
                return Collections.emptySet();
            }

            if (claim instanceof List<?> list) {
                Set<String> result = list.stream()
                        .map(this::convertToString)
                        .filter(s -> s != null && !s.isEmpty())
                        .map(s -> s.trim().toLowerCase(Locale.ROOT)) // 🧹 Normalisation
                        .collect(Collectors.toSet());

                log.info("[SofiPermissionProvider] ✅ {} extracted: {}", claimName, result);
                return result;
            }

            String single = convertToString(claim);
            if (single != null && !single.isEmpty()) {
                return Set.of(single.trim().toLowerCase(Locale.ROOT));
            }

            log.warn("[SofiPermissionProvider] ⚠️ Claim '{}' vide ou type inconnu: {}",
                    claimName, claim.getClass().getSimpleName());
            return Collections.emptySet();

        } catch (Exception e) {
            log.error("[SofiPermissionProvider] ❌ Erreur extraction claim '{}': {}", claimName, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 🔧 Convertit un objet JWT en String avec gestion des types spéciaux
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JsonString jsonString) {
            return jsonString.getString();
        }
        if (obj instanceof JsonValue jsonValue) {
            String valueString = jsonValue.toString();
            return valueString.replaceAll("^\"|\"$", "");
        }
        return obj.toString().trim();
    }
}