package ma.sofisoft.configuration.multitenancy;

// Stores the schema name (organization) for each HTTP thread
// Uses ThreadLocal to avoid interference between concurrent requests
public class TenantContext {

    // Each thread has its own copy of this variable
    private static final ThreadLocal<String> currentSchema = new ThreadLocal<>();

    // Sets the schema (organization) for the current thread
    public static void setCurrentSchema(String schemaName) {
        currentSchema.set(schemaName);
    }

    // Retrieves the schema for the current thread
    public static String getCurrentSchema() {
        return currentSchema.get();
    }

    // Clears the context (MANDATORY to avoid memory leaks)
    public static void clear() {
        currentSchema.remove();
    }
}
