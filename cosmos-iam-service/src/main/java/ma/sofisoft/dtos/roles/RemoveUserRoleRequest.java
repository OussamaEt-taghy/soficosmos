package ma.sofisoft.dtos.roles;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveUserRoleRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Role ID is required")
    private String roleId;
}