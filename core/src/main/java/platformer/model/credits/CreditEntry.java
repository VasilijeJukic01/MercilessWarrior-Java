package platformer.model.credits;

/**
 * Represents a single, individual credit line within a {@link CreditSection}.
 * <p>
 * This record holds the name of the person or group being credited and their specific role, if any.
 * It is designed as a simple data model for deserialization from a JSON file.
 *
 * @see CreditSection
 */
public record CreditEntry(
        String role,
        String name
) {
}