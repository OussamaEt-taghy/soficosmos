package ma.sofisoft.services;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.*;
import ma.sofisoft.dtos.userGroup.RemoveRoleFromKeycloakGroupRequest;
import ma.sofisoft.dtos.roles.*;
import ma.sofisoft.dtos.users.CreateUserRequest;
import ma.sofisoft.dtos.users.UpdateUserRequest;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.exceptions.roles.*;
import ma.sofisoft.exceptions.userGroups.*;
import ma.sofisoft.exceptions.organizations.UnauthorizedOrganizationAccessException;
import ma.sofisoft.exceptions.users.*;
import ma.sofisoft.mappers.PermissionMapper;
import ma.sofisoft.mappers.UserMapper;
import ma.sofisoft.repositories.AutorisationRepository;
import ma.sofisoft.repositories.PermissionRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class KeycloakService {
    private final UserMapper userMapper;
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AutorisationRepository autorisationRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    public KeycloakService(UserMapper userMapper, PermissionRepository permissionRepository, PermissionMapper permissionMapper, AutorisationRepository autorisationRepository) {
        this.userMapper = userMapper;
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.autorisationRepository = autorisationRepository;
    }

    // Keycloak serveur connexion
    @ConfigProperty(name = "cosmos.keycloak.adminClientId")
    String adminClientId;
    @ConfigProperty(name = "cosmos.keycloak.adminClientSecret")
    String adminClientSecret;
    @ConfigProperty(name = "cosmos.keycloak.authServerUrl")
    String authServerUrl;
    @ConfigProperty(name = "cosmos.keycloak.adminRealm")
    String adminRealm;
    @Getter
    @ConfigProperty(name = "cosmos.keycloak.realm")
    String realm;
    @Getter
    private Keycloak keycloak;

    //###########################################################################//
    @PostConstruct
    public void initKeycloak() {
        try {
            log.info("Initializing Keycloak client to manage realm '{}' using admin credentials from realm '{}'",
                    realm, adminRealm);
            log.info("Auth server URL: {}", authServerUrl);
            log.info("Admin client ID: {}", adminClientId);
            log.info("Admin realm: {}", adminRealm);
            log.info("Target realm to manage: {}", realm);
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(adminRealm)
                    .clientId(adminClientId)
                    .clientSecret(adminClientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Keycloak client", e);
        }
    }
    //###########################################################################//
    @PreDestroy
    public void closeKeycloak() {
        if (keycloak != null) {
            keycloak.close(); // Releases HTTP connections
            log.info("Keycloak client closed.");
        }
    }

    //###########################################################################//
    public Keycloak getKeycloakWithToken(String token) {
        log.info("Creating Keycloak instance with provided token for realm '{}'", realm);
        try {
            return KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(realm)
                    .authorization("Bearer " + token)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create Keycloak instance with token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Keycloak instance with token: " + e.getMessage(), e);
        }
    }

    //###########################################################################//
    //get Organization
    public String extractOrganizationName(String token) {
        try {
            JWSInput jwsInput = new JWSInput(token);
            AccessToken accessToken = jwsInput.readJsonContent(AccessToken.class);
            Object orgClaim = accessToken.getOtherClaims().get("organization");

            if (orgClaim == null) {
                throw new RuntimeException("The token does not contain the 'organization' claim");
            }
            return orgClaim.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while decoding the token", e);
        }
    }

    //###########################################################################//
    // get OrganizationByName
    public String getOrganizationIdByName(String orgName) {
        try {
            var organizationsResource = keycloak.realm(realm).organizations();
            var organizations = organizationsResource.search(orgName, true, 0, 10);
            var organization = organizations.stream()
                    .filter(org -> orgName.equals(org.getName()))
                    .findFirst()
                    .orElse(null);
            if (organization == null) {
                log.error("Organization '{}' not found", orgName);
                throw new RuntimeException("Unknown organization: " + orgName);
            }
            String orgId = organization.getId();
            log.info("Organization detected: {} (ID: {})", orgName, orgId);
            return orgId;

        } catch (Exception e) {
            log.error("Error retrieving organization '{}': {}", orgName, e.getMessage());
            throw new RuntimeException("Error retrieving organization.", e);
        }
    }
    //################ Manage groupes ##################//
// create group
    public String createGroup(String token, CreateKeycloakGroupRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            List<GroupRepresentation> existingGroups = keycloak.realm(realm).groups().groups();
            boolean alreadyExists = existingGroups.stream()
                    .filter(g -> {
                        Map<String, List<String>> attrs = g.getAttributes();
                        return attrs != null && attrs.containsKey("organization-id") &&
                                attrs.get("organization-id").contains(orgId);
                    })
                    .anyMatch(g -> g.getName().equalsIgnoreCase(request.getGroupName()));
            if (alreadyExists) {
                log.warn("Group '{}' already exists for organization '{}'", request.getGroupName(), orgName);
                throw new GroupAlreadyExistsException(request.getGroupName());
            }
        } catch (GroupAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Organization check warning: {}", e.getMessage());
        }
        GroupRepresentation group = new GroupRepresentation();
        group.setName(request.getGroupName());
        Map<String, List<String>> attrs = new HashMap<>();
        attrs.put("organization-id", List.of(orgId));
        attrs.put("organization-name", List.of(orgName));
        group.setAttributes(attrs);
        Response response = keycloak.realm(realm).groups().add(group);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            log.error(" Failed to create group '{}'. HTTP status code: {}", request.getGroupName(), response.getStatus());
            throw new GroupCreationException(request.getGroupName(), response.getStatus());
        }
        String groupId = CreatedResponseUtil.getCreatedId(response);
        log.info(" Group '{}' successfully created for organization '{}' (ID: {})",
                request.getGroupName(), orgName, orgId);
        return groupId;
    }

    // Delete group
    public void deleteGroup(String token, String groupId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        GroupRepresentation group = keycloak.realm(realm).groups().group(groupId).toRepresentation();
        if (group == null) {
            throw new GroupNotFoundException(groupId);
        }
        Map<String, List<String>> attrs = group.getAttributes();
        if (attrs == null || !attrs.containsKey("organization-id") ||
                !attrs.get("organization-id").contains(orgId)) {
            log.error("Unauthorized attempt to delete group '{}' by organization '{}'",
                    group.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm).groups().group(groupId).remove();
            log.info("Group '{}' successfully deleted by organization '{}' (ID: {})",
                    group.getName(), orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to delete group '{}': {}", group.getName(), e.getMessage());
        }
    }

    // Add user to group
    public void addUserToGroup(String token, AddUserToKeycloakGroupRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(request.getUserId()));

            if (!userBelongsToOrg) {
                log.error("User '{}' does not belong to organization '{}'", request.getUserId(), orgName);
                throw new UnauthorizedOrganizationAccessException(orgName);
            }

        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Organization membership check failed, falling back to attributes: {}", e.getMessage());
            UserRepresentation user;
            try {
                user = keycloak.realm(realm).users().get(request.getUserId()).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException(request.getUserId());
                }
            } catch (Exception ex) {
                log.error("User not found: {}", request.getUserId());
                throw new UserNotFoundException(request.getUserId());
            }
            Map<String, List<String>> userAttrs = user.getAttributes();
            if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                    !userAttrs.get("organization-id").contains(orgId)) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        }
        GroupRepresentation group = keycloak.realm(realm).groups().group(request.getGroupId()).toRepresentation();
        if (group == null) {
            log.error("Group not found: {}", request.getGroupId());
            throw new GroupNotFoundException(request.getGroupId());
        }
        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            log.error("Group '{}' does not belong to organization '{}'", group.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm).users().get(request.getUserId()).joinGroup(request.getGroupId());
            log.info("User '{}' added to group '{}' in organization '{}'",
                    request.getUserId(), group.getName(), orgName);
        } catch (Exception e) {
            throw new UserGroupAssignmentException(request.getUserId(), request.getGroupId());
        }
    }

    // Remove user from group
    public void removeUserFromGroup(String token, RemoveUserFromKeycloakGroupRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();

            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(request.getUserId()));
            if (!userBelongsToOrg) {
                log.error("User '{}' does not belong to organization '{}'", request.getUserId(), orgName);
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Organization membership check failed, falling back to attributes: {}", e.getMessage());
            UserRepresentation user;
            try {
                user = keycloak.realm(realm).users().get(request.getUserId()).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException(request.getUserId());
                }
            } catch (Exception ex) {
                throw new UserNotFoundException(request.getUserId());
            }
            Map<String, List<String>> userAttrs = user.getAttributes();
            if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                    !userAttrs.get("organization-id").contains(orgId)) {
                log.error("User '{}' does not belong to organization '{}'", user.getUsername(), orgName);
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        }
        GroupRepresentation group = keycloak.realm(realm).groups().group(request.getGroupId()).toRepresentation();
        if (group == null) {
            log.error("Group not found: {}", request.getGroupId());
            throw new GroupNotFoundException(request.getGroupId());
        }
        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm).users().get(request.getUserId()).leaveGroup(request.getGroupId());
            log.info("User '{}' removed from group '{}' in organization '{}'",
                    request.getUserId(), group.getName(), orgName);
        } catch (Exception e) {
            throw new UserGroupAssignmentException(request.getUserId(), request.getGroupId());
        }
    }

    // Get all groups
    public List<GroupResponse> getAllGroups(String token) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            List<GroupRepresentation> allGroups = keycloak.realm(realm).groups().groups();
            return allGroups.stream()
                    .filter(group -> {
                        Map<String, List<String>> attrs = group.getAttributes();
                        return attrs != null && attrs.containsKey("organization-id") &&
                                attrs.get("organization-id").contains(orgId);
                    })
                    .map(group -> {
                        List<String> roleNames = new ArrayList<>();
                        try {
                            List<RoleRepresentation> roles = keycloak.realm(realm)
                                    .groups().group(group.getId())
                                    .roles().realmLevel().listAll();
                            roleNames = roles.stream()
                                    .map(RoleRepresentation::getName)
                                    .toList();
                        } catch (Exception e) {
                            log.warn("Error retrieving roles for group '{}': {}", group.getName(), e.getMessage());
                        }
                        return GroupResponse.builder()
                                .id(group.getId())
                                .name(group.getName())
                                .roles(roleNames)
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            throw new GroupRetrievalException(orgName);
        }
    }

    // Get group members
    public List<UserResponse> getGroupMembers(String token, String groupId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        GroupRepresentation group = keycloak.realm(realm).groups().group(groupId).toRepresentation();
        if (group == null) {
            throw new GroupNotFoundException(groupId);
        }
        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var orgMembers = orgResource.members().getAll();
            Set<String> orgMemberIds = orgMembers.stream()
                    .map(UserRepresentation::getId)
                    .collect(Collectors.toSet());
            List<UserRepresentation> groupMembers = keycloak.realm(realm).groups().group(groupId).members();
            return groupMembers.stream()
                    .filter(user -> orgMemberIds.contains(user.getId()))
                    .map(user -> {
                        List<String> roles = keycloak.realm(realm)
                                .users().get(user.getId())
                                .roles().realmLevel().listEffective()
                                .stream().map(RoleRepresentation::getName)
                                .toList();
                        return userMapper.toUserResponse(user, roles);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Organization API failed, falling back to attributes: {}", e.getMessage());
            try {
                List<UserRepresentation> members = keycloak.realm(realm).groups().group(groupId).members();
                return members.stream()
                        .filter(user -> {
                            Map<String, List<String>> userAttrs = user.getAttributes();
                            return userAttrs != null && userAttrs.containsKey("organization-id") &&
                                    userAttrs.get("organization-id").contains(orgId);
                        })
                        .map(user -> {
                            List<String> roles = keycloak.realm(realm)
                                    .users().get(user.getId())
                                    .roles().realmLevel().listEffective()
                                    .stream().map(RoleRepresentation::getName)
                                    .toList();
                            return userMapper.toUserResponse(user, roles);
                        })
                        .toList();
            } catch (Exception ex) {
                log.error("Error retrieving members for group '{}': {}", group.getName(), ex.getMessage());
                throw new GroupMembersRetrievalException(groupId);
            }
        }
    }

    //################ Manage users ##################//
    // Create user
    public String createUser(String token, CreateUserRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userExistsInOrg = members.stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(request.getUsername()));
            if (userExistsInOrg) {
                log.warn("User '{}' already exists in organization '{}'", request.getUsername(), orgName);
                throw new UserAlreadyExistsException(request.getUsername());
            }
        } catch (Exception e) {
            log.debug("Organization check failed, proceeding with user creation: {}", e.getMessage());
        }
        UsersResource usersResource = keycloak.realm(realm).users();
        UserRepresentation user = userMapper.toUserRepresentation(request);
        user.setEnabled(request.isEnabled());
        Map<String, List<String>> attrs = new HashMap<>();
        attrs.put("organization-id", List.of(orgId));
        attrs.put("organization-name", List.of(orgName));
        user.setAttributes(attrs);
        Response response = usersResource.create(user);
        if (response.getStatus() != 201) {
            log.error("Failed to create user '{}'. HTTP status code: {}", request.getUsername(), response.getStatus());
            throw new UserCreationException(request.getUsername(), response.getStatus());
        }
        String userId = CreatedResponseUtil.getCreatedId(response);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            orgResource.members().addMember(userId);
            log.info("User '{}' added to organization '{}' as member", request.getUsername(), orgName);
        } catch (Exception e) {
            log.warn("Failed to add user to organization membership, but user created: {}", e.getMessage());
        }
        try {
            CredentialRepresentation password = new CredentialRepresentation();
            password.setType(CredentialRepresentation.PASSWORD);
            password.setTemporary(false);
            password.setValue(request.getPassword());
            usersResource.get(userId).resetPassword(password);
        } catch (Exception e) {
            log.error("Failed to set password for user '{}': {}", request.getUsername(), e.getMessage());
            throw new UserCreationException(request.getUsername(), 500);
        }
        log.info("User '{}' successfully created for organization '{}' (ID: {})", request.getUsername(), orgName, orgId);
        return userId;
    }

    // Update user info
    public void updateUserInfo(String token, String userId, UpdateUserRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        // Verify that the user belongs to the organization
        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                !userAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        try {
            userResource.update(user);
            log.info(" User '{}' updated in organization '{}' (ID: {})",
                    userId, orgName, orgId);
        } catch (Exception e) {
            log.error(" Failed to update user '{}': {}", userId, e.getMessage());
            throw new UserUpdateException(userId);
        }
    }

    // Update user password
    public void updateUserPassword(String token, String userId, String newPassword) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        // Verify that the user belongs to the organization
        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                !userAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setTemporary(false);
        password.setValue(newPassword);
        try {
            keycloak.realm(realm).users().get(userId).resetPassword(password);
        } catch (Exception e) {
            throw new UserPasswordUpdateException(userId);
        }
    }

    // Delete user
    public void deleteUser(String token, String userId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(userId));
            if (!userBelongsToOrg) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
            orgResource.members().member(userId).delete();
        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to remove user from organization, proceeding with deletion: {}", e.getMessage());
        }
        Response response = keycloak.realm(realm).users().delete(userId);
        int status = response.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode() &&
                status != Response.Status.OK.getStatusCode()) {
            throw new UserDeletionException(userId, status);
        }
        log.info("User '{}' deleted from organization '{}' (ID: {})", userId, orgName, orgId);
    }

    // Get all users
    public List<UserResponse> getUsers(String token) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            return members.stream()
                    .map(user -> {
                        List<String> roles = keycloak.realm(realm)
                                .users().get(user.getId())
                                .roles().realmLevel()
                                .listAll()
                                .stream()
                                .map(RoleRepresentation::getName)
                                .toList();
                        return userMapper.toUserResponse(user, roles);
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving users for organization '{}': {}", orgName, e.getMessage());
            throw new UserRetrievalException(orgName);
        }
    }
    // Get user by ID
    public UserResponse getUserById(String token, String userId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        // Verify that the user belongs to the organization
        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                !userAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        List<String> roles = keycloak.realm(realm)
                .users().get(userId)
                .roles().realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .toList();
        return userMapper.toUserResponse(user, roles);
    }

    // Activate/Deactivate a user
    public void enableUser(String token, String userId, boolean enabled) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        // Verify that the user belongs to the organization
        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                !userAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        user.setEnabled(enabled);
        try {
            keycloak.realm(realm).users().get(userId).update(user);
            log.info(" User '{}' {} in organization '{}' (ID: {})",
                    userId, enabled ? "activated" : "deactivated", orgName, orgId);
        } catch (Exception e) {
            log.error(" Failed to {} user '{}': {}",
                    enabled ? "activate" : "deactivate", userId, e.getMessage());
            throw new UserStatusUpdateException(userId, enabled);
        }
    }

    // get all group for user
    public List<GroupResponse> getUserGroups(String token, String userId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        //  Vérifier que l'utilisateur appartient à l'organization
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                !userAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException( orgName);
        }
        try {
            List<GroupRepresentation> groups = keycloak.realm(realm)
                    .users().get(userId).groups();
            return groups.stream()
                    .filter(group -> {
                        //  Filtrer seulement les groupes de cette organization
                        Map<String, List<String>> groupAttrs = group.getAttributes();
                        return groupAttrs != null && groupAttrs.containsKey("organization-id") &&
                                groupAttrs.get("organization-id").contains(orgId);
                    })
                    .map(group -> GroupResponse.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error(" Error retrieving user groups '{}': {}", userId, e.getMessage());
            throw new UserGroupsRetrievalException(userId);
        }
    }

    //################ Manage roles ##################//
    // Add role to user
    public void addRoleToUser(String token, String userId, String roleId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm).rolesById().getRole(roleId);
            if (role == null) {
                throw new RoleNotFoundException(roleId);
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", roleId);
            throw new RoleNotFoundException(roleId);
        }
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(userId));
            if (!userBelongsToOrg) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Organization API check failed, falling back to attributes: {}", e.getMessage());
            UserRepresentation user;
            try {
                user = keycloak.realm(realm).users().get(userId).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException(userId);
                }
            } catch (NotFoundException ex) {
                log.error("User not found: {}", userId);
                throw new UserNotFoundException(userId);
            }
            Map<String, List<String>> userAttrs = user.getAttributes();
            if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                    !userAttrs.get("organization-id").contains(orgId)) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        }
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm)
                    .users().get(userId)
                    .roles().realmLevel()
                    .add(List.of(role));
            log.info("Role '{}' (ID: {}) assigned to user '{}' in organization '{}' (ID: {})",
                    role.getName(), roleId, userId, orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to user '{}'", role.getName(), userId);
            throw new UserRoleAssignmentException(userId, roleId);
        }
    }

    // Remove role from user
    public void removeRoleFromUser(String token, RemoveUserRoleRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm).rolesById().getRole(request.getRoleId());
            if (role == null) {
                throw new RoleNotFoundException(request.getRoleId());
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", request.getRoleId());
            throw new RoleNotFoundException(request.getRoleId());
        }
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(request.getUserId()));
            if (!userBelongsToOrg) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Organization API check failed, falling back to attributes: {}", e.getMessage());
            UserRepresentation user;
            try {
                user = keycloak.realm(realm).users().get(request.getUserId()).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException(request.getUserId());
                }
            } catch (Exception ex) {
                log.error("User not found: {}", request.getUserId());
                throw new UserNotFoundException(request.getUserId());
            }
            Map<String, List<String>> userAttrs = user.getAttributes();
            if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                    !userAttrs.get("organization-id").contains(orgId)) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        }
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            log.error("Role '{}' does not belong to organization '{}'", role.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm)
                    .users().get(request.getUserId())
                    .roles().realmLevel()
                    .remove(List.of(role));
            log.info("Role '{}' removed from user '{}' in organization '{}' (ID: {})",
                    role.getName(), request.getUserId(), orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to remove role '{}' from user '{}'",
                    role.getName(), request.getUserId());
            throw new UserRoleRemovalException(request.getUserId(), request.getRoleId());
        }
    }

    // Add role to group
    public void addRoleToGroup(String token, AddRoleToKeycloakGroupRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm).rolesById().getRole(request.getRoleId());
            if (role == null) {
                throw new RoleNotFoundException(request.getRoleId());
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", request.getRoleId());
            throw new RoleNotFoundException(request.getRoleId());
        } catch (Exception e) {
            log.error("Error retrieving role: {}", request.getRoleId());
            throw new RoleNotFoundException(request.getRoleId());
        }
        GroupRepresentation group;
        try {
            group = keycloak.realm(realm).groups().group(request.getGroupId()).toRepresentation();
            if (group == null) {
                throw new GroupNotFoundException(request.getGroupId());
            }
        } catch (Exception e) {
            log.error("Group not found: {}", request.getGroupId());
            throw new GroupNotFoundException(request.getGroupId());
        }
        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            log.error("Role '{}' does not belong to organization '{}'", role.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm)
                    .groups().group(request.getGroupId())
                    .roles().realmLevel()
                    .add(List.of(role));
            log.info("Role '{}' assigned to group '{}' in organization '{}' (ID: {})",
                    role.getName(), group.getName(), orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to group '{}'", role.getName(), group.getName());
            throw new GroupRoleAssignmentException(request.getGroupId(), request.getRoleId());
        }
    }

    // Delete role
    public void deleteRole(String token, String roleId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm).rolesById().getRole(roleId);
            if (role == null) {
                throw new RoleNotFoundException(roleId);
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", roleId);
            throw new RoleNotFoundException(roleId);
        }
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            keycloak.realm(realm).rolesById().deleteRole(roleId);
            log.info("Role '{}' (ID: {}) deleted from organization '{}' (ID: {})",
                    role.getName(), roleId, orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to delete role '{}': {}", role.getName(), e.getMessage());
            throw new RoleDeletionException(roleId);
        }
    }
    // get Role Users
    public UserRolesListResponse getUserRoles(String token, String userId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var members = orgResource.members().getAll();
            boolean userBelongsToOrg = members.stream()
                    .anyMatch(u -> u.getId().equals(userId));
            if (!userBelongsToOrg) {
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        } catch (UnauthorizedOrganizationAccessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Organization API check failed, falling back to attributes: {}", e.getMessage());
            UserRepresentation user;
            try {
                user = keycloak.realm(realm).users().get(userId).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException(userId);
                }
            } catch (NotFoundException ex) {
                log.error("User not found: {}", userId);
                throw new UserNotFoundException(userId);
            }
            Map<String, List<String>> userAttrs = user.getAttributes();
            if (userAttrs == null || !userAttrs.containsKey("organization-id") ||
                    !userAttrs.get("organization-id").contains(orgId)) {
                log.error("User '{}' does not belong to organization '{}'", userId, orgName);
                throw new UnauthorizedOrganizationAccessException(orgName);
            }
        }
        try {
            List<RoleRepresentation> allRoles = keycloak.realm(realm)
                    .users().get(userId)
                    .roles().realmLevel()
                    .listEffective();
            List<String> orgRoleNames = allRoles.stream()
                    .filter(role -> {
                        Map<String, List<String>> roleAttrs = role.getAttributes();
                        return roleAttrs == null ||
                                !roleAttrs.containsKey("organization-id") ||
                                roleAttrs.get("organization-id").contains(orgId);
                    })
                    .map(RoleRepresentation::getName)
                    .toList();

            log.info("{} roles retrieved for user '{}' in organization '{}' (ID: {})",
                    orgRoleNames.size(), userId, orgName, orgId);
            return UserRolesListResponse.builder()
                    .userId(userId)
                    .roles(orgRoleNames)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving roles for user '{}': {}", userId, e.getMessage());
            throw new UserRolesRetrievalException(userId);
        }
    }

    // Remove role from group
    public void removeRoleFromGroup(String token, RemoveRoleFromKeycloakGroupRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);

        // Verify that the role exists (retrieved by name for consistency with request)
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm)
                    .roles().get(request.getRoleName())
                    .toRepresentation();
            if (role == null) {
                throw new RoleNotFoundException(request.getRoleName());
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", request.getRoleName());
            throw new RoleNotFoundException(request.getRoleName());
        } catch (Exception e) {
            log.error("Error retrieving role: {}", request.getRoleName());
            throw new RoleNotFoundException(request.getRoleName());
        }

        // Verify that the group exists and belongs to the organization
        GroupRepresentation group;
        try {
            group = keycloak.realm(realm).groups().group(request.getGroupId()).toRepresentation();
            if (group == null) {
                throw new GroupNotFoundException(request.getGroupId());
            }
        } catch (Exception e) {
            log.error("Group not found: {}", request.getGroupId());
            throw new GroupNotFoundException(request.getGroupId());
        }

        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            log.error("Group '{}' does not belong to organization '{}'",
                    group.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }

        // Verify that the role belongs to the organization (via attributes)
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            log.error("Role '{}' does not belong to organization '{}'", role.getName(), orgName);
            throw new UnauthorizedOrganizationAccessException(orgName);
        }

        try {
            keycloak.realm(realm)
                    .groups().group(request.getGroupId())
                    .roles().realmLevel()
                    .remove(List.of(role));
            log.info("Role '{}' removed from group '{}' in organization '{}' (ID: {})",
                    role.getName(), group.getName(), orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to remove role '{}' from group '{}'",
                    role.getName(), group.getName());
            throw new GroupRoleRemovalException(request.getGroupId(), request.getRoleName());
        }
    }
// get role for users
    public List<UserResponse> getRoleUsers(String token, String roleId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);
        RoleRepresentation role;
        try {
            role = keycloak.realm(realm).rolesById().getRole(roleId);
            if (role == null) {
                throw new RoleNotFoundException(roleId);
            }
        } catch (NotFoundException e) {
            log.error("Role not found: {}", roleId);
            throw new RoleNotFoundException(roleId);
        }

        // Verify that the role belongs to the organization (via attributes)
        Map<String, List<String>> roleAttrs = role.getAttributes();
        if (roleAttrs != null && roleAttrs.containsKey("organization-id") &&
                !roleAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }
        try {
            var orgResource = keycloak.realm(realm).organizations().get(orgId);
            var orgMembers = orgResource.members().getAll();
            Set<String> orgMemberIds = orgMembers.stream()
                    .map(UserRepresentation::getId)
                    .collect(Collectors.toSet());

            Set<UserRepresentation> roleUsers = keycloak.realm(realm)
                    .roles().get(role.getName())
                    .getRoleUserMembers();

            List<UserResponse> result = roleUsers.stream()
                    .filter(user -> orgMemberIds.contains(user.getId()))
                    .map(user -> {
                        try {
                            List<String> roleNames = keycloak.realm(realm)
                                    .users().get(user.getId())
                                    .roles().realmLevel()
                                    .listEffective()
                                    .stream()
                                    .map(RoleRepresentation::getName)
                                    .toList();
                            return userMapper.toUserResponse(user, roleNames);
                        } catch (Exception e) {
                            log.warn("Failed to retrieve roles for user '{}': {}",
                                    user.getUsername(), e.getMessage());
                            return userMapper.toUserResponse(user, List.of());
                        }
                    })
                    .toList();

            log.info("{} users with role '{}' retrieved for organization '{}' (ID: {})",
                    result.size(), role.getName(), orgName, orgId);
            return result;

        } catch (Exception e) {
            log.warn("Organization API failed, falling back to attributes: {}", e.getMessage());
            try {
                Set<UserRepresentation> roleUsers = keycloak.realm(realm)
                        .roles().get(role.getName())
                        .getRoleUserMembers();
                List<UserResponse> result = roleUsers.stream()
                        .filter(user -> {
                            Map<String, List<String>> userAttrs = user.getAttributes();
                            return userAttrs != null && userAttrs.containsKey("organization-id") &&
                                    userAttrs.get("organization-id").contains(orgId);
                        })
                        .map(user -> {
                            try {
                                List<String> roleNames = keycloak.realm(realm)
                                        .users().get(user.getId())
                                        .roles().realmLevel()
                                        .listEffective()
                                        .stream()
                                        .map(RoleRepresentation::getName)
                                        .toList();
                                return userMapper.toUserResponse(user, roleNames);
                            } catch (Exception ex) {
                                log.warn("Failed to retrieve roles for user '{}': {}",
                                        user.getUsername(), ex.getMessage());
                                return userMapper.toUserResponse(user, List.of());
                            }
                        })
                        .toList();

                log.info("{} users with role '{}' retrieved for organization '{}' (ID: {})",
                        result.size(), role.getName(), orgName, orgId);
                return result;
            } catch (Exception ex) {
                log.error("Error retrieving users for role '{}': {}", role.getName(), ex.getMessage());
                throw new RoleUsersRetrievalException(roleId);
            }
        }
    }

    // Get group roles
    public GroupRolesListResponse getGroupRoles(String token, String groupId) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);

        // Verify that the group exists and belongs to the organization
        GroupRepresentation group;
        try {
            group = keycloak.realm(realm).groups().group(groupId).toRepresentation();
            if (group == null) {
                throw new GroupNotFoundException(groupId);
            }
        } catch (NotFoundException e) {
            log.error("Group not found: {}", groupId);
            throw new GroupNotFoundException(groupId);
        }

        Map<String, List<String>> groupAttrs = group.getAttributes();
        if (groupAttrs == null || !groupAttrs.containsKey("organization-id") ||
                !groupAttrs.get("organization-id").contains(orgId)) {
            throw new UnauthorizedOrganizationAccessException(orgName);
        }

        try {
            List<RoleRepresentation> allRoles = keycloak.realm(realm)
                    .groups().group(groupId)
                    .roles().realmLevel()
                    .listAll();

            // Filter only the roles belonging to this organization
            List<String> orgRoleNames = allRoles.stream()
                    .filter(role -> {
                        Map<String, List<String>> roleAttrs = role.getAttributes();
                        return roleAttrs == null ||
                                !roleAttrs.containsKey("organization-id") ||
                                roleAttrs.get("organization-id").contains(orgId);
                    })
                    .map(RoleRepresentation::getName)
                    .toList();

            log.info("{} roles retrieved for group '{}' in organization '{}' (ID: {})",
                    orgRoleNames.size(), group.getName(), orgName, orgId);

            return GroupRolesListResponse.builder()
                    .groupId(groupId)
                    .groupName(group.getName())
                    .roles(orgRoleNames)
                    .build();
        } catch (Exception e) {
            log.error("Failed to retrieve roles for group '{}': {}",
                    group.getName(), e.getMessage());
            throw new RuntimeException("Failed to retrieve group roles: " + e.getMessage(), e);
        }
    }
    // Get realm roles
    public List<RoleResponse> getRealmRoles(String token) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);

        try {
            List<RoleResponse> result = keycloak.realm(realm).roles()
                    .list().stream()
                    .filter(role -> {
                        // Filter roles based on organization (via attributes)
                        Map<String, List<String>> roleAttrs = role.getAttributes();
                        if (roleAttrs == null || !roleAttrs.containsKey("organization-id")) {
                            // Roles without organization-id attribute are considered global/system roles
                            return true;
                        } else {
                            // Roles with organization-id attribute: ensure they belong to this organization
                            return roleAttrs.get("organization-id").contains(orgId);
                        }
                    })
                    .map(role -> RoleResponse.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .description(role.getDescription())
                            .build())
                    .toList();

            log.info("{} roles retrieved for organization '{}' (ID: {})",
                    result.size(), orgName, orgId);
            return result;
        } catch (Exception e) {
            log.error("Failed to retrieve roles for organization '{}': {}", orgName, e.getMessage());
            throw new RoleRetrievalException(orgName);
        }
    }

    // create realm role
    public void createRealmRole(String token, RoleCreationRequest request) {
        String orgName = extractOrganizationName(token);
        String orgId = getOrganizationIdByName(orgName);

        // Vérifier qu'un rôle du même nom n'existe pas déjà pour cette organisation
        boolean exists = keycloak.realm(realm).roles().list().stream()
                .filter(r -> r.getName().equalsIgnoreCase(request.getRoleName()))
                .anyMatch(r -> {
                    Map<String, List<String>> attrs = r.getAttributes();
                    return attrs != null && attrs.containsKey("organization-id") &&
                            attrs.get("organization-id").contains(orgId);
                });

        if (exists) {
            log.warn("The role '{}' already exists for organization '{}'", request.getRoleName(), orgName);
            throw new RoleAlreadyExistsException(request.getRoleName());
        }

        try {
            // Créer le rôle avec les bons attributs
            RoleRepresentation role = new RoleRepresentation();
            role.setName(request.getRoleName());
            role.setDescription(request.getDescription());

            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("organization-id", List.of(orgId));
            role.setAttributes(attributes);

            keycloak.realm(realm).roles().create(role);
            log.info("Realm role '{}' created for organization '{}' (ID: {})",
                    request.getRoleName(), orgName, orgId);
        } catch (Exception e) {
            log.error("Failed to create realm role '{}' for organization '{}': {}",
                    request.getRoleName(), orgName, e.getMessage());
            throw new RoleCreationException(request.getRoleName());
        }
    }
}
