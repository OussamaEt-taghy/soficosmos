package ma.sofisoft.dtos.graphqlDtos;
import org.eclipse.microprofile.graphql.Type;


@Type // Permet à SmallRye de l'exposer dans le schéma GraphQL
public class AddressUsageResult {
    public boolean usedByTier;
    // par la suite en ajoute company
    // Résumé global : true si utilisée par un ou plusieurs services
    public boolean isGloballyUsed() {
        return usedByTier;
    }
}

