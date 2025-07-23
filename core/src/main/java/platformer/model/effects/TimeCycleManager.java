package platformer.model.effects;

/**
 * Manages the in-game day-night cycle.
 * <p>
 * This class acts as the central clock for the game world. It tracks the current time,
 * handles its progression, and provides utility methods to query the current period of the day.
 */
public class TimeCycleManager {

    /* The duration of a full in-game day in real-world seconds. */
    public static final double SECONDS_PER_GAME_DAY = 600.0; // 10 minutes
    private static final int UPS = 200;
    private double gameTime = 0;

    /**
     * Advances the game clock by one tick. This should be called once per frame from the main game loop.
     */
    public void update() {
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
     * Checks if it is currently daytime.
     * Day is defined as the period from 25% to 75% of the cycle.
     *
     * @return true if it is daytime, false otherwise.
     */
    public boolean isDay() {
        double time = getGameTimeNormalized();
        return time >= 0.25 && time < 0.75;
    }

    /**
     * Checks if it is currently nighttime.
     * Night is defined as the period from 0% to 25% and 75% to 100% of the cycle.
     *
     * @return true if it is nighttime, false otherwise.
     */
    public boolean isNight() {
        return !isDay();
    }

    /**
     * Checks if it is currently dawn.
     * Dawn is the transition from night to day (20% to 30% of the cycle).
     *
     * @return true if it is dawn, false otherwise.
     */
    public boolean isDawn() {
        double time = getGameTimeNormalized();
        return time >= 0.20 && time < 0.30;
    }

    /**
     * Checks if it is currently dusk.
     * Dusk is the transition from day to night (70% to 80% of the cycle).
     *
     * @return true if it is dusk, false otherwise.
     */
    public boolean isDusk() {
        double time = getGameTimeNormalized();
        return time >= 0.70 && time < 0.80;
    }

}