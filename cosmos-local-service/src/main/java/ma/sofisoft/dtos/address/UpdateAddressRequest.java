package ma.sofisoft.dtos.address;

import lombok.*;
import ma.sofisoft.enums.OwnerType;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAddressRequest {

    private OwnerType ownerType;

    private UUID ownerId;

    private Boolean shippingAddress;

    private Boolean billingAddress;

    private Boolean otherAddresses;

    private UUID countryId;

    private UUID regionId;

    private UUID cityId;

    private Boolean detailedAddress;

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

    private Boolean isActive;

    private Boolean defaultAddress;
}
