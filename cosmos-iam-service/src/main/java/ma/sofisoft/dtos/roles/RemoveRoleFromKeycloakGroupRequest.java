package ma.sofisoft.dtos.roles;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveRoleFromKeycloakGroupRequest {

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Role ID is required")
    private String roleId;
}
