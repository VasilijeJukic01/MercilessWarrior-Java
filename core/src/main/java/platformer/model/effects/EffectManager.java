package platformer.model.effects;

import platformer.animation.Animation;
import platformer.core.Framework;
import platformer.core.Settings;
import platformer.model.effects.particles.*;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.ui.text.DamageNumber;
import platformer.ui.text.ItemPickupText;

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
    private final AmbientParticle[] ambientParticles;
    private final SmokeParticle[] smokeParticles;
    private final AmbientParticleFactory ambientParticleFactory;

    public EffectManager() {
        this.ambientParticleFactory = new AmbientParticleFactory();
        this.ambientParticles = loadDustParticles();
        this.smokeParticles = loadSmokeParticles();
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
            String key = "DefaultParticle";
            BufferedImage[] images = Animation.getInstance().loadFromSprite(PARTICLE_SHEET, DEFAULT_PARTICLE_FRAMES, 0, size, size, 0, PARTICLE_W, PARTICLE_H);
            AmbientParticleType ambientParticleType = ambientParticleFactory.getParticleImage(key, images);
            fireflies[i] = new AmbientParticle(ambientParticleType, size, xPos, yPos);
        }
        return fireflies;
    }

    private SmokeParticle[] loadSmokeParticles() {
        int smokeCount = PARTICLES_CAP / 2;
        SmokeParticle[] smoke = new SmokeParticle[smokeCount];
        BufferedImage[] smokeFrames = Animation.getInstance().loadFromSprite(SMOKE_SHEET, 1, 0, SMOKE_W, SMOKE_H, 0, SMOKE_W, SMOKE_H);
        for (int i = 0; i < smoke.length; i++) {
            smoke[i] = new SmokeParticle(smokeFrames);
        }
        return smoke;
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

    // Core
    public void update() {
        if (itemTextCooldown > 0) itemTextCooldown--;
        else itemTextYOffset = 0;
        updateParticleList(foregroundParticles);
        updateParticleList(backgroundParticles);
        updateDamageNumbers();
        updateItemPickupTexts();
        Arrays.stream(ambientParticles).forEach(AmbientParticle::update);
        Arrays.stream(smokeParticles).forEach(SmokeParticle::update);
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

    public void renderAmbientEffects(Graphics g) {
        Arrays.stream(smokeParticles).forEach(p -> p.render((Graphics2D) g));
        Arrays.stream(ambientParticles).forEach(p -> p.render(g));
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

}
