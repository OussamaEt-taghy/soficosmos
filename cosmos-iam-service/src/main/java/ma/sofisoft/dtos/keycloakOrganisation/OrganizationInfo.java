package ma.sofisoft.dtos.keycloakOrganisation;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationInfo {
    private String id;
    private String name;
}
