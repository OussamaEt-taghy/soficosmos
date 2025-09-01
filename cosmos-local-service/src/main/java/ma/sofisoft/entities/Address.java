package ma.sofisoft.entities;
import jakarta.persistence.*;
import lombok.*;
import ma.sofisoft.enums.OwnerType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "address")
@Getter
@Setter
@ToString(exclude = {"country", "region", "city"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "shipping_address", nullable = false)
    private Boolean shippingAddress;

    @Column(name = "billing_address", nullable = false)
    private Boolean billingAddress;

    @Column(name = "other_addresses", nullable = false)
    private Boolean otherAddresses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Builder.Default
    @Column(name = "detailed_address")
    private Boolean detailedAddress = false;

    @Column(name = "address_description", length = 500)
    private String addressDescription;

    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @Column(name = "street", length = 150)
    private String street;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "apartment_building_residence", length = 150)
    private String apartmentBuildingResidence;

    @Column(name = "coord_longitude")
    private Double coordLongitude;

    @Column(name = "coord_latitude")
    private Double coordLatitude;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "default_address")
    private Boolean defaultAddress = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
