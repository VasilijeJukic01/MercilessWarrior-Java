package platformer.core;

public class Settings {

    private boolean screenShake = true;
    private double particleDensity = 1.0;

    public boolean isScreenShake() {
        return screenShake;
    }

    public void setScreenShake(boolean screenShake) {
        this.screenShake = screenShake;
    }

    public double getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(double particleDensity) {
        this.particleDensity = particleDensity;
    }
}
