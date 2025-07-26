package ma.sofisoft.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "autorisation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autorisation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "idRole", length = 36, nullable = false)
    private String idRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPermission", nullable = false)
    private Permission permission;
}
