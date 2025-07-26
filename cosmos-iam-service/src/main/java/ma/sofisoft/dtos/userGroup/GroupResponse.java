package ma.sofisoft.dtos.userGroup;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private String id;
    private String name;
    private List<String> roles;
}
