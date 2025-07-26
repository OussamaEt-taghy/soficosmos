package ma.sofisoft.controllers;
import it.oussama.Secured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.RemoveRoleFromKeycloakGroupRequest;
import ma.sofisoft.dtos.roles.*;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.roles.RoleServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
@Tag(name = "Keycloak Roles", description = "Role management")
@Slf4j
@ApplicationScoped
public class RoleResources {
    @Inject
    RoleServices roleServices;
    @POST
    @Path("/create")
    @Operation(summary = "Créer un nouveau rôle", description = "Crée un nouveau rôle realm dans Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Rôle créé avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Rôle déjà existant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createRole(@HeaderParam("Authorization") String auth, @Valid RoleCreationRequest request) {
        String token = extractToken(auth);
        roleServices.createRealmRole(token, request);
        return Response.status(Response.Status.CREATED).build();
    }
    @POST
    @Path("/add-to-group")
    @Operation(summary = "Ajouter un rôle à un groupe", description = "Associe un rôle à un groupe Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôle ajouté au groupe avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Rôle ou groupe non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Rôle déjà associé au groupe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response addToGroup(@HeaderParam("Authorization") String auth, AddRoleToKeycloakGroupRequest request) {
        String token = extractToken(auth);
        roleServices.addRoleToGroup(token, request);
        return Response.ok().build();
    }

    @POST
    @Path("/remove-from-group")
    @Operation(summary = "Retirer un rôle d'un groupe", description = "Supprime l'association entre un rôle et un groupe Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôle retiré du groupe avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Rôle, groupe ou association non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response removeFromGroup(@HeaderParam("Authorization") String auth, RemoveRoleFromKeycloakGroupRequest request) {
        String token = extractToken(auth);
        roleServices.removeRoleFromGroup(token, request);
        return Response.ok().build();
    }


    @GET
    @Path("/user-roles")
    @Operation(summary = "Récupérer les rôles d'un utilisateur", description = "Récupère tous les rôles associés à un utilisateur spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôles utilisateur récupérés avec succès"),
            @APIResponse(responseCode = "400", description = "Paramètres de requête invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getUserRoles(@HeaderParam("Authorization") String auth, @QueryParam("userId") String userId) {
        String token = extractToken(auth);
        UserRolesListResponse roles = roleServices.getUserRoles(token, userId);
        return Response.ok(roles).build();
    }
    @GET
    @Path("/group-roles")
    @Operation(summary = "Récupérer les rôles d'un groupe", description = "Récupère tous les rôles assignés à un groupe spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôles du groupe récupérés avec succès"),
            @APIResponse(responseCode = "400", description = "Paramètres de requête invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Groupe non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getGroupRoles(@HeaderParam("Authorization") String auth, @QueryParam("groupId") String groupId) {
        String token = extractToken(auth);
        GroupRolesListResponse roles = roleServices.getGroupRoles(token, groupId);
        return Response.ok(roles).build();
    }
    @GET
    @Operation(summary = "Récupérer tous les rôles", description = "Récupère la liste de tous les rôles realm disponibles")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôles récupérés avec succès"),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAllRoles(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        List<RoleResponse> roles = roleServices.getRealmRoles(token);
        return Response.ok(roles).build();
    }

    @POST
    @Path("/assign-to-user")
    @Operation(summary = "Assigner un rôle à un utilisateur", description = "Assigne un rôle realm à un utilisateur spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôle assigné à l'utilisateur avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur ou rôle non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Rôle déjà assigné à l'utilisateur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response assignRoleToUser(@HeaderParam("Authorization") String auth,
                                     @QueryParam("userId") String userId,
                                     @QueryParam("roleId") String roleId) {
        String token = extractToken(auth);
        roleServices.addRoleToUser(token, userId, roleId);
        return Response.ok().build();
    }


    @POST
    @Path("/remove-from-user")
    @Operation(summary = "Retirer un rôle d'un utilisateur", description = "Supprime l'association entre un rôle et un utilisateur")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Rôle retiré de l'utilisateur avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur, rôle ou association non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response removeRoleFromUser(@HeaderParam("Authorization") String auth, @Valid RemoveUserRoleRequest request) {
        String token = extractToken(auth);
        roleServices.removeRoleFromUser(token, request);
        return Response.ok().build();
    }


    @DELETE
    @Path("/{roleId}")
    @Operation(summary = "Supprimer un rôle", description = "Supprime un rôle realm de Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Rôle supprimé avec succès"),
            @APIResponse(responseCode = "400", description = "ID de rôle invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Rôle non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer le rôle (contraintes)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteRole(@HeaderParam("Authorization") String auth, @PathParam("roleId") String roleId) {
        String token = extractToken(auth);
        roleServices.deleteRole(token, roleId);
        return Response.noContent().build();
    }


    @GET
    @Path("/{roleId}/users")
    @Operation(summary = "Récupérer les utilisateurs ayant un rôle", description = "Récupère la liste de tous les utilisateurs qui possèdent un rôle spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateurs du rôle récupérés avec succès"),
            @APIResponse(responseCode = "400", description = "ID de rôle invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Rôle non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getRoleUsers(@HeaderParam("Authorization") String auth, @PathParam("roleId") String roleId) {
        String token = extractToken(auth);
        List<UserResponse> users = roleServices.getRoleUsers(token, roleId);
        return Response.ok(users).build();
    }


    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token d'autorisation invalide ou manquant");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
