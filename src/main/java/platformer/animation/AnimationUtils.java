package platformer.animation;

import platformer.model.entities.effects.EffectType;
import platformer.model.entities.effects.Particle;
import platformer.model.objects.Obj;
import platformer.utils.Utils;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class AnimationUtils {

    public static volatile AnimationUtils instance = null;

    private AnimationUtils() {}

    public static AnimationUtils getInstance() {
        if (instance == null) {
            synchronized (AnimationUtils.class) {
                if (instance == null) {
                    instance = new AnimationUtils();
                }
            }
        }
        return instance;
    }

    // Animation loader
    private BufferedImage[] loadAnimFromSprite(String basePath, int frames, int row, int width, int height, int offset, int x, int y) {
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

        anim[Anim.IDLE.ordinal()] = loadAnimFromSprite(sheet, 8, 0, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.RUN.ordinal()] = loadAnimFromSprite(sheet, 8, 1, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.JUMP.ordinal()] = loadAnimFromSprite(sheet, 3, 6, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.FALL.ordinal()] = loadAnimFromSprite(sheet, 3, 8, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimFromSprite(sheet, 4, 10, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_2.ordinal()] = loadAnimFromSprite(sheet, 4, 11, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_3.ordinal()] = loadAnimFromSprite(sheet, 5, 12, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.BLOCK.ordinal()] = loadAnimFromSprite(sheet, 6, 14, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.HIT.ordinal()] = loadAnimFromSprite(sheet, 4, 23, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.DEATH.ordinal()] = loadAnimFromSprite(sheet, 11, 24, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.WALL.ordinal()] = loadAnimFromSprite(sheet, 4, 21, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.TRANSFORM.ordinal()] = loadAnimFromSprite(sheet, 12, 20, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_1.ordinal()] = loadAnimFromSprite(sheet, 13, 19, w, h, 0, PLAYER_W, PLAYER_H);

        return anim;
    }

    // Enemy
    public BufferedImage[][] loadSkeletonAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 4, 0, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 8, 2, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.FALL.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 2, 3, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 6, 4, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.BLOCK.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 6, 5, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 4, 7, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 10, 6, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadAnimFromSprite(SKELETON_SHEET, 8, 1, w, h, 0, SKELETON_W, SKELETON_H);

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[17][20];

        anim[Anim.IDLE.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 8, 0, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.RUN.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 6, 2, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 8, 5, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIT.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 4, 4, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.DEATH.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 4, 7, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.WALK.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 6, 1, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIDE.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = loadAnimFromSprite(GHOUL_SHEET, 19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        Utils.getInstance().reverseArray(anim[16]);

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[20][25];

        anim[Anim.IDLE.ordinal()] = loadAnimFromSprite(SW_SHEET, 8, 0, w, h, 0, SW_W, SW_H);
        anim[Anim.RUN.ordinal()] = loadAnimFromSprite(SW_SHEET, 8, 2, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimFromSprite(SW_SHEET, 5, 10, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_2.ordinal()] = loadAnimFromSprite(SW_SHEET, 5, 11, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_3.ordinal()] = loadAnimFromSprite(SW_SHEET, 6, 12, w, h, 0, SW_W, SW_H);
        anim[Anim.BLOCK.ordinal()] = loadAnimFromSprite(SW_SHEET, 16, 16, w, h, 0, SW_W, SW_H);
        anim[Anim.HIT.ordinal()] = loadAnimFromSprite(SW_SHEET, 4, 23, w, h, 0, SW_W, SW_H);
        anim[Anim.DEATH.ordinal()] = loadAnimFromSprite(SW_SHEET, 9, 24, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_1.ordinal()] = loadAnimFromSprite(SW_SHEET, 14, 13, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_2.ordinal()] = loadAnimFromSprite(SW_SHEET, 11, 14, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_3.ordinal()] = loadAnimFromSprite(SW_SHEET, 22, 15, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_4.ordinal()] = loadAnimFromSprite(SW_SHEET, 2, 15, w, h, 1, SW_W, SW_H);

        return anim;
    }

    // Effects
    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        anim[EffectType.WALL_SLIDE.ordinal()] = loadAnimFromSprite(DUST_SHEET_1, 8, 0, DUST1_W, DUST1_H, 0, DUST1_W, DUST1_H);
        return anim;
    }

    public Particle[] loadParticles() {
        Particle[] particles = new Particle[PARTICLES_CAP];
        Random rand = new Random();
        for (int i = 0; i < particles.length; i++) {
            int size = (int)((rand.nextInt(15-5) + 5) * SCALE);
            int xPos = rand.nextInt(GAME_WIDTH-10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT-10) + 10;
            BufferedImage[] images = loadAnimFromSprite(PARTICLE_SHEET, 8, 0, size, size, 0, PARTICLE_W, PARTICLE_H);
            particles[i] = new Particle(images, xPos, yPos);
        }
        return particles;
    }

    // Objects
    public BufferedImage[][] loadObjects() {
        BufferedImage[][] anim = new BufferedImage[17][17];

        anim[Obj.STAMINA_POTION.ordinal()] = loadAnimFromSprite(POTIONS_SHEET, 7, 0, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[Obj.HEAL_POTION.ordinal()] = loadAnimFromSprite(POTIONS_SHEET, 7, 1, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[Obj.BOX.ordinal()] = loadAnimFromSprite(CONTAINERS_SHEET, 8, 0, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[Obj.BARREL.ordinal()] = loadAnimFromSprite(CONTAINERS_SHEET, 8, 1, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[Obj.SPIKE.ordinal()] = loadAnimFromSprite(SPIKES_SHEET, 10, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        anim[Obj.ARROW_TRAP_RIGHT.ordinal()] = loadAnimFromSprite(ARROW_TRAP_SHEET, 16, 0, ARROW_TRAP_WID, ARROW_TRAP_HEI, 1, AT_W, AT_H);
        anim[Obj.ARROW_TRAP_LEFT.ordinal()] = anim[Obj.ARROW_TRAP_RIGHT.ordinal()];
        anim[Obj.COIN.ordinal()] = loadAnimFromSprite(COIN_SHEET, 4, 0, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
        anim[Obj.SHOP.ordinal()] = loadAnimFromSprite(SHOP_SHEET, 6, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H);
        anim[Obj.BLOCKER.ordinal()] = loadAnimFromSprite(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H);
        anim[Obj.BLACKSMITH.ordinal()] = loadAnimFromSprite(BS_SHEET, 8, 0, BLACKSMITH_WID, BLACKSMITH_HEI, 0, BLACKSMITH_W, BLACKSMITH_H);
        anim[Obj.DOG.ordinal()] = loadAnimFromSprite(DOG_SHEET, 8, 0, DOG_WID, DOG_HEI, 0, DOG_W, DOG_H);

        return anim;
    }

    // Spell
    public BufferedImage[] loadLightningAnimations() {
        return loadAnimFromSprite(LIGHTNING_SHEET, 8, 0, LIGHTNING_WIDTH, LIGHTNING_HEIGHT, 0, LIGHTNING_W, LIGHTNING_H);
    }

    public BufferedImage[] loadFlashAnimations() {
        return loadAnimFromSprite(FLASH_SHEET, 16, 0, FLASH_WIDTH, FLASH_HEIGHT, 0, FLASH_W, FLASH_H);
    }

    public BufferedImage[] loadLightningBall(String sprite) {
        return loadAnimFromSprite(sprite, 9, 0, LB_WID, LB_HEI, 0, LIGHTNING_BALL_W, LIGHTNING_BALL_H);
    }

    // Other
    public BufferedImage[] loadMenuAnimation() {
        BufferedImage[] anim = new BufferedImage[24];
        for (int i = 0; i < 24; i++) {
            anim[i] = Utils.getInstance().importImage("/images/menu/background/Background"+i+".png", GAME_WIDTH, GAME_HEIGHT);
        }
        return anim;
    }

}
