package ma.sofisoft.controllers;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.AddUserToKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.CreateKeycloakGroupRequest;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.userGroup.RemoveUserFromKeycloakGroupRequest;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.userGroups.GroupServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
//@Secured
@Tag(name = "Keycloak Groups", description = "Group management")
@Slf4j
@ApplicationScoped
public class GroupUserResources {
    @Inject
    GroupServices groupServices;
    @POST
    @Path("/create")
    @Operation(summary = "Créer un nouveau groupe", description = "Crée un nouveau groupe Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Groupe créé avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Groupe déjà existant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response create(@HeaderParam("Authorization") String auth, @Valid CreateKeycloakGroupRequest request) {
        String token = extractToken(auth);
        String id = groupServices.createGroup(token, request);
        return Response.status(Response.Status.CREATED).entity(id).build();
    }


    @DELETE
    @Path("/{groupName}")
    @Operation(summary = "Supprimer un groupe", description = "Supprime un groupe Keycloak par son nom")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Groupe supprimé avec succès"),
            @APIResponse(responseCode = "400", description = "Nom de groupe invalide",
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
    public Response delete(@HeaderParam("Authorization") String auth, @PathParam("groupName") String name) {
        String token = extractToken(auth);
        groupServices.deleteGroup(token, name);
        return Response.noContent().build();
    }

    @POST
    @Path("/add-user")
    @Operation(summary = "Ajouter un utilisateur à un groupe", description = "Ajoute un utilisateur à un groupe Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateur ajouté au groupe avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur ou groupe non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Utilisateur déjà membre du groupe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response addUser(@HeaderParam("Authorization") String auth, @Valid AddUserToKeycloakGroupRequest request) {
        String token = extractToken(auth);
        groupServices.addUserToGroup(token, request);
        return Response.ok().build();
    }

    @POST
    @Path("/remove-user")
    @Operation(summary = "Retirer un utilisateur d'un groupe", description = "Retire un utilisateur d'un groupe Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateur retiré du groupe avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur, groupe ou association non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    public Response removeUser(@HeaderParam("Authorization") String auth, @Valid RemoveUserFromKeycloakGroupRequest request) {
        String token = extractToken(auth);
        groupServices.removeUserFromGroup(token, request);
        return Response.ok().build();
    }

    @GET
   // @RequiredPermission("CAN_EDIT_ACC")
    @Operation(summary = "Récupérer tous les groupes", description = "Récupère la liste de tous les groupes Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Groupes récupérés avec succès"),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAll(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        List<GroupResponse> groups = groupServices.getAllGroups(token);
        return Response.ok(groups).build();
    }

    @GET
    @Path("/{groupId}/members")
    @Operation(summary = "Récupérer les membres d'un groupe", description = "Récupère la liste de tous les utilisateurs membres d'un groupe spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Membres du groupe récupérés avec succès"),
            @APIResponse(responseCode = "400", description = "ID de groupe invalide",
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
    public Response getGroupMembers(@HeaderParam("Authorization") String auth, @PathParam("groupId") String groupId) {
        String token = extractToken(auth);
        List<UserResponse> members = groupServices.getGroupMembers(token, groupId);
        return Response.ok(members).build();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization token manquant ou invalide");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}

