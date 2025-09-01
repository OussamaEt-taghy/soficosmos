package ma.sofisoft.services.region;

import ma.sofisoft.dtos.region.RegionResponse;
import ma.sofisoft.dtos.region.CreateRegionRequest;
import ma.sofisoft.dtos.region.UpdateRegionRequest;
import ma.sofisoft.entities.Region;

import java.util.List;
import java.util.UUID;

public interface RegionService {

    RegionResponse createRegion(CreateRegionRequest dto, String createdBy);

    RegionResponse updateRegion(UUID id, UpdateRegionRequest dto, String updatedBy);

    Region getRegionById(UUID id);

    List<RegionResponse> getAllRegions();

    void deleteRegion(UUID id);
}
