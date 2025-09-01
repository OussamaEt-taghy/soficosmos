package ma.sofisoft.services.address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.address.AddressResponse;
import ma.sofisoft.dtos.address.CreateAddressRequest;
import ma.sofisoft.dtos.address.UpdateAddressRequest;
import ma.sofisoft.entities.Address;
import ma.sofisoft.entities.City;
import ma.sofisoft.entities.Country;
import ma.sofisoft.entities.Region;
import ma.sofisoft.exceptions.address.*;
import ma.sofisoft.mappers.AddressMapper;
import ma.sofisoft.repositories.AddressRepository;
import ma.sofisoft.repositories.CityRepository;
import ma.sofisoft.repositories.CountryRepository;
import ma.sofisoft.repositories.RegionRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final AddressMapper addressMapper;

    @Inject
    public AddressServiceImpl(AddressRepository addressRepository,
                              CountryRepository countryRepository,
                              RegionRepository regionRepository,
                              CityRepository cityRepository,
                              AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.countryRepository = countryRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
        this.addressMapper = addressMapper;
    }

    @Override
    @Transactional
    public AddressResponse createAddress(CreateAddressRequest dto, String createdBy) {
        log.info("Creating new address for owner: {} ({})", dto.getOwnerId(), dto.getOwnerType());
        Country country = resolveCountry(dto.getCountryId());
        Region region = resolveRegion(dto.getRegionId());
        City city = resolveCity(dto.getCityId());
        validateGeographicalConsistency(country, region, city);
        validateAddressTypes(dto);

        // Validation adresse par défaut (si demandée)
        /*if (Boolean.TRUE.equals(dto.getDefaultAddress())) {
            validateDefaultAddress(dto.getOwnerType(), dto.getOwnerId());
        }*/
        Address address = addressMapper.toEntity(dto, country, region, city, createdBy);
        addressRepository.persist(address);
        log.info("Address created with id: {}", address.getId());
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID id, UpdateAddressRequest dto, String updatedBy) {
        log.info("Updating address with id: {}", id);
        Address address = getAddressById(id);
        // Validation des entités géographiques
        Country country = resolveCountry(dto.getCountryId());
        Region region = resolveRegion(dto.getRegionId());
        City city = resolveCity(dto.getCityId());
        // Validation cohérence géographique
        validateGeographicalConsistency(country, region, city);
        // Validation des types d'adresse
        validateAddressTypes(dto);

        // Mise à jour de l'entité
        addressMapper.updateEntity(address, dto, country, region, city, updatedBy);
        addressRepository.persist(address);

        log.info("Address updated successfully");
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional
    public Address getAddressById(UUID id) {
        log.debug("Fetching address with id: {}", id);
        return addressRepository.findByIdOptional(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
    }

    @Override
    @Transactional
    public List<AddressResponse> getAllAddresses() {
        log.debug("Fetching all addresses");
        return addressRepository.findAll().stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAddress(UUID id) {
        log.info("Deleting address with id: {}", id);

        // Vérification existence
        Address address = getAddressById(id);

        // Validation des contraintes métier
        validateDeletionConstraints(id);

        // Suppression
        addressRepository.delete(address);
        log.info("Address deleted successfully");
    }

    // ==================== MÉTHODES DE VALIDATION ====================
    private Country resolveCountry(UUID countryId) {
        return countryRepository.findByIdOptional(countryId)
                .orElseThrow(() -> new AddressCountryNotFoundException(countryId));
    }
    private Region resolveRegion(UUID regionId) {
        return regionRepository.findByIdOptional(regionId)
                .orElseThrow(() -> new AddressRegionNotFoundException(regionId));
    }
    private City resolveCity(UUID cityId) {
        return cityRepository.findByIdOptional(cityId)
                .orElseThrow(() -> new AddressCityNotFoundException(cityId));
    }

    /**
     * Valide la cohérence géographique complète
     */
    private void validateGeographicalConsistency(Country country, Region region, City city) {
        // Vérifier que la région appartient au pays
        if (!region.getCountry().getId().equals(country.getId())) {
            throw AddressGeographicalMismatchException.regionCountryMismatch(region.getId(), country.getId());
        }

        // Vérifier que la ville appartient à la région ET au pays
        if (!city.getRegion().getId().equals(region.getId())) {
            throw AddressGeographicalMismatchException.cityRegionMismatch(city.getId(), region.getId());
        }

        if (!city.getCountry().getId().equals(country.getId())) {
            throw AddressGeographicalMismatchException.regionCountryMismatch(city.getRegion().getId(), country.getId());
        }
    }

    /**
     * Valide qu'au moins un type d'adresse est sélectionné
     */
    private void validateAddressTypes(Object dto) {
        Boolean shipping = null, billing = null, other = null;

        if (dto instanceof CreateAddressRequest) {
            CreateAddressRequest request = (CreateAddressRequest) dto;
            shipping = request.getShippingAddress();
            billing = request.getBillingAddress();
            other = request.getOtherAddresses();
        } else if (dto instanceof UpdateAddressRequest) {
            UpdateAddressRequest request = (UpdateAddressRequest) dto;
            shipping = request.getShippingAddress();
            billing = request.getBillingAddress();
            other = request.getOtherAddresses();
        }

        if (!Boolean.TRUE.equals(shipping) && !Boolean.TRUE.equals(billing) && !Boolean.TRUE.equals(other)) {
            throw new InvalidAddressTypeException();
        }
    }

    /**
     * Valide les contraintes avant suppression
     */
    private void validateDeletionConstraints(UUID addressId) {
        if (!addressRepository.canBeDeleted(addressId)) {
            throw new AddressInUseException(addressId);
        }
    }
}