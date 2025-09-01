package ma.sofisoft.clients;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import java.util.UUID;

@GraphQLClientApi(configKey = "tier-service")
public interface TierServiceClient {
    @Query("isAddressInUse")
    boolean isAddressInUse(@Name("addressId") UUID addressId);
}
