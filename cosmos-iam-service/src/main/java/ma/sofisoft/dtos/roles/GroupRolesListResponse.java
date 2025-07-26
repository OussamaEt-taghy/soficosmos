package ma.sofisoft.dtos.roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRolesListResponse {
    private String groupId;
    private String groupName;
    private List<String> roles;
}
