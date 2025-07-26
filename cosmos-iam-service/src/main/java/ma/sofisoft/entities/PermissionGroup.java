package ma.sofisoft.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permission_group",
        indexes = {
                @Index(name = "idx_perm_group_name", columnList = "name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroup {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
        private UUID id;

        @Column(name = "name", length = 255, nullable = false)
        private String name;

        @Column(name = "description", length = 500)
        private String description;

        @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<Permission> permissions = new HashSet<>();
}
