package ma.sofisoft.services.remoteServiceChecker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.clients.TierServiceClient;
import ma.sofisoft.enums.OwnerType;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AddressUsageChecker {

    @Inject
    TierServiceClient tierServiceClient;

    public boolean isInUse(OwnerType ownerType, UUID ownerId, UUID addressId) {
        return switch (ownerType) {
            case TIER -> checkTierUsage(addressId);  //  Juste addressId
            //case COMPANY -> checkCompanyUsage(addressId);
            default -> false;
        };
    }

    private boolean checkTierUsage(UUID addressId) {
        try {
            return tierServiceClient.isAddressInUse(addressId);  //  GraphQL call
        } catch (Exception e) {
            log.warn("Cannot verify address usage: {}", e.getMessage());
            return true;
        }
    }
}
