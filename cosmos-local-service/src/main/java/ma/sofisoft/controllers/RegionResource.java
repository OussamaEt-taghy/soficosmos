package ma.sofisoft.controllers;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.dtos.region.CreateRegionRequest;
import ma.sofisoft.dtos.region.UpdateRegionRequest;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.region.RegionService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/loc-apis/region")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "REGION API", description = "Gestion des régions")
@ApplicationScoped
public class RegionResource {

    @Inject
    RegionService regionService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(summary = "Créer une région", description = "Créer une nouvelle région")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Région créée avec succès"),
            @APIResponse(responseCode = "400", description = "Données de région invalides ou pays introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code région existe déjà dans ce pays",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createRegion(@HeaderParam("Authorization") String auth, @Valid CreateRegionRequest dto) {
        String token = extractToken(auth);
        String createdBy = getCurrentUserName();
        return Response.ok(regionService.createRegion(dto, createdBy)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mettre à jour une région", description = "Met à jour une région existante")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Région mise à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données de région invalides ou pays introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Région non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code région existe déjà dans ce pays",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateRegion(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id, @Valid UpdateRegionRequest dto) {
        String token = extractToken(auth);
        String updatedBy = getCurrentUserName();
        return Response.ok(regionService.updateRegion(id, dto, updatedBy)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer une région par ID", description = "Retourne une région basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Région récupérée avec succès"),
            @APIResponse(responseCode = "404", description = "Région non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getRegionById(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        return Response.ok(regionService.getRegionById(id)).build();
    }

    @GET
    @Operation(summary = "Liste des régions", description = "Retourne la liste de toutes les régions")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Régions récupérées avec succès"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAllRegions(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        return Response.ok(regionService.getAllRegions()).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer une région", description = "Supprime une région basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Région supprimée avec succès"),
            @APIResponse(responseCode = "404", description = "Région non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer une région utilisée par des villes ou adresses",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteRegion(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        regionService.deleteRegion(id);
        return Response.ok("La région est bien supprimée").build();
    }

    // ==================== MÉTHODES UTILS ====================

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
        return authHeader.substring("Bearer ".length()).trim();
    }

    private String getCurrentUserName() {
        return jwt.getClaim("name");
    }
}
