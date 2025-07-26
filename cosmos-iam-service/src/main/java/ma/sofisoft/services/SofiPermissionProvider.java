package ma.sofisoft.services;
import it.oussama.PermissionProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@ApplicationScoped
public class SofiPermissionProvider implements PermissionProvider {

    @Inject
    JsonWebToken jwt;

    @Override
    public Set<String> getPermissions() {
        Object autorisationClaim = jwt.getClaim("autorisation");

        log.info("[SofiPermissionProvider] Claim 'autorisation' = {}", autorisationClaim);
        log.info("[SofiPermissionProvider] Claim type = {}", autorisationClaim != null ? autorisationClaim.getClass() : "null");

        if (autorisationClaim == null) {
            return Collections.emptySet();
        }

        if (autorisationClaim instanceof List<?> list) {
            return list.stream()
                    .map(this::convertToString)
                    .collect(Collectors.toSet());
        }

        return Set.of(convertToString(autorisationClaim));
    }

    private String convertToString(Object obj) {
        if (obj == null) return "";

        if (obj instanceof JsonString jsonString) {
            return jsonString.getString();
        }

        if (obj instanceof JsonValue jsonValue) {
            String valueString = jsonValue.toString();
            return valueString.replaceAll("^\"|\"$", "");
        }

        return obj.toString();
    }
}