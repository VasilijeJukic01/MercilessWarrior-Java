package platformer.model.effects;

import platformer.animation.SpriteManager;
import platformer.core.Framework;
import platformer.core.Settings;
import platformer.model.effects.particles.*;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.effects.text.DamageNumber;
import platformer.model.effects.text.ItemPickupText;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.PARTICLE_SHEET;
import static platformer.constants.FilePaths.SMOKE_SHEET;

public class EffectManager {

    private final List<DustParticle> foregroundParticles = new ArrayList<>();
    private final List<DustParticle> backgroundParticles = new ArrayList<>();
    private final List<DamageNumber> damageNumbers = new ArrayList<>();
    private final List<ItemPickupText> itemPickupTexts = new ArrayList<>();
    private final Random rand = new Random();

    // Text Effects
    private int itemTextYOffset = 0;
    private int itemTextCooldown = 0;
    private static final int ITEM_TEXT_COOLDOWN_MAX = 20;

    // Ambient Effects
    private AmbientParticle[] ambientParticles;
    private SmokeParticle[] smokeParticles;
    private WindParticle[] windParticles;
    private FallingLeafParticle[] fallingLeafParticles;
    private final AmbientParticleFactory ambientParticleFactory;
    private boolean ambientEffectsActive = true;
    private float dayFactor = 0.0f;
    private float nightFactor = 1.0f;

    // TODO: Temporary
    private boolean stormPeakMode = false;
    private double stormIntensity = 1.0;

    private BufferedImage[] smokeParticleFrames;
    private final Map<Integer, BufferedImage[]> dustParticleCache = new HashMap<>();

    public EffectManager() {
        this.ambientParticleFactory = new AmbientParticleFactory();
        this.ambientParticles = new AmbientParticle[0];
        this.smokeParticles = new SmokeParticle[0];
        this.windParticles = new WindParticle[0];
        this.fallingLeafParticles = new FallingLeafParticle[0];
        preloadAmbientParticleAssets();
    }

    /**
     * Pre-loads and caches all image assets required for ambient particles.
     * This should be called once after SpriteManager is ready.
     */
    public void preloadAmbientParticleAssets() {
        this.smokeParticleFrames = SpriteManager.getInstance().loadFromSprite(SMOKE_SHEET, 1, 0, SMOKE_W, SMOKE_H, 0, SMOKE_W, SMOKE_H);
    }

    /**
     * This method creates and initializes the particles used in the game.
     * It uses the Flyweight pattern to manage the particles, which helps to save memory.
     * The ParticleFactory is used to create and manage the Particle objects.
     * @return An array of Particle objects.
     */
    private AmbientParticle[] loadDustParticles() {
        AmbientParticle[] fireflies = new AmbientParticle[PARTICLES_CAP];
        for (int i = 0; i < fireflies.length; i++) {
            int xPos = rand.nextInt(GAME_WIDTH - 10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT - 10) + 10;
            int size = (int)((rand.nextInt(15 - 5) + 5) * SCALE);
            BufferedImage[] images = dustParticleCache.computeIfAbsent(size, s ->
                    SpriteManager.getInstance().loadFromSprite(PARTICLE_SHEET, DEFAULT_PARTICLE_FRAMES, 0, s, s, 0, PARTICLE_W, PARTICLE_H)
            );

            String key = "DefaultParticle" + size;
            AmbientParticleType ambientParticleType = ambientParticleFactory.getParticleImage(key, images);
            fireflies[i] = new AmbientParticle(ambientParticleType, size, xPos, yPos);
        }
        return fireflies;
    }

    private SmokeParticle[] loadSmokeParticles() {
        int smokeCount = PARTICLES_CAP / 2;
        SmokeParticle[] smoke = new SmokeParticle[smokeCount];
        for (int i = 0; i < smoke.length; i++) {
            smoke[i] = new SmokeParticle(smokeParticleFrames);
        }
        return smoke;
    }

    private WindParticle[] loadWindParticles(int levelWidth, int levelHeight) {
        int windCount = PARTICLES_CAP * 3;
        WindParticle[] wind = new WindParticle[windCount];
        for (int i = 0; i < wind.length; i++) {
            wind[i] = new WindParticle(levelWidth, levelHeight);
        }
        return wind;
    }

    private FallingLeafParticle[] loadFallingLeafParticles(int levelWidth, int levelHeight) {
        int leafCount = (int) (PARTICLES_CAP * 1.8);
        FallingLeafParticle[] leaves = new FallingLeafParticle[leafCount];
        for (int i = 0; i < leaves.length; i++) {
            leaves[i] = new FallingLeafParticle(levelWidth, levelHeight);
        }
        return leaves;
    }

    public void reinitializeAmbientParticles(int levelWidth, int levelHeight) {
        this.ambientParticles = loadDustParticles();
        this.smokeParticles = loadSmokeParticles();
        this.windParticles = loadWindParticles(levelWidth, levelHeight);
        this.fallingLeafParticles = loadFallingLeafParticles(levelWidth, levelHeight);
    }

    public void spawnDustParticles(double x, double y, int count, DustType type, int flipSign, Entity target) {
        Settings settings = Framework.getInstance().getGame().getSettings();
        int adjustedCount = (int) (count * settings.getParticleDensity());

        for (int i = 0; i < adjustedCount; i++) {
            double yOffset = 0;
            if (type == DustType.DASH) {
                yOffset = (rand.nextDouble() - 0.5) * (target.getHitBox().height * 0.8);
            }
            else if (type == DustType.JUMP_PAD) {
                yOffset = target.getHitBox().height * 0.5;
            }

            DustParticle particle = ParticleFactory.createParticle((int) x, (int) (y + yOffset), 0, type, flipSign, target);

            if (type == DustType.WALL_SLIDE) backgroundParticles.add(particle);
            else foregroundParticles.add(particle);
        }
    }

    public void spawnAura(Entity target, int count) {
        if (isAuraActive(target)) return;
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count / 2, DustType.SW_AURA_PULSE, 0, target);
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count, DustType.SW_CHANNELING_AURA, 0, target);
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count, DustType.SW_AURA_CRACKLE, 0, target);
    }

    public void clearAura(Entity target) {
        foregroundParticles.removeIf(p -> p.getTarget() == target && (p.getType() == DustType.SW_CHANNELING_AURA || p.getType() == DustType.SW_AURA_PULSE || p.getType() == DustType.SW_AURA_CRACKLE));
    }

    public boolean isAuraActive(Entity target) {
        return foregroundParticles.stream()
                .anyMatch(p -> p.getTarget() == target && (p.getType() == DustType.SW_CHANNELING_AURA || p.getType() == DustType.SW_AURA_PULSE || p.getType() == DustType.SW_AURA_CRACKLE));
    }

    public void spawnDamageNumber(String text, double x, double y, Color color) {
        if (!Framework.getInstance().getGame().getSettings().isShowDamageCounters()) return;
        damageNumbers.add(new DamageNumber(text, x, y, color));
    }

    public void spawnItemPickupText(String text, Player player, Color color) {
        int yOffset = itemTextYOffset;
        itemPickupTexts.add(new ItemPickupText(text, player, yOffset, color));
        itemTextYOffset += (int)(15 * SCALE);
        itemTextCooldown = ITEM_TEXT_COOLDOWN_MAX;
    }

    /**
     * Clears all active foreground particles of a specific type associated with a target entity.
     *
     * @param target The entity to clear particles for.
     * @param type The DustType of the particles to clear.
     */
    public void clearParticlesByType(Entity target, DustType type) {
        foregroundParticles.removeIf(p -> p.getTarget() == target && p.getType() == type);
    }

    // Core
    public void update() {
        if (itemTextCooldown > 0) itemTextCooldown--;
        else itemTextYOffset = 0;
        updateParticleList(foregroundParticles);
        updateParticleList(backgroundParticles);
        updateDamageNumbers();
        updateItemPickupTexts();
        if (ambientEffectsActive) {
            Arrays.stream(smokeParticles).forEach(SmokeParticle::update);
            if (stormPeakMode) {
                Arrays.stream(windParticles).forEach(p -> p.update(stormIntensity));
            } else {
                if (nightFactor > 0.02f) {
                    Arrays.stream(ambientParticles).forEach(AmbientParticle::update);
                    Arrays.stream(windParticles).forEach(WindParticle::update);
                }
                if (dayFactor > 0.02f) {
                    Arrays.stream(fallingLeafParticles).forEach(FallingLeafParticle::update);
                }
            }
        }
    }

    private void updateParticleList(List<DustParticle> particles) {
        try {
            Iterator<DustParticle> pIterator = particles.iterator();
            while (pIterator.hasNext()) {
                DustParticle p = pIterator.next();
                p.update();
                if (!p.isActive()) pIterator.remove();
            }
        } catch (Exception ignored) {}
    }

    private void updateDamageNumbers() {
        try {
            Iterator<DamageNumber> iterator = damageNumbers.iterator();
            while (iterator.hasNext()) {
                DamageNumber dn = iterator.next();
                dn.update();
                if (!dn.isActive()) iterator.remove();
            }
        } catch (Exception ignored) {}
    }

    private void updateItemPickupTexts() {
        try {
            Iterator<ItemPickupText> iterator = itemPickupTexts.iterator();
            while (iterator.hasNext()) {
                ItemPickupText text = iterator.next();
                text.update();
                if (!text.isActive()) iterator.remove();
            }
        } catch (Exception ignored) {}
    }

    public void updateTimeOfDay(TimeCycleManager timeCycleManager) {
        if (timeCycleManager == null) return;
        double time = timeCycleManager.getGameTimeNormalized();

        if (time >= 0.30 && time < 0.70) {
            // Daytime
            this.nightFactor = 0.0f;
            this.dayFactor = 1.0f;
        }
        else if (time >= 0.20 && time < 0.30) {
            // Dawn
            float progress = (float) ((time - 0.20) / 0.10);
            this.nightFactor = 1.0f - progress;
            this.dayFactor = progress;
        }
        else if (time >= 0.70 && time < 0.80) {
            // Dusk
            float progress = (float) ((time - 0.70) / 0.10);
            this.nightFactor = progress;
            this.dayFactor = 1.0f - progress;
        }
        else {
            // Nighttime
            this.nightFactor = 1.0f;
            this.dayFactor = 0.0f;
        }
    }


    public void renderAmbientEffectsAbove(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!ambientEffectsActive) return;
        Graphics2D g2d = (Graphics2D) g;
        if (stormPeakMode) {
            Arrays.stream(windParticles).forEach(p -> p.render(g2d, xLevelOffset, yLevelOffset, 1.0f));
            return;
        }
        if (nightFactor > 0.02f) {
            Arrays.stream(windParticles).forEach(p -> p.render(g2d, xLevelOffset, yLevelOffset, nightFactor));
        }
        if (dayFactor > 0.02f) {
            Arrays.stream(fallingLeafParticles).forEach(p -> p.render(g2d, xLevelOffset, yLevelOffset, dayFactor));
        }
    }

    public void renderAmbientEffects(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!ambientEffectsActive) return;
        Graphics2D g2d = (Graphics2D) g;
        Arrays.stream(smokeParticles).forEach(p -> p.render(g2d));
        if (nightFactor > 0.02f) {
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nightFactor));
            Arrays.stream(ambientParticles).forEach(p -> p.render(g2d));
            g2d.setComposite(old);
        }
    }

    public void renderForegroundEffects(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderParticleList(g, xLevelOffset, yLevelOffset, foregroundParticles);
        renderDamageNumbers(g, xLevelOffset, yLevelOffset);
        renderItemPickupTexts(g, xLevelOffset, yLevelOffset);
    }

    public void renderBackgroundEffects(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderParticleList(g, xLevelOffset, yLevelOffset, backgroundParticles);
    }

    private void renderParticleList(Graphics g, int xLevelOffset, int yLevelOffset, List<DustParticle> particles) {
        try {
            for (DustParticle particle : particles) {
                particle.render(g, xLevelOffset, yLevelOffset);
            }
        } catch (Exception ignored) { }
    }

    private void renderDamageNumbers(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            for (DamageNumber dn : damageNumbers) {
                dn.render(g, xLevelOffset, yLevelOffset);
            }
        } catch (Exception ignored) { }
    }

    private void renderItemPickupTexts(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            for (ItemPickupText text : itemPickupTexts) {
                text.render(g, xLevelOffset, yLevelOffset);
            }
        } catch (Exception ignored) {}
    }

    public void reset() {
        foregroundParticles.clear();
        backgroundParticles.clear();
        damageNumbers.clear();
        itemPickupTexts.clear();
        itemTextYOffset = 0;
        itemTextCooldown = 0;
        Arrays.stream(windParticles).forEach(p -> p.reset(true));
    }

    public void setAmbientEffectsActive(boolean ambientEffectsActive) {
        this.ambientEffectsActive = ambientEffectsActive;
    }

    public void setStormPeakMode(boolean stormPeakMode) {
        this.stormPeakMode = stormPeakMode;
        this.stormIntensity = 1.0;
        Arrays.stream(windParticles).forEach(p -> p.setStormPeakMode(stormPeakMode));
    }

    public void setStormIntensity(double stormIntensity) {
        this.stormIntensity = stormIntensity;
    }

}
