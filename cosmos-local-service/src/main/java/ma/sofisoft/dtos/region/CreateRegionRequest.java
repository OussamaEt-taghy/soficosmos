package ma.sofisoft.dtos.region;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRegionRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Country ID is required")
    private UUID countryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
