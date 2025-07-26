package ma.sofisoft.dtos.permissionGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    private String description;
}
