package ma.sofisoft.mappers;

import ma.sofisoft.entities.Address;
import ma.sofisoft.entities.Country;
import ma.sofisoft.entities.Region;
import ma.sofisoft.entities.City;
import ma.sofisoft.dtos.address.AddressResponse;
import ma.sofisoft.dtos.address.CreateAddressRequest;
import ma.sofisoft.dtos.address.UpdateAddressRequest;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = {LocalDateTime.class, UUID.class})
public interface AddressMapper {

    // --- Entity ➜ ResponseDTO ---
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "cityId", source = "city.id")
    AddressResponse toResponse(Address entity);

    // --- CreateAddressRequest ➜ Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Address toEntity(CreateAddressRequest dto, Country country, Region region, City city, String createdBy);

    // --- UpdateAddressRequest ➜ Mise à jour de l'entité existante ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Address entity, UpdateAddressRequest dto, Country country, Region region, City city, String updatedBy);


}

