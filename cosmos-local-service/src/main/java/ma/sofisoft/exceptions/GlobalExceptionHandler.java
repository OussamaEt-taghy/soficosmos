package ma.sofisoft.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    // Injects the request information (path, URI, etc.)
    @Context
    UriInfo uriInfo;
    // Method called automatically when an exception is thrown
    @Override
    public Response toResponse(Throwable exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";
        log.error("Exception caught in GlobalExceptionHandler: ", exception);

        // Bussiness Exeception
        if (exception instanceof BusinessException) {
            BusinessException be = (BusinessException) exception;
            return Response.status(be.getStatusCode())
                    .entity(ErrorResponse.of(
                            be.getStatusCode(),
                            be.getErrorCode(),
                            be.getMessage(),
                            path))
                    .build();
        }
        // Converting JPA/Hibernate exceptions
        if (exception instanceof EntityNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(
                            404,
                            "ENTITY_NOT_FOUND",
                            exception.getMessage(),
                            path))
                    .build();
        }
        // Database constraint violated (exp duplicate)
        if (exception instanceof PersistenceException && exception.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception.getCause();
            String constraintName = cve.getConstraintName();
            String message = "Violation des contraintes de la base de données";
            String errorCode = "DB_CONSTRAINT";
            // Identification of specific constraints
            if (constraintName != null) {
                if (constraintName.contains("ix_acc_account_code")) {
                    message = "Le code de compte doit être unique";
                    errorCode = "DUPLICATE_ACC_CODE";
                }
            }
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.of(
                            409,
                            errorCode,
                            message,
                            path))
                    .build();
        }
        // Handling illegal state errors
        if (exception instanceof IllegalStateException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.of(
                            409,
                            "ILLEGAL_STATE",
                            exception.getMessage(),
                            path))
                    .build();
        }
        // Invalid argument (exp , null or poorly formatted parameter)
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.of(
                            400,
                            "ILLEGAL_ARGUMENT",
                            exception.getMessage(),
                            path))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of(
                        500,
                        "INTERNAL_ERROR",
                        "Une erreur interne s'est produite", path))
                .build();
    }
}
