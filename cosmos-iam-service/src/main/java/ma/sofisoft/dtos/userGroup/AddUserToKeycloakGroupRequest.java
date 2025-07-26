package ma.sofisoft.dtos.userGroup;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddUserToKeycloakGroupRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Group ID is required")
    private String groupId;
}

