package platformer.animation;

import platformer.model.objects.Obj;
import platformer.utils.Utils;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class AnimationUtils {

    public static AnimationUtils instance = null;

    private AnimationUtils() {}

    // Animation loader
    private BufferedImage[] loadAnimation(String basePath, int frames, int row, int width, int height, int offset, int x, int y) {
        BufferedImage sprite = Utils.getInstance().importImage(basePath, -1, -1);
        BufferedImage[] animation = new BufferedImage[frames];
        int yOffset = y * row;
        for (int i = offset; i < frames+offset; i++) {
            animation[i-offset] = Utils.getInstance().resize(sprite.getSubimage(i * x, yOffset, x, y), width, height);
        }
        return animation;
    }

    // Player
    public static BufferedImage[][] loadPlayerAnimations(int w, int h, String sheet) {
        BufferedImage sprite = Utils.getInstance().importImage("/images/player/" + sheet + "Sheet.png", -1, -1);
        BufferedImage[][] anim = new BufferedImage[15][15];

        int[][] animationInfo = {
                // Frame count, Sheet row
                {8, 0},             // Idle anim 0
                {8, 80},            // Run anim 1
                {3, 6*80},          // Jump anim 2
                {3, 8*80},          // Fall anim 3
                {4, 10*80},         // Attack 1 anim 4
                {4, 11*80},         // Attack 2 anim 5
                {5, 12*80},         // Attack 3 anim 6
                {6, 14*80},         // Block anim 7
                {4, 23*80},         // Hit anim 8
                {11, 24*80},        // Death anim 9
                {0, 0},             // Skipping frame 10
                {0, 0},             // Skipping frame 11
                {4, 21*80},         // Wall anim 12
                {12, 20*80},        // Transform anim 13
                {13, 19*80}         // Spell 1 anim 14
        };

        for (int i = 0; i < anim.length; i++) {
            int frameCount = animationInfo[i][0];
            int sheetRow = animationInfo[i][1];
            BufferedImage[] animation = new BufferedImage[frameCount];
            for (int j = 0; j < frameCount; j++) {
                animation[j] = Utils.getInstance().resize(sprite.getSubimage(j * PLAYER_W, sheetRow, PLAYER_W, PLAYER_H), w, h);
            }
            anim[i] = animation;
        }

        return anim;
    }

    // Enemy
    public BufferedImage[][] loadSkeletonAnimations() {
        BufferedImage[][] anim = new BufferedImage[13][13];
        int w = SKELETON_WIDTH;
        int h = SKELETON_HEIGHT;

        anim[Anim.IDLE.ordinal()] = loadAnimation(SKELETON_SHEET, 4, 0, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadAnimation(SKELETON_SHEET, 8, 2, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.FALL.ordinal()] = loadAnimation(SKELETON_SHEET, 2, 3, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimation(SKELETON_SHEET, 6, 4, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.BLOCK.ordinal()] = loadAnimation(SKELETON_SHEET, 6, 5, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadAnimation(SKELETON_SHEET, 4, 7, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadAnimation(SKELETON_SHEET, 10, 6, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadAnimation(SKELETON_SHEET, 8, 1, w, h, 0, SKELETON_W, SKELETON_H);

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation() {
        BufferedImage[][] anim = new BufferedImage[17][20];
        int w = GHOUL_WIDTH;
        int h = GHOUL_HEIGHT;

        anim[Anim.IDLE.ordinal()] = loadAnimation(GHOUL_SHEET, 8, 0, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadAnimation(GHOUL_SHEET, 6, 2, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimation(GHOUL_SHEET, 8, 5, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadAnimation(GHOUL_SHEET, 4, 4, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadAnimation(GHOUL_SHEET, 4, 7, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadAnimation(GHOUL_SHEET, 6, 1, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIDE.ordinal()] = loadAnimation(GHOUL_SHEET, 19, 6, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.REVEAL.ordinal()] = loadAnimation(GHOUL_SHEET, 19, 6, w, h, 0, SKELETON_W, SKELETON_H);
        Utils.getInstance().reverseArray(anim[16]);

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations() {
        BufferedImage[][] anim = new BufferedImage[20][25];
        int w = SW_WIDTH;
        int h = SW_HEIGHT;

        anim[Anim.IDLE.ordinal()] = loadAnimation(SW_SHEET, 8, 0, w, h, 0, SW_W, SW_H);
        anim[Anim.RUN.ordinal()] = loadAnimation(SW_SHEET, 8, 2, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_1.ordinal()] = loadAnimation(SW_SHEET, 5, 10, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_2.ordinal()] = loadAnimation(SW_SHEET, 5, 11, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_3.ordinal()] = loadAnimation(SW_SHEET, 6, 12, w, h, 0, SW_W, SW_H);
        anim[Anim.BLOCK.ordinal()] = loadAnimation(SW_SHEET, 16, 16, w, h, 0, SW_W, SW_H);
        anim[Anim.HIT.ordinal()] = loadAnimation(SW_SHEET, 4, 23, w, h, 0, SW_W, SW_H);
        anim[Anim.DEATH.ordinal()] = loadAnimation(SW_SHEET, 9, 24, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_1.ordinal()] = loadAnimation(SW_SHEET, 14, 13, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_2.ordinal()] = loadAnimation(SW_SHEET, 11, 14, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_3.ordinal()] = loadAnimation(SW_SHEET, 22, 15, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_4.ordinal()] = loadAnimation(SW_SHEET, 2, 15, w, h, 1, SW_W, SW_H);

        return anim;
    }

    // Effects
    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        int index = 0;

        // Wall Slide
        BufferedImage sprite = Utils.getInstance().importImage("/images/particles/dustSprite.png", -1, -1);
        BufferedImage[] wallSlide = new BufferedImage[8];
        wallSlide[0] = sprite.getSubimage(0, 0, 1, 1);
        wallSlide[1] = sprite.getSubimage(0, 0, 1, 1);
        for (int i = 2; i < 8; i++) {
            wallSlide[i] = sprite.getSubimage(64*i+5120, 0, 64, 64);
            wallSlide[i] = Utils.getInstance().rotateImage(wallSlide[i], Math.PI/2);
        }
        anim[index] = wallSlide;

        return anim;
    }

    // Objects
    public BufferedImage[][] loadObjects() {
        BufferedImage[][] anim = new BufferedImage[17][17];

        anim[Obj.STAMINA_POTION.ordinal()] = loadAnimation(POTIONS_SHEET, 7, 0, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[Obj.HEAL_POTION.ordinal()] = loadAnimation(POTIONS_SHEET, 7, 1, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[Obj.BOX.ordinal()] = loadAnimation(CONTAINERS_SHEET, 8, 0, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[Obj.BARREL.ordinal()] = loadAnimation(CONTAINERS_SHEET, 8, 1, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[Obj.SPIKE.ordinal()] = loadAnimation(SPIKES_SHEET, 10, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        anim[Obj.ARROW_TRAP_RIGHT.ordinal()] = loadAnimation(ARROW_TRAP_SHEET, 16, 0, ARROW_TRAP_WID, ARROW_TRAP_HEI, 1, AT_W, AT_H);
        anim[Obj.ARROW_TRAP_LEFT.ordinal()] = anim[Obj.ARROW_TRAP_RIGHT.ordinal()];
        anim[Obj.COIN.ordinal()] = loadAnimation(COIN_SHEET, 4, 0, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
        anim[Obj.SHOP.ordinal()] = loadAnimation(SHOP_SHEET, 6, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H);
        anim[Obj.BLOCKER.ordinal()] = loadAnimation(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H);
        anim[Obj.BLACKSMITH.ordinal()] = loadAnimation(BS_SHEET, 8, 0, BLACKSMITH_WID, BLACKSMITH_HEI, 0, BLACKSMITH_W, BLACKSMITH_H);
        anim[Obj.DOG.ordinal()] = loadAnimation(DOG_SHEET, 8, 0, DOG_WID, DOG_HEI, 0, DOG_W, DOG_H);

        return anim;
    }

    // Spell
    public BufferedImage[] loadLightningAnimations() {
        return loadAnimation(LIGHTNING_SHEET, 8, 0, LIGHTNING_WIDTH, LIGHTNING_HEIGHT, 0, LIGHTNING_W, LIGHTNING_H);
    }

    public BufferedImage[] loadFlashAnimations() {
        return loadAnimation(FLASH_SHEET, 16, 0, FLASH_WIDTH, FLASH_HEIGHT, 0, FLASH_W, FLASH_H);
    }

    public BufferedImage[] loadLightningBall(int type) {
        String index = type == 1 ? "" : "2";
        BufferedImage sprite = Utils.getInstance().importImage("/images/objs/lightningBall"+index+".png", -1, -1);
        BufferedImage[] anim = new BufferedImage[9];
        for (int i = 0; i < anim.length; i++) {
            anim[i] = sprite.getSubimage(i*50, 0, 50, 50);
        }
        return anim;
    }

    // Other
    public BufferedImage[] loadMenuAnimation() {
        BufferedImage[] anim = new BufferedImage[24];
        for (int i = 0; i < 24; i++) {
            anim[i] = Utils.getInstance().importImage("/images/menu/background/Background"+i+".png", GAME_WIDTH, GAME_HEIGHT);
        }
        return anim;
    }

    public static AnimationUtils getInstance() {
        if (instance == null) {
            instance = new AnimationUtils();
        }
        return instance;
    }
}
