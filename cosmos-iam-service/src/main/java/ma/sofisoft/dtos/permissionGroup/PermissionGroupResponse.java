package ma.sofisoft.dtos.permissionGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupResponse {
    private String id;
    private String name;
    private String description;

    private List<String> permissionNames;
}
