package ma.sofisoft.dtos.address;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.*;
import ma.sofisoft.enums.OwnerType;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private UUID id;
    private OwnerType ownerType;
    private UUID ownerId;
    private Boolean shippingAddress;
    private Boolean billingAddress;
    private Boolean otherAddresses;
    private UUID countryId;
    private UUID regionId;
    private UUID cityId;
    private Boolean detailedAddress;
    private String addressDescription;
    private String neighborhood;
    private String street;
    private String postalCode;
    private String apartmentBuildingResidence;
    private Double coordLongitude;
    private Double coordLatitude;
    private Boolean isActive;
    private Boolean defaultAddress;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
