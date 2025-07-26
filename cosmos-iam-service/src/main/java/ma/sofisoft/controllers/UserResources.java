package ma.sofisoft.controllers;
import it.oussama.Secured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.users.CreateUserRequest;
import ma.sofisoft.dtos.users.UpdateUserRequest;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.users.UserServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
//@Secured
@Tag(name = "Keycloak Users", description = "User management in Keycloak")
@Slf4j
@ApplicationScoped
public class UserResources {
    @Inject
    UserServices userServices;

    @POST
    @Path("/create")
    @Operation(summary = "Créer un nouvel utilisateur", description = "Crée un nouvel utilisateur dans Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Utilisateur déjà existant (email ou username)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createUser(@HeaderParam("Authorization") String auth, @Valid CreateUserRequest request) {
        String token = extractToken(auth);
        String id = userServices.createUser(token, request);
        return Response.status(Response.Status.CREATED).entity(id).build();
    }

    @PUT
    @Path("/update/{userId}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur existant")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides ou ID utilisateur invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Conflit avec des données existantes (email/username)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateUser(@HeaderParam("Authorization") String auth,
                               @PathParam("userId") String userId,
                               @Valid UpdateUserRequest request) {
        String token = extractToken(auth);
        userServices.updateUserInfo(token, userId, request);
        return Response.ok().build();
    }

    @PUT
    @Path("/password")
    @Operation(summary = "Mettre à jour le mot de passe", description = "Met à jour le mot de passe d'un utilisateur")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Mot de passe mis à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Paramètres manquants ou invalides (username/password)",
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
    public Response updatePassword(@HeaderParam("Authorization") String auth,
                                   @QueryParam("username") String username,
                                   @QueryParam("newPassword") String newPassword) {
        String token = extractToken(auth);
        userServices.updateUserPassword(token, username, newPassword);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{userId}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur de Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Utilisateur supprimé avec succès"),
            @APIResponse(responseCode = "400", description = "ID utilisateur invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Utilisateur non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer l'utilisateur (contraintes)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteUser(@HeaderParam("Authorization") String auth, @PathParam("userId") String userId) {
        String token = extractToken(auth);
        userServices.deleteUser(token, userId);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Récupérer tous les utilisateurs", description = "Récupère la liste de tous les utilisateurs Keycloak")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateurs récupérés avec succès"),
            @APIResponse(responseCode = "401", description = "Token d'authentification manquant ou invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "403", description = "Permissions insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getUsers(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        List<UserResponse> users = userServices.getUsers(token);
        return Response.ok(users).build();
    }

    @GET
    @Path("/{userId}")
    @Operation(summary = "Récupérer un utilisateur par ID", description = "Récupère les informations d'un utilisateur spécifique")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Utilisateur récupéré avec succès"),
            @APIResponse(responseCode = "400", description = "ID utilisateur invalide",
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
    public Response getUser(@HeaderParam("Authorization") String auth, @PathParam("userId") String userId) {
        String token = extractToken(auth);
        UserResponse user = userServices.getUserById(token, userId);
        return Response.ok(user).build();
    }
    @PUT
    @Path("/{userId}/status")
    @Operation(summary = "Activer/Désactiver un utilisateur", description = "Active ou désactive un compte utilisateur")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Statut utilisateur mis à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides ou ID utilisateur invalide",
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
    public Response enableUser(@HeaderParam("Authorization") String auth,
                               @PathParam("userId") String userId,
                               @QueryParam("enabled") boolean enabled) {
        String token = extractToken(auth);
        userServices.enableUser(token, userId, enabled);
        return Response.ok().build();
    }

    @GET
    @Path("/{userId}/groups")
    @Operation(summary = "Récupérer les groupes d'un utilisateur", description = "Récupère la liste de tous les groupes auxquels appartient un utilisateur")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Groupes utilisateur récupérés avec succès"),
            @APIResponse(responseCode = "400", description = "ID utilisateur invalide",
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
    public Response getUserGroups(@HeaderParam("Authorization") String auth,
                                  @PathParam("userId") String userId) {
        String token = extractToken(auth);
        List<GroupResponse> groups = userServices.getUserGroups(token, userId);
        return Response.ok(groups).build();
    }
        private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
