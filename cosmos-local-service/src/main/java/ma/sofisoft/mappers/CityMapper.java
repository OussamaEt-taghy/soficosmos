package ma.sofisoft.mappers;

import ma.sofisoft.entities.City;
import ma.sofisoft.entities.Country;
import ma.sofisoft.entities.Region;
import ma.sofisoft.dtos.city.CityResponse;
import ma.sofisoft.dtos.city.CreateCityRequest;
import ma.sofisoft.dtos.city.UpdateCityRequest;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = {LocalDateTime.class, UUID.class})
public interface CityMapper {

    // --- Entity ➜ ResponseDTO ---
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "regionId", source = "region.id")
    CityResponse toResponse(City entity);

    // --- CreateCityRequest ➜ Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "country", source = "country")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    City toEntity(CreateCityRequest dto, Country country, Region region, String createdBy);

    // --- UpdateCityRequest ➜ Update Entity ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget City entity, UpdateCityRequest dto, Country country, Region region, String updatedBy);

}
