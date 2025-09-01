package ma.sofisoft.controllers;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.dtos.city.CreateCityRequest;
import ma.sofisoft.dtos.city.UpdateCityRequest;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.city.CityService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/loc-apis/city")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "CITY API", description = "Gestion des villes")
@ApplicationScoped
public class CityResource {

    @Inject
    CityService cityService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(summary = "Créer une ville", description = "Créer une nouvelle ville")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Ville créée avec succès"),
            @APIResponse(responseCode = "400", description = "Données de ville invalides, pays ou région introuvable, ou incohérence géographique",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code ville existe déjà dans ce pays et cette région",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createCity(@HeaderParam("Authorization") String auth, @Valid CreateCityRequest dto) {
        String token = extractToken(auth);
        String createdBy = getCurrentUserName();
        return Response.ok(cityService.createCity(dto, createdBy)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mettre à jour une ville", description = "Met à jour une ville existante")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Ville mise à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données de ville invalides, pays ou région introuvable, ou incohérence géographique",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Ville non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code ville existe déjà dans ce pays et cette région",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateCity(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id, @Valid UpdateCityRequest dto) {
        String token = extractToken(auth);
        String updatedBy = getCurrentUserName();
        return Response.ok(cityService.updateCity(id, dto, updatedBy)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer une ville par ID", description = "Retourne une ville basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Ville récupérée avec succès"),
            @APIResponse(responseCode = "404", description = "Ville non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getCityById(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        return Response.ok(cityService.getCityById(id)).build();
    }

    @GET
    @Operation(summary = "Liste des villes", description = "Retourne la liste de toutes les villes")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Villes récupérées avec succès"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAllCities(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        return Response.ok(cityService.getAllCities()).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer une ville", description = "Supprime une ville basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Ville supprimée avec succès"),
            @APIResponse(responseCode = "404", description = "Ville non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer une ville utilisée par des adresses",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteCity(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        cityService.deleteCity(id);
        return Response.ok("La ville est bien supprimée").build();
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