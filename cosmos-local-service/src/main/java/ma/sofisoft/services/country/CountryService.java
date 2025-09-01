package ma.sofisoft.services.country;

import ma.sofisoft.dtos.country.CountryResponse;
import ma.sofisoft.dtos.country.CreateCountryRequest;
import ma.sofisoft.dtos.country.UpdateCountryRequest;
import ma.sofisoft.entities.Country;

import java.util.List;
import java.util.UUID;

public interface CountryService {

    CountryResponse createCountry(CreateCountryRequest dto, String createdBy);

    CountryResponse updateCountry(UUID id, UpdateCountryRequest dto, String updatedBy);

    Country getCountryById(UUID id);

    List<CountryResponse> getAllCountries();

    void deleteCountry(UUID id);
}

