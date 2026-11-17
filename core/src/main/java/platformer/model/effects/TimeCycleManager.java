package platformer.model.effects;

/**
 * Manages the in-game day-night cycle and provides smooth cosine-interpolated daylight and night factors.
 */
public class TimeCycleManager {

    /* The duration of a full in-game day in real-world seconds. */
    public static final double SECONDS_PER_GAME_DAY = 1440.0; // 24 minutes
    private static final int UPS = 200;

    // Dawn: 0.18 -> 0.30 | Day: 0.30 -> 0.68 | Dusk: 0.68 -> 0.82 | Night: 0.82 -> 0.18
    public static final double DAWN_START = 0.18;
    public static final double DAWN_END = 0.30;
    public static final double DUSK_START = 0.68;
    public static final double DUSK_END = 0.82;

    private double gameTime = 0;
    private boolean lockedToNight = false;

    /**
     * Advances the game clock by one tick. This should be called once per frame from the main game loop.
     */
    public void update() {
        if (lockedToNight) {
            gameTime = 0;
            return;
        }
        gameTime += 1.0 / UPS;
        if (gameTime >= SECONDS_PER_GAME_DAY) {
            gameTime = 0;
        }
    }

    /**
     * Calculates the current time of day as a normalized value.
     *
     * @return A double value from 0.0 (midnight start) to 1.0 (midnight end).
     */
    public double getGameTimeNormalized() {
        return gameTime / SECONDS_PER_GAME_DAY;
    }

    /**
     * Returns a smoothly interpolated daylight factor from 0.0 (full night) to 1.0 (full day).
     */
    public float getDaylightFactor() {
        if (lockedToNight) return 0.0f;
        double t = getGameTimeNormalized();
        if (t >= DAWN_END && t < DUSK_START) {
            return 1.0f;
        }
        else if (t >= DAWN_START && t < DAWN_END) {
            double progress = (t - DAWN_START) / (DAWN_END - DAWN_START);
            return cosineEase(progress);
        }
        else if (t >= DUSK_START && t < DUSK_END) {
            double progress = (t - DUSK_START) / (DUSK_END - DUSK_START);
            return 1.0f - cosineEase(progress);
        }
        return 0.0f;
    }

    /**
     * Returns a smoothly interpolated night factor from 0.0 (full day) to 1.0 (full night).
     */
    public float getNightFactor() {
        return 1.0f - getDaylightFactor();
    }

    private float cosineEase(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return (float) ((1.0 - Math.cos(clamped * Math.PI)) * 0.5);
    }

    public boolean isDay() {
        double time = getGameTimeNormalized();
        return time >= DAWN_END && time < DUSK_START;
    }

    /**
     * Checks if it is currently nighttime.
     * Night is defined as the period from 0% to 25% and 75% to 100% of the cycle.
     *
     * @return true if it is nighttime, false otherwise.
     */
    public boolean isNight() {
        double time = getGameTimeNormalized();
        return time < DAWN_START || time >= DUSK_END;
    }

    /**
     * Checks if it is currently dawn.
     * Dawn is the transition from night to day (20% to 30% of the cycle).
     *
     * @return true if it is dawn, false otherwise.
     */
    public boolean isDawn() {
        double time = getGameTimeNormalized();
        return time >= DAWN_START && time < DAWN_END;
    }

    /**
     * Checks if it is currently dusk.
     * Dusk is the transition from day to night (70% to 80% of the cycle).
     *
     * @return true if it is dusk, false otherwise.
     */
    public boolean isDusk() {
        double time = getGameTimeNormalized();
        return time >= DUSK_START && time < DUSK_END;
    }

    public void setLockedToNight(boolean locked) {
        this.lockedToNight = locked;
        if (locked) {
            this.gameTime = 0;
        }
    }

    /**
     * Advances the in-game clock by a fraction of a full day (e.g., 0.5 for 12 in-game hours).
     *
     * @param normalizedDelta Fraction of a day to skip forward (0.0 to 1.0).
     */
    public void advanceTime(double normalizedDelta) {
        if (lockedToNight) return;
        gameTime = (gameTime + normalizedDelta * SECONDS_PER_GAME_DAY) % SECONDS_PER_GAME_DAY;
    }

    public void reset() {
        this.lockedToNight = false;
    }

}