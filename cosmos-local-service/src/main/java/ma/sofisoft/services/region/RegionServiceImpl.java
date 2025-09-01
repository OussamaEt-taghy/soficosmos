package ma.sofisoft.services.region;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.region.RegionResponse;
import ma.sofisoft.dtos.region.CreateRegionRequest;
import ma.sofisoft.dtos.region.UpdateRegionRequest;
import ma.sofisoft.entities.Country;
import ma.sofisoft.entities.Region;
import ma.sofisoft.exceptions.country.CountryNameAlreadyExistsException;
import ma.sofisoft.exceptions.region.*;
import ma.sofisoft.mappers.RegionMapper;
import ma.sofisoft.repositories.CountryRepository;
import ma.sofisoft.repositories.RegionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final RegionMapper regionMapper;

    @Inject
    public RegionServiceImpl(RegionRepository regionRepository,
                             CountryRepository countryRepository,
                             RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.regionMapper = regionMapper;
    }

    @Override
    @Transactional
    public RegionResponse createRegion(CreateRegionRequest dto, String createdBy) {
        log.info("Creating new region with code: {} in country: {}", dto.getCode(), dto.getCountryId());
        Country country = resolveCountry(dto.getCountryId());
        if (regionRepository.existsByCodeAndCountryId(dto.getCode(), dto.getCountryId())) {
            throw new RegionCodeAlreadyExistsException(dto.getCode(), dto.getCountryId());
        }
        if (regionRepository.existsByName(dto.getName())) {
            throw new CountryNameAlreadyExistsException(dto.getName());
        }
        Region region = regionMapper.toEntity(dto, country, createdBy);
        regionRepository.persist(region);
        log.info("Region created with id: {}", region.getId());
        return regionMapper.toResponse(region);
    }

    @Override
    @Transactional
    public RegionResponse updateRegion(UUID id, UpdateRegionRequest dto, String updatedBy) {
        log.info("Updating region with id: {}", id);
        Region region = getRegionById(id);
        Country country = resolveCountry(dto.getCountryId());
        validateCodeUniqueness(dto.getCode(), region.getCode(), id, dto.getCountryId());
        validateNameUniqueness(dto.getName(), country.getName(), id);
        regionMapper.updateEntity(region, dto, country, updatedBy);
        regionRepository.persist(region);
        log.info("Region updated successfully");
        return regionMapper.toResponse(region);
    }

    @Override
    @Transactional
    public Region getRegionById(UUID id) {
        log.debug("Fetching region with id: {}", id);
        return regionRepository.findByIdOptional(id)
                .orElseThrow(() -> new RegionNotFoundException(id));
    }

    @Override
    @Transactional
    public List<RegionResponse> getAllRegions() {
        log.debug("Fetching all regions");
        return regionRepository.findAll().stream()
                .map(regionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRegion(UUID id) {
        log.info("Deleting region with id: {}", id);
        Region region = getRegionById(id);
        validateDeletionConstraints(id);
        regionRepository.delete(region);
        log.info("Region '{}' deleted successfully", region.getCode());
    }
    // ==================== MÉTHODES DE VALIDATION ====================

    private Country resolveCountry(UUID countryId) {
        return countryRepository.findByIdOptional(countryId)
                .orElseThrow(() -> new RegionCountryNotFoundException(countryId));
    }
    private void validateCodeUniqueness(String newCode, String currentCode, UUID regionId, UUID countryId) {
        if (newCode != null && !newCode.equals(currentCode)) {
            if (regionRepository.existsByCodeAndCountryIdAndIdNot(newCode, countryId, regionId)) {
                throw new RegionCodeAlreadyExistsException(newCode, countryId);
            }
        }
    }
    private void validateNameUniqueness(String newName, String currentName, UUID countryId) {
        if (newName != null && !newName.equals(currentName)) {
            if (regionRepository.existsByNameAndIdNot(newName, countryId)) {
                throw new RegionNameAlreadyExistsException(newName);
            }
        }
    }
    /**
     * Valide les contraintes avant suppression
     */
    private void validateDeletionConstraints(UUID regionId) {
        if (!regionRepository.canBeDeleted(regionId)) {
            throw new RegionInUseException(regionId);
        }
    }
}