package ma.sofisoft.dtos.userGroup;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateKeycloakGroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;

}
