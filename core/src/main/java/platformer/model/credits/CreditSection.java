package platformer.model.credits;

import java.util.List;

/**
 * Represents a single category or section in the game's credits screen.
 * <p>
 * This record is designed to be a simple data model, typically populated by deserializing a JSON file using a library like Gson.
 * Each section contains a title and a list of individual credit entries.
 *
 * @see CreditEntry
 */
public record CreditSection (
     String section,
     List<CreditEntry> entries
) {
}