package it.oussama.mapper;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationData {
    private Set<String> permissions = new HashSet<>();
    private Set<String> permissionGroups = new HashSet<>();
}
