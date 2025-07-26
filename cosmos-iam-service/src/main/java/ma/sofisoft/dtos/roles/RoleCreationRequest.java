package ma.sofisoft.dtos.roles;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreationRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;

    private String description;
}