package ma.sofisoft.services.country;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.country.CountryResponse;
import ma.sofisoft.dtos.country.CreateCountryRequest;
import ma.sofisoft.dtos.country.UpdateCountryRequest;
import ma.sofisoft.entities.Country;
import ma.sofisoft.exceptions.country.CountryCodeAlreadyExistsException;
import ma.sofisoft.exceptions.country.CountryInUseException;
import ma.sofisoft.exceptions.country.CountryNameAlreadyExistsException;
import ma.sofisoft.exceptions.country.CountryNotFoundException;
import ma.sofisoft.mappers.CountryMapper;
import ma.sofisoft.repositories.CountryRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Inject
    public CountryServiceImpl(CountryRepository countryRepository,
                              CountryMapper countryMapper) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    @Override
    @Transactional
    public CountryResponse createCountry(CreateCountryRequest dto, String createdBy) {
        log.info("Creating new country with code: {}", dto.getCode());
        if (countryRepository.existsByCode(dto.getCode())) {
            throw new CountryCodeAlreadyExistsException(dto.getCode());
        }
        if (countryRepository.existsByName(dto.getName())) {
            throw new CountryNameAlreadyExistsException(dto.getName());
        }
        Country country = countryMapper.toEntity(dto, createdBy);
        countryRepository.persist(country);
        log.info("Country created with id: {}", country.getId());
        return countryMapper.toResponse(country);
    }

    @Override
    @Transactional
    public CountryResponse updateCountry(UUID id, UpdateCountryRequest dto, String updatedBy) {
        log.info("Updating country with id: {}", id);
        Country country = getCountryById(id);
        validateCodeUniqueness(dto.getCode(), country.getCode(), id);
        validateNameUniqueness(dto.getName(), country.getName(), id);
        countryMapper.updateEntity(country, dto, updatedBy);
        countryRepository.persist(country);
        log.info("Country updated successfully");
        return countryMapper.toResponse(country);
    }

    @Override
    @Transactional
    public Country getCountryById(UUID id) {
        log.debug("Fetching country with id: {}", id);
        return countryRepository.findByIdOptional(id)
                .orElseThrow(() -> new CountryNotFoundException(id));
    }

    @Override
    @Transactional
    public List<CountryResponse> getAllCountries() {
        log.debug("Fetching all countries");
        return countryRepository.findAll().stream()
                .map(countryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCountry(UUID id) {
        log.info("Deleting country with id: {}", id);
        Country country = getCountryById(id);
        validateDeletionConstraints(id);
        countryRepository.delete(country);
        log.info("Country '{}' deleted successfully", country.getCode());
    }

    // ==================== MÉTHODES DE VALIDATION ====================
    private void validateCodeUniqueness(String newCode, String currentCode, UUID countryId) {
        if (newCode != null && !newCode.equals(currentCode)) {
            if (countryRepository.existsByCodeAndIdNot(newCode, countryId)) {
                throw new CountryCodeAlreadyExistsException(newCode);
            }
        }
    }
    private void validateNameUniqueness(String newName, String currentName, UUID countryId) {
        if (newName != null && !newName.equals(currentName)) {
            if (countryRepository.existsByNameAndIdNot(newName, countryId)) {
                throw new CountryNameAlreadyExistsException(newName);
            }
        }
    }

    private void validateDeletionConstraints(UUID countryId) {
        if (!countryRepository.canBeDeleted(countryId)) {
            throw new CountryInUseException(countryId);
        }
    }
}
