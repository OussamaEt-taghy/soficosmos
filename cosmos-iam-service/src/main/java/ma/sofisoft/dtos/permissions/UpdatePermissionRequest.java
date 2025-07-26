package ma.sofisoft.dtos.permissions;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePermissionRequest {
    @NotBlank(message = "Permission ID is required")
    private String id;

    @NotBlank(message = "Permission name is required")
    private String name;

    private String description;
}
