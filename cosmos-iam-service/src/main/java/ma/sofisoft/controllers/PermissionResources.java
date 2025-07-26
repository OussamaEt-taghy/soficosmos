package ma.sofisoft.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.permissions.*;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.permissions.PermissionServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permissions", description = "Gestion des permissions dans le système")
@Slf4j
@ApplicationScoped
public class PermissionResources {

    @Inject
    PermissionServices permissionServices;

    @POST
    @Path("/create")
    @Operation(summary = "Créer une permission", description = "Crée une nouvelle permission")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Permission créée avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Permission déjà existante",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createPermission(@HeaderParam("Authorization") String auth,
                                     @Valid CreatePermissionRequest request) {
        String token = extractToken(auth);
        UUID id = permissionServices.createPermission(token, request);
        return Response.status(Response.Status.CREATED).entity(id).build();
    }

    @PUT
    @Path("/update/{permissionId}")
    @Operation(summary = "Mettre à jour une permission", description = "Met à jour une permission existante")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Permission mise à jour"),
            @APIResponse(responseCode = "404", description = "Permission non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updatePermission(@HeaderParam("Authorization") String auth,
                                     @PathParam("permissionId") UUID permissionId,
                                     @Valid UpdatePermissionRequest request) {
        String token = extractToken(auth);
        permissionServices.updatePermission(token, permissionId, request);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{permissionId}")
    @Operation(summary = "Supprimer une permission", description = "Supprime une permission si elle n'est pas utilisée")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Permission supprimée"),
            @APIResponse(responseCode = "404", description = "Permission non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Permission utilisée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deletePermission(@HeaderParam("Authorization") String auth,
                                     @PathParam("permissionId") UUID permissionId) {
        String token = extractToken(auth);
        permissionServices.deletePermission(token, permissionId);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Lister les permissions", description = "Récupère la liste de toutes les permissions")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Liste des permissions récupérée")
    })
    public Response getAllPermissions(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        List<PermissionResponse> permissions = permissionServices.getAllPermissions(token);
        return Response.ok(permissions).build();
    }

    @GET
    @Path("/{permissionId}")
    @Operation(summary = "Obtenir une permission par ID", description = "Récupère les détails d'une permission")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Permission récupérée"),
            @APIResponse(responseCode = "404", description = "Permission non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getPermission(@HeaderParam("Authorization") String auth,
                                  @PathParam("permissionId") UUID permissionId) {
        String token = extractToken(auth);
        PermissionResponse response = permissionServices.getPermissionById(token, permissionId);
        return Response.ok(response).build();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
