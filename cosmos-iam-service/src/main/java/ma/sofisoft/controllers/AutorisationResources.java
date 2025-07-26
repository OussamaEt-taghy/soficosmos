package ma.sofisoft.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.autorisations.*;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.autorisations.AutorisationServices;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/autorisations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autorisations", description = "Gestion des permissions attribuées aux rôles")
@Slf4j
@ApplicationScoped
public class AutorisationResources {

    @Inject
    AutorisationServices autorisationServices;

    @POST
    @Path("/assign")
    @Operation(summary = "Assigner une permission à un rôle")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Permission assignée"),
            @APIResponse(responseCode = "409", description = "Déjà assignée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response assignPermission(@HeaderParam("Authorization") String auth,
                                     @Valid AssignPermissionToRoleRequest request) {
        String token = extractToken(auth);
        autorisationServices.assignPermissionToRole(token, request);
        return Response.ok().build();
    }

    @POST
    @Path("/remove")
    @Operation(summary = "Retirer une permission d’un rôle")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Permission retirée"),
            @APIResponse(responseCode = "404", description = "Association non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response removePermission(@HeaderParam("Authorization") String auth,
                                     @Valid RemovePermissionFromRoleRequest request) {
        String token = extractToken(auth);
        autorisationServices.removePermissionFromRole(token, request);
        return Response.ok().build();
    }

    @GET
    @Path("/by-role/{roleId}")
    @Operation(summary = "Lister les permissions d’un rôle")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Liste récupérée")
    })
    public Response getPermissionsByRole(@HeaderParam("Authorization") String auth,
                                         @PathParam("roleId") String roleId) {
        String token = extractToken(auth);
        List<AutorisationResponse> result = autorisationServices.getAutorisationsByRole(token, roleId);
        return Response.ok(result).build();
    }

    @GET
    @Path("/by-permission/{permissionId}")
    @Operation(summary = "Lister les rôles associés à une permission")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Liste récupérée")
    })
    public Response getRolesByPermission(@HeaderParam("Authorization") String auth,
                                         @PathParam("permissionId") UUID permissionId) {
        String token = extractToken(auth);
        List<AutorisationResponse> result = autorisationServices.getAutorisationsByPermission(token, permissionId);
        return Response.ok(result).build();
    }
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
