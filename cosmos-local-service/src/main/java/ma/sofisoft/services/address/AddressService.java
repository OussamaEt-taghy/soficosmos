package ma.sofisoft.services.address;

import ma.sofisoft.dtos.address.AddressResponse;
import ma.sofisoft.dtos.address.CreateAddressRequest;
import ma.sofisoft.dtos.address.UpdateAddressRequest;
import ma.sofisoft.entities.Address;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponse createAddress(CreateAddressRequest dto, String createdBy);

    AddressResponse updateAddress(UUID id, UpdateAddressRequest dto, String updatedBy);

    Address getAddressById(UUID id);

    List<AddressResponse> getAllAddresses();

    void deleteAddress(UUID id);
}
