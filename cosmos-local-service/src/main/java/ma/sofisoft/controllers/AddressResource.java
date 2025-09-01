package ma.sofisoft.controllers;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.sofisoft.dtos.address.CreateAddressRequest;
import ma.sofisoft.dtos.address.UpdateAddressRequest;
import ma.sofisoft.exceptions.ErrorResponse;
import ma.sofisoft.services.address.AddressService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

@Path("/loc-apis/address")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "ADDRESS API", description = "Gestion des adresses")
@ApplicationScoped
public class AddressResource {

    @Inject
    AddressService addressService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(summary = "Créer une adresse", description = "Créer une nouvelle adresse")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Adresse créée avec succès"),
            @APIResponse(responseCode = "400", description = "Données d'adresse invalides, entités géographiques introuvables, incohérence géographique ou type d'adresse invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Une adresse par défaut existe déjà pour ce propriétaire",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createAddress(@HeaderParam("Authorization") String auth, @Valid CreateAddressRequest dto) {
        String token = extractToken(auth);
        String createdBy = getCurrentUserName();
        return Response.ok(addressService.createAddress(dto, createdBy)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mettre à jour une adresse", description = "Met à jour une adresse existante")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Adresse mise à jour avec succès"),
            @APIResponse(responseCode = "400", description = "Données d'adresse invalides, entités géographiques introuvables, incohérence géographique ou type d'adresse invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Adresse non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Une adresse par défaut existe déjà pour ce propriétaire",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateAddress(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id, @Valid UpdateAddressRequest dto) {
        String token = extractToken(auth);
        String updatedBy = getCurrentUserName();
        return Response.ok(addressService.updateAddress(id, dto, updatedBy)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer une adresse par ID", description = "Retourne une adresse basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Adresse récupérée avec succès"),
            @APIResponse(responseCode = "404", description = "Adresse non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAddressById(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        return Response.ok(addressService.getAddressById(id)).build();
    }

    @GET
    @Operation(summary = "Liste des adresses", description = "Retourne la liste de toutes les adresses")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Adresses récupérées avec succès"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAllAddresses(@HeaderParam("Authorization") String auth) {
        String token = extractToken(auth);
        return Response.ok(addressService.getAllAddresses()).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer une adresse", description = "Supprime une adresse basée sur son ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Adresse supprimée avec succès"),
            @APIResponse(responseCode = "404", description = "Adresse non trouvée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "409", description = "Impossible de supprimer une adresse utilisée par d'autres services",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteAddress(@HeaderParam("Authorization") String auth, @PathParam("id") UUID id) {
        String token = extractToken(auth);
        addressService.deleteAddress(id);
        return Response.ok("L'adresse est bien supprimée").build();
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