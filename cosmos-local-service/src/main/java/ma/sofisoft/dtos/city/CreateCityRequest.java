package ma.sofisoft.dtos.city;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// jnews = > google wordplace
public class CreateCityRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Country ID is required")
    private UUID countryId;

    @NotNull(message = "Region ID is required")
    private UUID regionId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
