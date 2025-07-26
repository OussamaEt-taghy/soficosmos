package it.oussama.mapper;
import jakarta.persistence.EntityManager;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.representations.AccessToken;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import java.sql.*;
import java.util.*;

public class AutorisationProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper {

    public static final String PROVIDER_ID = "custom-autorisation-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName("iam.db.url");
        property.setLabel("IAM Database URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("jdbc:postgresql://iam-db:5432/cosmos_iam_db");
        property.setHelpText("URL de la base de données IAM ");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName("iam.db.username");
        property.setLabel("IAM Database Username");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("postgres");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName("iam.db.password");
        property.setLabel("IAM Database Password");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setDefaultValue("Eghy@@2002");
        configProperties.add(property);
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Cosmos Autorisation Mapper";
    }

    @Override
    public String getHelpText() {
        return "Add authorizations (permissions linked to roles) to the token.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token,
                                            ProtocolMapperModel mappingModel,
                                            KeycloakSession session,
                                            UserSessionModel userSession,
                                            ClientSessionContext clientSessionCtx) {
        try {
            // Récupération du nom de l'organisation à partir du domaine (host HTTP)
            OrganizationData organizationData = getUserOrganization(session, userSession);
            System.out.println("🏢 Organization détectée: " + organizationData.getName() + " (ID: " + organizationData.getId() + ")");
            // Récupération des rôles de l'utilisateur connecté
            Set<String> keycloakRoleIds = extractKeycloakRoleIds(userSession);
            System.out.println("🔑 Rôles utilisateur: " + keycloakRoleIds);
           // Si aucun rôle n'est associé, on retourne un token avec des claims vides
            if (keycloakRoleIds.isEmpty()) {
                System.out.println("⚠️ Aucun rôle trouvé pour l'utilisateur");
                addEmptyClaimsToToken(token, organizationData);
                return token;
            }
            // Récupération des autorisations à partir de la base IAM
            AuthorizationData authData = getAuthorizationsFromDB(keycloakRoleIds, organizationData, mappingModel);
            System.out.println("✅ Permissions récupérées: " + authData.getPermissions());
            System.out.println("📦 Groupes de permissions: " + authData.getPermissionGroups());
            // Ajout des claims dans le token JWT
            addClaimsToToken(token, organizationData, authData);
        } catch (Exception e) {
            // En cas d’erreur globale, on log et ajoute des claims par défaut
            System.err.println("❌ Erreur lors de la récupération des autorisations: " + e.getMessage());
            e.printStackTrace();
            addErrorClaimsToToken(token, new OrganizationData("default", "default-id"));
        }
        return token;
    }

    // Extrait l'organisation à partir du nom de domaine (host HTTP)
    private OrganizationData getUserOrganization(KeycloakSession session, UserSessionModel userSession) {
        try {
            String host = session.getContext().getUri().getRequestUri().getHost();
            System.out.println("🌐 Host de la requête: " + host);
            // Déduire le nom de l'organisation à partir du host
            String orgName = extractOrganizationFromHost(host);
            String orgId = getOrganizationIdFromKeycloak(session, orgName);
            System.out.println("🏢 Organization déduite: " + orgName + " (ID: " + orgId + ")");
            return new OrganizationData(orgName, orgId);
        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
            throw new RuntimeException("Organisation non trouvée : " + e.getMessage());
        }
    }

    private String getOrganizationIdFromKeycloak(KeycloakSession session, String orgName) {
        try {
            String realmId = session.getContext().getRealm().getId();
            EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
            String sql = "SELECT o.id FROM OrganizationEntity o WHERE o.realmId = :realmId AND o.name = :name";
            String orgId = em.createQuery(sql, String.class)
                    .setParameter("realmId", realmId)
                    .setParameter("name", orgName)
                    .getSingleResult();
            return orgId;
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération ID: " + e.getMessage());
            return orgName.toLowerCase();
        }
    }

    // Découpe le nom d’hôte pour extraire le nom de l’organisation (ex: www.company1.cosmos.ma → company1)
    private String extractOrganizationFromHost(String host) {
        if (host == null || !host.contains(".")) return "default";
        String[] parts = host.split("\\.");
        if (parts.length < 3) return "default";
        return parts[1];
    }

    // Récupère les IDs des rôles associés à l'utilisateur dans Keycloak
    private Set<String> extractKeycloakRoleIds(UserSessionModel userSession) {
        Set<String> roleIds = new HashSet<>();
        UserModel user = userSession.getUser();
        user.getRoleMappingsStream().forEach(role -> {
            roleIds.add(role.getId());
            System.out.println("🔑 Rôle utilisateur: " + role.getName() + " (ID: " + role.getId() + ")");
        });
        System.out.println("🎯 Total rôles utilisateur: " + roleIds.size());
        return roleIds;
    }
    // Interroge la base de données IAM pour récupérer les permissions liées aux rôles
    private AuthorizationData getAuthorizationsFromDB(Set<String> keycloakRoleIds,
                                                      OrganizationData organizationData,
                                                      ProtocolMapperModel mappingModel) {
        String dbUrl = mappingModel.getConfig().get("iam.db.url");
        String dbUsername = mappingModel.getConfig().get("iam.db.username");
        String dbPassword = mappingModel.getConfig().get("iam.db.password");
        AuthorizationData result = new AuthorizationData();
        if (keycloakRoleIds.isEmpty()) return result;
        String placeholders = String.join(",", Collections.nCopies(keycloakRoleIds.size(), "?"));
        String schema = organizationData.getName();
        // Construction dynamique de la requête SQL (avec schéma multi-tenant)
        String sql = String.format("""
           SELECT DISTINCT
           p.name as permission_name,
           pg.name as group_name
           FROM %s.autorisation a
           JOIN %s.permission p ON a.idpermission = p.id
           LEFT JOIN %s.permission_group pg ON p.groupid = pg.id
           WHERE a.idrole IN (%s)
   """, schema, schema, schema, placeholders);

        System.out.println("🏢 Organization: " + organizationData.getName() + " → Schéma: " + schema);
        System.out.println("🗄️ SQL Query: " + sql);
        System.out.println("🔍 Role IDs: " + keycloakRoleIds);

        try (Connection connection = createConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement stmt = connection.prepareStatement(sql)) {
 // On utilise le nom de l’organisation comme nom de schéma
            int paramIndex = 1;
            for (String roleId : keycloakRoleIds) {
                stmt.setString(paramIndex++, roleId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String permissionName = rs.getString("permission_name");
                    String groupName = rs.getString("group_name");
                    if (permissionName != null) result.getPermissions().add(permissionName);
                    if (groupName != null) result.getPermissionGroups().add(groupName);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL pour organization '" + organizationData.getName() + "': " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des autorisations pour l'organization : " + organizationData.getName(), e);
        }
        return result;
    }
    // Ajoute les données personnalisées (organization, autorisation...) dans le token
            private void addClaimsToToken(AccessToken token, OrganizationData organizationData, AuthorizationData authData) {
                token.getOtherClaims().put("organization", organizationData.getName());
                token.getOtherClaims().put("autorisation", new ArrayList<>(authData.getPermissions()));
                token.getOtherClaims().put("permission_groups", new ArrayList<>(authData.getPermissionGroups()));
                System.out.println("✅ Claims ajoutés au token:");
                System.out.println("   - organization: " + organizationData.getName());
                System.out.println("   - autorisation: " + authData.getPermissions());
                System.out.println("   - permission_groups: " + authData.getPermissionGroups());
            }

            private void addEmptyClaimsToToken(AccessToken token, OrganizationData organizationData) {
                token.getOtherClaims().put("organization", organizationData.getName());
                token.getOtherClaims().put("autorisation", Collections.emptyList());
                token.getOtherClaims().put("permission_groups", Collections.emptyList());
            }

            private void addErrorClaimsToToken(AccessToken token, OrganizationData organizationData) {
                token.getOtherClaims().put("organization", organizationData.getName());
                token.getOtherClaims().put("autorisation", Collections.emptyList());
                token.getOtherClaims().put("permission_groups", Collections.emptyList());
            }

            private Connection createConnection(String dbUrl, String username, String password) throws SQLException {
                try {
                    Class.forName("org.postgresql.Driver");
                    Properties props = new Properties();
                    props.setProperty("user", username);
                    props.setProperty("password", password);
                    props.setProperty("ssl", "false");
                    props.setProperty("connectTimeout", "5");
                    props.setProperty("socketTimeout", "30");
                    return DriverManager.getConnection(dbUrl, props);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Driver PostgreSQL non trouvé", e);
                }
            }

            @Override
            public String getId() {
                return PROVIDER_ID;
            }
        }
