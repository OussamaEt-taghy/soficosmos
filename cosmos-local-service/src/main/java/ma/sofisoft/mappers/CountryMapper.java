package ma.sofisoft.mappers;

import ma.sofisoft.entities.Country;
import ma.sofisoft.dtos.country.CountryResponse;
import ma.sofisoft.dtos.country.CreateCountryRequest;
import ma.sofisoft.dtos.country.UpdateCountryRequest;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = {LocalDateTime.class, UUID.class})
public interface CountryMapper {

    // Entité ➜ ResponseDTO pour la lecture
    CountryResponse toResponse(Country entity);

    // CreateCountryRequest ➜ Entité pour la création
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "regions", ignore = true)
    @Mapping(target = "cities", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Country toEntity(CreateCountryRequest dto, String createdBy);

    // UpdateCountryRequest ➜ Mise à jour de l'entité existante
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "regions", ignore = true)
    @Mapping(target = "cities", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Country entity, UpdateCountryRequest dto, String updatedBy);
}
