package ma.sofisoft.dtos.permissions;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePermissionRequest {
    @NotBlank(message = "Permission name is required")
    private String name;
    private String description;
}
