package ma.sofisoft.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.permissionGroup.*;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.PermissionGroup.PermissionGroupServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/permission-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission Groups", description = "Gestion des groupes de permissions")
@Slf4j
@ApplicationScoped
public class GroupPermissionResources {

    @Inject
    PermissionGroupServices permissionGroupServices;

    @POST
    @Path("/create")
    @Operation(summary = "Créer un groupe de permissions")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Créé avec succès"),
            @APIResponse(responseCode = "400", description = "Données invalides", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Groupe déjà existant", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response create(@HeaderParam("Authorization") String auth, @Valid CreatePermissionGroupRequest request) {
        String token = extractToken(auth);
        UUID id = permissionGroupServices.createPermissionGroup(token, request);
        return Response.status(Response.Status.CREATED).entity(id).build();
    }

    @PUT
    @Path("/update/{groupId}")
    @Operation(summary = "Mettre à jour un groupe")
    public Response update(@HeaderParam("Authorization") String auth,
                           @PathParam("groupId") UUID groupId,
                           @Valid UpdatePermissionGroupRequest request) {
        String token = extractToken(auth);
        permissionGroupServices.updatePermissionGroup(token, groupId, request);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{groupId}")
    @Operation(summary = "Supprimer un groupe")
    public Response delete(@HeaderParam("Authorization") String auth,
                           @PathParam("groupId") UUID groupId) {
        String token = extractToken(auth);
        permissionGroupServices.deletePermissionGroup(token, groupId);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Lister tous les groupes de permissions")
    public Response list(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        List<PermissionGroupResponse> groups = permissionGroupServices.getAllPermissionGroups(token);
        return Response.ok(groups).build();
    }

    @GET
    @Path("/{groupId}")
    @Operation(summary = "Obtenir un groupe par ID")
    public Response getById(@HeaderParam("Authorization") String auth,
                            @PathParam("groupId") UUID groupId) {
        String token = extractToken(auth);
        PermissionGroupResponse group = permissionGroupServices.getPermissionGroupById(token, groupId);
        return Response.ok(group).build();
    }

    @POST
    @Path("/assign-permission")
    @Operation(summary = "Assigner une permission à un groupe")
    public Response assign(@HeaderParam("Authorization") String auth,
                           @Valid AssignPermissionToGroupRequest request) {
        String token = extractToken(auth);
        permissionGroupServices.assignPermissionToGroup(token, request);
        return Response.ok().build();
    }

    @POST
    @Path("/remove-permission")
    @Operation(summary = "Retirer une permission d’un groupe")
    public Response remove(@HeaderParam("Authorization") String auth,
                           @Valid RemovePermissionFromGroupRequest request) {
        String token = extractToken(auth);
        permissionGroupServices.removePermissionFromGroup(token, request);
        return Response.ok().build();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
