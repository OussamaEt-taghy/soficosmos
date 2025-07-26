package ma.sofisoft.dtos.autorisations;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorisationResponse {
    private String id;
    private String roleId;

    private String permissionId;
    private String permissionName;
    private String permissionDescription;
    private String groupName;
}
