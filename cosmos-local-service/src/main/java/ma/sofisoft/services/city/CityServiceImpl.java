package ma.sofisoft.services.city;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.city.CityResponse;
import ma.sofisoft.dtos.city.CreateCityRequest;
import ma.sofisoft.dtos.city.UpdateCityRequest;
import ma.sofisoft.entities.City;
import ma.sofisoft.entities.Country;
import ma.sofisoft.entities.Region;
import ma.sofisoft.exceptions.city.*;
import ma.sofisoft.exceptions.country.CountryNameAlreadyExistsException;
import ma.sofisoft.mappers.CityMapper;
import ma.sofisoft.repositories.CityRepository;
import ma.sofisoft.repositories.CountryRepository;
import ma.sofisoft.repositories.RegionRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityMapper cityMapper;

    @Inject
    public CityServiceImpl(CityRepository cityRepository,
                           CountryRepository countryRepository,
                           RegionRepository regionRepository,
                           CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.regionRepository = regionRepository;
        this.cityMapper = cityMapper;
    }

    @Override
    @Transactional
    public CityResponse createCity(CreateCityRequest dto, String createdBy) {
        log.info("Creating new city with code: {} in country: {} and region: {}",
                dto.getCode(), dto.getCountryId(), dto.getRegionId());
        Country country = resolveCountry(dto.getCountryId());
        Region region = resolveRegion(dto.getRegionId());
        validateGeographicalConsistency(region, country);
        if (cityRepository.existsByCodeAndCountryIdAndRegionId(dto.getCode(), dto.getCountryId(), dto.getRegionId())) {
            throw new CityCodeAlreadyExistsException(dto.getCode(), dto.getCountryId(), dto.getRegionId());
        }
        if (cityRepository.existsByName(dto.getName())) {
            throw new CountryNameAlreadyExistsException(dto.getName());
        }
        City city = cityMapper.toEntity(dto, country, region, createdBy);
        cityRepository.persist(city);
        log.info("City created with id: {}", city.getId());
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public CityResponse updateCity(UUID id, UpdateCityRequest dto, String updatedBy) {
        log.info("Updating city with id: {}", id);
        City city = getCityById(id);
        Country country = resolveCountry(dto.getCountryId());
        Region region = resolveRegion(dto.getRegionId());
        validateGeographicalConsistency(region, country);
        validateCodeUniqueness(dto.getCode(), city.getCode(), id, dto.getCountryId(), dto.getRegionId());
        validateNameUniqueness(dto.getName(), country.getName(), id);
        cityMapper.updateEntity(city, dto, country, region, updatedBy);
        cityRepository.persist(city);
        log.info("City updated successfully");
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public City getCityById(UUID id) {
        log.debug("Fetching city with id: {}", id);
        return cityRepository.findByIdOptional(id)
                .orElseThrow(() -> new CityNotFoundException(id));
    }

    @Override
    @Transactional
    public List<CityResponse> getAllCities() {
        log.debug("Fetching all cities");
        return cityRepository.findAll().stream()
                .map(cityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCity(UUID id) {
        log.info("Deleting city with id: {}", id);
        City city = getCityById(id);
        validateDeletionConstraints(id);
        cityRepository.delete(city);
        log.info("City '{}' deleted successfully", city.getCode());
    }

    // ==================== MÉTHODES DE VALIDATION ====================
    private Country resolveCountry(UUID countryId) {
        return countryRepository.findByIdOptional(countryId)
                .orElseThrow(() -> new CityCountryNotFoundException(countryId));
    }
    private Region resolveRegion(UUID regionId) {
        return regionRepository.findByIdOptional(regionId)
                .orElseThrow(() -> new CityRegionNotFoundException(regionId));
    }

    /**
     * Valide la cohérence géographique : la région doit appartenir au pays
     */
    private void validateGeographicalConsistency(Region region, Country country) {
        if (!region.getCountry().getId().equals(country.getId())) {
            throw new CityRegionCountryMismatchException(region.getId(), country.getId());
        }
    }

    /**
     * Valide l'unicité du code dans le pays + région lors de la mise à jour
     */
    private void validateCodeUniqueness(String newCode, String currentCode, UUID cityId, UUID countryId, UUID regionId) {
        if (newCode != null && !newCode.equals(currentCode)) {
            if (cityRepository.existsByCodeAndCountryIdAndRegionIdAndIdNot(newCode, countryId, regionId, cityId)) {
                throw new CityCodeAlreadyExistsException(newCode, countryId, regionId);
            }
        }
    }
    private void validateNameUniqueness(String newName, String currentName, UUID countryId) {
        if (newName != null && !newName.equals(currentName)) {
            if (cityRepository.existsByNameAndIdNot(newName, countryId)) {
                throw new CityNameAlreadyExistsException(newName);
            }
        }
    }

    /**
     * Valide les contraintes avant suppression
     */
    private void validateDeletionConstraints(UUID cityId) {
        if (!cityRepository.canBeDeleted(cityId)) {
            throw new CityInUseException(cityId);
        }
    }
}