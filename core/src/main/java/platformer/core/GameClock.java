package platformer.core;

/**
 * Fixed-timestep simulation clock advanced strictly during active gameplay updates (200 UPS).
 * Automatically freezes during pause screens and hit-stop without manual offset tracking.
 */
public class GameClock {

    public static final int TICKS_PER_SECOND = 200;
    public static final long MS_PER_TICK = 1000L / TICKS_PER_SECOND;

    private long elapsedTicks = 0;

    /**
     * Advances the simulation clock by one engine tick.
     */
    public void tick() {
        elapsedTicks++;
    }

    public long getElapsedTicks() {
        return elapsedTicks;
    }

    public long getElapsedMs() {
        return elapsedTicks * MS_PER_TICK;
    }

    public double getElapsedSeconds() {
        return elapsedTicks / (double) TICKS_PER_SECOND;
    }

    public static int secondsToTicks(double seconds) {
        return (int) Math.round(seconds * TICKS_PER_SECOND);
    }

    public void reset() {
        elapsedTicks = 0;
    }
}