package ma.sofisoft.dtos.roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.QueryParam;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRolesListRequest {
    @NotBlank(message = "User ID is required")
    @QueryParam("userId")
    private String userId;
}
