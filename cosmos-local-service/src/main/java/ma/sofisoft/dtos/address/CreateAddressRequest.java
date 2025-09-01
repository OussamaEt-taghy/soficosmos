package ma.sofisoft.dtos.address;

import lombok.*;
import ma.sofisoft.enums.OwnerType;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {

    @NotNull(message = "Owner type is required")
    private OwnerType ownerType;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

    @NotNull(message = "Shipping address flag is required")
    private Boolean shippingAddress;

    @NotNull(message = "Billing address flag is required")
    private Boolean billingAddress;

    @NotNull(message = "Other addresses flag is required")
    private Boolean otherAddresses;

    @NotNull(message = "Country ID is required")
    private UUID countryId;

    @NotNull(message = "Region ID is required")
    private UUID regionId;

    @NotNull(message = "City ID is required")
    private UUID cityId;

    private Boolean detailedAddress = false;

    @Size(max = 500, message = "Address description must not exceed 500 characters")
    private String addressDescription;

    @Size(max = 100, message = "Neighborhood must not exceed 100 characters")
    private String neighborhood;

    @Size(max = 150, message = "Street must not exceed 150 characters")
    private String street;

    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;

    @Size(max = 150, message = "Apartment/Building/Residence must not exceed 150 characters")
    private String apartmentBuildingResidence;

    private Double coordLongitude;

    private Double coordLatitude;

    private Boolean isActive = true;

    private Boolean defaultAddress = false;
}
