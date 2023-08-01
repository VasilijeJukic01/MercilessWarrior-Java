package platformer.animation;

import platformer.model.entities.effects.EffectType;
import platformer.model.entities.effects.Particle;
import platformer.model.objects.ObjType;
import platformer.utils.Utils;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class AnimUtils {

    private static volatile AnimUtils instance = null;

    private AnimUtils() {}

    public static AnimUtils getInstance() {
        if (instance == null) {
            synchronized (AnimUtils.class) {
                if (instance == null) {
                    instance = new AnimUtils();
                }
            }
        }
        return instance;
    }

    // Animation loader
    public BufferedImage[] loadFromSprite(String basePath, int frames, int row, int width, int height, int offset, int x, int y) {
        BufferedImage sprite = Utils.getInstance().importImage(basePath, -1, -1);
        BufferedImage[] animation = new BufferedImage[frames];
        int yOffset = y * row;
        for (int i = offset; i < frames+offset; i++) {
            animation[i-offset] = Utils.getInstance().resizeImage(sprite.getSubimage(i * x, yOffset, x, y), width, height);
        }
        return animation;
    }

    // Player
    public BufferedImage[][] loadPlayerAnimations(int w, int h, String sheet) {
        BufferedImage[][] anim = new BufferedImage[17][17];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(sheet, 8, 0, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(sheet, 8, 1, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.JUMP.ordinal()] = loadFromSprite(sheet, 3, 6, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(sheet, 3, 8, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(sheet, 4, 10, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(sheet, 4, 11, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(sheet, 5, 12, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(sheet, 6, 14, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(sheet, 4, 23, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(sheet, 11, 24, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.WALL.ordinal()] = loadFromSprite(sheet, 4, 21, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.TRANSFORM.ordinal()] = loadFromSprite(sheet, 12, 20, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(sheet, 13, 19, w, h, 0, PLAYER_W, PLAYER_H);

        return anim;
    }

    // Enemy
    public BufferedImage[][] loadSkeletonAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(SKELETON_SHEET, 4, 0, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SKELETON_SHEET, 8, 2, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(SKELETON_SHEET, 2, 3, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SKELETON_SHEET, 6, 4, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(SKELETON_SHEET, 6, 5, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(SKELETON_SHEET, 4, 7, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SKELETON_SHEET, 10, 6, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(SKELETON_SHEET, 8, 1, w, h, 0, SKELETON_W, SKELETON_H);

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[17][20];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 0, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(GHOUL_SHEET, 6, 2, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 5, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(GHOUL_SHEET, 4, 4, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(GHOUL_SHEET, 4, 7, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(GHOUL_SHEET, 6, 1, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIDE.ordinal()] = loadFromSprite(GHOUL_SHEET, 19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = loadFromSprite(GHOUL_SHEET, 19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        Utils.getInstance().reverseArray(anim[16]);

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[20][25];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(SW_SHEET, 8, 0, w, h, 0, SW_W, SW_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SW_SHEET, 8, 2, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SW_SHEET, 5, 10, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(SW_SHEET, 5, 11, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(SW_SHEET, 6, 12, w, h, 0, SW_W, SW_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(SW_SHEET, 16, 16, w, h, 0, SW_W, SW_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(SW_SHEET, 4, 23, w, h, 0, SW_W, SW_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SW_SHEET, 9, 24, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(SW_SHEET, 14, 13, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(SW_SHEET, 11, 14, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_3.ordinal()] = loadFromSprite(SW_SHEET, 22, 15, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_4.ordinal()] = loadFromSprite(SW_SHEET, 2, 15, w, h, 1, SW_W, SW_H);

        return anim;
    }

    // Effects
    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        anim[EffectType.WALL_SLIDE.ordinal()] = loadFromSprite(DUST_SHEET_1, 8, 0, DUST1_W, DUST1_H, 0, DUST1_W, DUST1_H);
        return anim;
    }

    public Particle[] loadParticles() {
        Particle[] particles = new Particle[PARTICLES_CAP];
        Random rand = new Random();
        for (int i = 0; i < particles.length; i++) {
            int size = (int)((rand.nextInt(15-5) + 5) * SCALE);
            int xPos = rand.nextInt(GAME_WIDTH-10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT-10) + 10;
            BufferedImage[] images = loadFromSprite(PARTICLE_SHEET, 8, 0, size, size, 0, PARTICLE_W, PARTICLE_H);
            particles[i] = new Particle(images, xPos, yPos);
        }
        return particles;
    }

    // Objects
    public BufferedImage[][] loadObjects() {
        BufferedImage[][] anim = new BufferedImage[17][17];

        anim[ObjType.STAMINA_POTION.ordinal()] = loadFromSprite(POTIONS_SHEET, 7, 0, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[ObjType.HEAL_POTION.ordinal()] = loadFromSprite(POTIONS_SHEET, 7, 1, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[ObjType.BOX.ordinal()] = loadFromSprite(CONTAINERS_SHEET, 8, 0, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[ObjType.BARREL.ordinal()] = loadFromSprite(CONTAINERS_SHEET, 8, 1, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[ObjType.SPIKE.ordinal()] = loadFromSprite(SPIKES_SHEET, 10, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        anim[ObjType.ARROW_TRAP_RIGHT.ordinal()] = loadFromSprite(ARROW_TRAP_SHEET, 16, 0, ARROW_TRAP_WID, ARROW_TRAP_HEI, 1, AT_W, AT_H);
        anim[ObjType.ARROW_TRAP_LEFT.ordinal()] = anim[ObjType.ARROW_TRAP_RIGHT.ordinal()];
        anim[ObjType.COIN.ordinal()] = loadFromSprite(COIN_SHEET, 4, 0, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
        anim[ObjType.SHOP.ordinal()] = loadFromSprite(SHOP_SHEET, 6, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H);
        anim[ObjType.BLOCKER.ordinal()] = loadFromSprite(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H);
        anim[ObjType.BLACKSMITH.ordinal()] = loadFromSprite(BS_SHEET, 8, 0, BLACKSMITH_WID, BLACKSMITH_HEI, 0, BLACKSMITH_W, BLACKSMITH_H);
        anim[ObjType.DOG.ordinal()] = loadFromSprite(DOG_SHEET, 8, 0, DOG_WID, DOG_HEI, 0, DOG_W, DOG_H);

        return anim;
    }

    public BufferedImage[] loadLightningBall(String sprite) {
        return AnimUtils.getInstance().loadFromSprite(sprite, 9, 0, LB_WID, LB_HEI, 0, LIGHTNING_BALL_W, LIGHTNING_BALL_H);
    }

}
