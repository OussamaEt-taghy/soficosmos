package ma.sofisoft.dtos.permissions;
import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private String id;
    private String name;
    private String description;
    private Set<String> roleIds;
    private String groupId;
    private String groupName;
}
