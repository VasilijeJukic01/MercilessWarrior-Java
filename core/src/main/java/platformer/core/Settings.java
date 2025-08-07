package platformer.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Manages global, user-configurable game settings that affect the player's experience.
 * <p>
 * This class serves as a simple data container for preferences.
 * These settings are distinct from the {@link Account} data, as they do not track game progress and are typically saved locally to persist between sessions.
 */
@Getter
@Setter
public class Settings {
    private boolean screenShake = true;
    private double particleDensity = 1.0;
    private boolean showDamageCounters = true;
}
