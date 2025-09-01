package ma.sofisoft.services.city;

import ma.sofisoft.dtos.city.CityResponse;
import ma.sofisoft.dtos.city.CreateCityRequest;
import ma.sofisoft.dtos.city.UpdateCityRequest;
import ma.sofisoft.entities.City;

import java.util.List;
import java.util.UUID;

public interface CityService {

    CityResponse createCity(CreateCityRequest dto, String createdBy);

    CityResponse updateCity(UUID id, UpdateCityRequest dto, String updatedBy);

    City getCityById(UUID id);

    List<CityResponse> getAllCities();

    void deleteCity(UUID id);
}