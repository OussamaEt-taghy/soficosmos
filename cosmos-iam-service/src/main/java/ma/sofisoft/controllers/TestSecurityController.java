package ma.sofisoft.controllers;
import it.oussama.RequiredPermission;
import it.oussama.Secured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
@Path("/test-security")
@Secured
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class TestSecurityController {
    @GET
    @Path("/protected")
    @RequiredPermission("CAN_EDIT_PRODUCT")
    public Response protectedEndpoint() {
        return Response.ok("{\"message\":\"Accès OK\"}").build();
    }
}