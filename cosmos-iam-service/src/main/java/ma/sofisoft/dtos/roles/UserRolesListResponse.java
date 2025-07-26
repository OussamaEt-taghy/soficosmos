package ma.sofisoft.dtos.roles;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRolesListResponse {
    private String userId;
    private List<String> roles;
}
