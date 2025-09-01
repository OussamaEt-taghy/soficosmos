package ma.sofisoft.mappers;

import ma.sofisoft.entities.Region;
import ma.sofisoft.entities.Country;
import ma.sofisoft.dtos.region.RegionResponse;
import ma.sofisoft.dtos.region.CreateRegionRequest;
import ma.sofisoft.dtos.region.UpdateRegionRequest;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = {LocalDateTime.class, UUID.class})
public interface RegionMapper {

    // --- Entity ➜ ResponseDTO ---
    @Mapping(target = "countryId", source = "country.id")
    RegionResponse toResponse(Region entity);

    // --- CreateRegionRequest ➜ Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cities", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Region toEntity(CreateRegionRequest dto, Country country, String createdBy);

    // --- UpdateRegionRequest ➜ Update Entity ---
    // Manque sur updateEntity :
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cities", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Region entity, UpdateRegionRequest dto, Country country, String updatedBy);
}
