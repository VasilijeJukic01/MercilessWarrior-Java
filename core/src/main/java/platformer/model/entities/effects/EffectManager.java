package platformer.model.entities.effects;

import platformer.model.entities.Entity;
import platformer.model.entities.effects.particles.DustParticle;
import platformer.model.entities.effects.particles.DustType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class EffectManager {

    private final List<DustParticle> dustParticles = new ArrayList<>();
    private final Random rand = new Random();

    public void spawnDustParticles(double x, double y, int count, DustType type, int flipSign, Entity target) {
        for (int i = 0; i < count; i++) {
            int size = 0;
            double yOffset = 0;
            switch (type) {
                case IMPACT:
                    size = (int) ((rand.nextInt(6) + 3) * SCALE);
                    break;
                case RUNNING:
                    size = (int) ((rand.nextInt(5) + 4) * SCALE);
                    break;
                case DASH:
                    yOffset = (rand.nextDouble() - 0.5) * (target.getHitBox().height * 0.8);
                    size = (int) ((rand.nextInt(4) + 4) * SCALE);
                    break;
                case IMPACT_SPARK:
                    size = (int) ((rand.nextInt(3) + 2) * SCALE);
                    break;
                case SW_TELEPORT:
                    size = (int) ((rand.nextInt(10) + 5) * SCALE);
                    break;
                case SW_CHANNELING_AURA:
                case SW_AURA_PULSE:
                    size = (int)((rand.nextInt(3) + 3)  *  SCALE);
                    break;
                case SW_AURA_CRACKLE:
                    size = (int)((rand.nextInt(2) + 1) * SCALE);
                    break;
            }
            dustParticles.add(new DustParticle((int) x, (int) (y + yOffset), size, type, flipSign, target));
        }
    }

    public void spawnAura(Entity target, int count) {
        if (isAuraActive(target)) return;
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count / 2, DustType.SW_AURA_PULSE, 0, target);
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count, DustType.SW_CHANNELING_AURA, 0, target);
        spawnDustParticles(target.getHitBox().getCenterX(), target.getHitBox().getCenterY(), count, DustType.SW_AURA_CRACKLE, 0, target);
    }

    public void clearAura(Entity target) {
        dustParticles.removeIf(p -> p.getTarget() == target && (p.getType() == DustType.SW_CHANNELING_AURA || p.getType() == DustType.SW_AURA_PULSE || p.getType() == DustType.SW_AURA_CRACKLE));
    }

    public boolean isAuraActive(Entity target) {
        return dustParticles.stream()
                .anyMatch(p -> p.getTarget() == target && (p.getType() == DustType.SW_CHANNELING_AURA || p.getType() == DustType.SW_AURA_PULSE || p.getType() == DustType.SW_AURA_CRACKLE));
    }

    // Core
    public void update() {
        try {
            Iterator<DustParticle> pIterator = dustParticles.iterator();
            while (pIterator.hasNext()) {
                DustParticle p = pIterator.next();
                p.update();
                if (!p.isActive()) pIterator.remove();
            }
        } catch (Exception ignored) {}
    }

    public void renderForegroundEffects(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            dustParticles.forEach(particle -> particle.render(g, xLevelOffset, yLevelOffset));
        } catch (Exception ignored) { }
    }

}
