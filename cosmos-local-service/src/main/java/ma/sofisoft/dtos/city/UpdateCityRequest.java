package ma.sofisoft.dtos.city;
import lombok.*;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCityRequest {

    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private UUID countryId;

    private UUID regionId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
