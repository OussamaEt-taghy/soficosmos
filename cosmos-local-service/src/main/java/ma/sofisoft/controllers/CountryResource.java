package ma.sofisoft.controllers;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.dtos.country.CreateCountryRequest;
import ma.sofisoft.dtos.country.UpdateCountryRequest;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.country.CountryService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/loc-apis/country")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "COUNTRY API", description = "Gestion des pays")
@ApplicationScoped
public class CountryResource {

    @Inject
    CountryService countryService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(summary = "Créer un pays", description = "Créer un nouveau pays")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pays créé avec succès"),
            @APIResponse(responseCode = "400", description = "Données de pays invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code pays existe déjà",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createCountry(@HeaderParam("Authorization") String auth, @Valid CreateCountryRequest dto) {
        String token = extractToken(auth);
        String createdBy = getCurrentUserName();
        return Response.ok(countryService.createCountry(dto, createdBy)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mettre à jour un pays", description = "Met à jour un pays existant")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pays mis à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données de pays invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Pays non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Le code pays existe déjà",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateCountry(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id, @Valid UpdateCountryRequest dto) {
        String token = extractToken(auth);
        String updatedBy = getCurrentUserName();
        return Response.ok(countryService.updateCountry(id, dto, updatedBy)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer un pays par ID", description = "Retourne un pays basé sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pays récupéré avec succès"),
            @APIResponse(responseCode = "404", description = "Pays non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getCountryById(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        return Response.ok(countryService.getCountryById(id)).build();
    }

    @GET
    @Operation(summary = "Liste des pays", description = "Retourne la liste de tous les pays")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pays récupérés avec succès"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAllCountries(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        return Response.ok(countryService.getAllCountries()).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer un pays", description = "Supprime un pays basé sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pays supprimé avec succès"),
            @APIResponse(responseCode = "404", description = "Pays non trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer un pays utilisé par des régions, villes ou adresses",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteCountry(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        countryService.deleteCountry(id);
        return Response.ok("Le pays est bien supprimé").build();
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
