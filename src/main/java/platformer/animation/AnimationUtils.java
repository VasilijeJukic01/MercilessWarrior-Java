package platformer.animation;

import platformer.utils.Utils;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class AnimationUtils {

    public static AnimationUtils instance = null;

    private AnimationUtils() {}

    private BufferedImage[] loadAnimation(String basePath, int numFrames, int width, int height, boolean reverse) {
        BufferedImage[] animation = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            int index = reverse ? numFrames - 1 - i : i;
            String imagePath = basePath + index + ".png";
            animation[i] = Utils.getInstance().importImage(imagePath, width, height);
        }
        return animation;
    }

    private BufferedImage[] loadAnimationWithSprite(String basePath, int numFrames, int row, int width, int height, int offset) {
        BufferedImage sprite = Utils.getInstance().importImage(basePath, -1, -1);
        BufferedImage[] animation = new BufferedImage[numFrames];
        int yOffset = 115 * row;
        for (int i = offset; i < numFrames+offset; i++) {
            animation[i-offset] = Utils.getInstance().resize(sprite.getSubimage(i * 128, yOffset, 128, 115), width, height);
        }
        return animation;
    }

    // Player
    public static BufferedImage[][] loadPlayerAnimations(int w, int h, String sheet) {
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/player/" + sheet + "Sheet.png", -1, -1);
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
                animation[j] = Utils.getInstance().resize(sprite.getSubimage(j * 144, sheetRow, 144, 80), w, h);
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

        anim[0] = loadAnimation("src/main/resources/images/enemies/Skeleton/Idle/Skeleton_Idle", 4, w, h, false);
        anim[1] = loadAnimation("src/main/resources/images/enemies/Skeleton/Run/Skeleton_Run", 8, w, h, false);
        anim[3] = loadAnimation("src/main/resources/images/enemies/Skeleton/JumpFall/Skeleton_Fall", 2, w, h, false);
        anim[4] = loadAnimation("src/main/resources/images/enemies/Skeleton/Attack/Skeleton_Attack", 6, w, h, false);
        anim[7] = loadAnimation("src/main/resources/images/enemies/Skeleton/Block/Skeleton_Block", 6, w, h, false);
        anim[8] = loadAnimation("src/main/resources/images/enemies/Skeleton/Hit/Skeleton_Hit", 4, w, h, false);
        anim[9] = loadAnimation("src/main/resources/images/enemies/Skeleton/Death/Skeleton_Death", 10, w, h, false);
        anim[11] = loadAnimation("src/main/resources/images/enemies/Skeleton/Walk/Skeleton_Walk", 8, w, h, false);

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation() {
        BufferedImage[][] anim = new BufferedImage[17][20];
        int w = GHOUL_WIDTH;
        int h = GHOUL_HEIGHT;

        anim[0] = loadAnimation("src/main/resources/images/enemies/Ghoul/Idle/Ghoul_Idle", 8, w, h, false);
        anim[1] = loadAnimation("src/main/resources/images/enemies/Ghoul/Chase/Ghoul_Chase", 6, w, h, false);
        anim[4] = loadAnimation("src/main/resources/images/enemies/Ghoul/Attack/Ghoul_Attack", 8, w, h, false);
        anim[8] = loadAnimation("src/main/resources/images/enemies/Ghoul/Hit/Ghoul_Hit", 4, w, h, false);
        anim[9] = loadAnimation("src/main/resources/images/enemies/Ghoul/Death/Ghoul_Death", 8, w, h, false);
        anim[11] = loadAnimation("src/main/resources/images/enemies/Ghoul/Move/Ghoul_Move", 6, w, h, false);
        anim[15] = loadAnimation("src/main/resources/images/enemies/Ghoul/Exile/Ghoul_Exile", 19, w, h, false);
        anim[16] = loadAnimation("src/main/resources/images/enemies/Ghoul/Exile/Ghoul_Exile", 19, w, h, true);

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations() {
        BufferedImage[][] anim = new BufferedImage[20][25];
        int w = SW_WIDTH;
        int h = SW_HEIGHT;

        anim[0] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 8, 0, w, h, 0);
        anim[1] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 8, 2, w, h, 0);
        anim[4] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 5, 10, w, h, 0);
        anim[5] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 5, 11, w, h, 0);
        anim[6] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 6, 12, w, h, 0);
        anim[7] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 16, 16, w, h, 0);
        anim[8] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 4, 23, w, h, 0);
        anim[9] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 9, 24, w, h, 0);
        anim[14] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 14, 13, w, h, 0);
        anim[17] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 11, 14, w, h, 0);
        anim[18] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 22, 15, w, h, 0);
        anim[19] = loadAnimationWithSprite("src/main/resources/images/enemies/Bosses/SpearWoman.png", 2, 15, w, h, 1);

        return anim;
    }

    // Effects
    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        int index = 0;

        // Double Jump Effect
        BufferedImage[] doubleJump = new BufferedImage[4];
        int djW = (int)(57.5*SCALE);
        int djH = (int)(25*SCALE);
        for (int i = 0; i < 4; i++) {
            doubleJump[i] = Utils.getInstance().importImage("src/main/resources/images/player/DoubleJump/doubleJump"+i+".png", djW, djH);
        }
        anim[index++] = doubleJump;

        // Wall Slide
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/particles/dustSprite.png", -1, -1);
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
        int index = 0;

        // Potions 0, 1
        BufferedImage potionSprite = Utils.getInstance().importImage("src/main/resources/images/objs/potions_sprites.png", -1, -1);
        for (int i = 0; i < 2; i++, index++) {
            for (int j = 0; j < 7; j++) {
                anim[index][j] = potionSprite.getSubimage(12*j, 16*i, 12, 16);
            }
        }

        // Containers 2, 3
        BufferedImage containerSprite = Utils.getInstance().importImage("src/main/resources/images/objs/objects_sprites.png", -1, -1);
        for (int i = 0; i < 2; i++, index++) {
            for (int j = 0; j < 8; j++) {
                anim[index][j] = containerSprite.getSubimage(40*j, 30*i, 40, 30);
            }
        }

        // Spikes 4
        BufferedImage spikeSprite = Utils.getInstance().importImage("src/main/resources/images/objs/spikes.png", -1, -1);
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 10; j++) {
                anim[index][j] = spikeSprite.getSubimage(32*j, 32*i, 32, 32);
            }
        }

        // Arrow Launchers 5
        BufferedImage arrowLauncherSprite = Utils.getInstance().importImage("src/main/resources/images/objs/arrowTrap.png", -1, -1);
        for (int i = 0; i < 1; i++, index+=2) {
            for (int j = 0; j < 16; j++) {
                anim[index][j] = arrowLauncherSprite.getSubimage((96*j)+27, 32*i, 32, 32);
            }
        }

        // Coins 7
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 4; j++) {
                anim[index][j] = Utils.getInstance().importImage("src/main/resources/images/objs/coin/Coin"+j+".png", -1, -1);
            }
        }

        // Shop 8
        BufferedImage shopSprite = Utils.getInstance().importImage("src/main/resources/images/objs/shop.png", -1, -1);
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 6; j++) {
                anim[index][j] = shopSprite.getSubimage((118*j), 128*i, 118, 128);
            }
        }

        // Blocker 9
        BufferedImage blockerSprite = Utils.getInstance().importImage("src/main/resources/images/objs/blocker.png", -1, -1);
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 12; j++) {
                anim[index][j] = blockerSprite.getSubimage((96*j), 0, 96, 96);
            }
        }

        // Blacksmith 10
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 8; j++) {
                anim[index][j] = Utils.getInstance().importImage("src/main/resources/images/objs/blacksmith/Idle"+j+".png", -1, -1);
                anim[index][j] = Utils.getInstance().flipImage(anim[index][j]);
            }
        }

        // Dog 11
        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 8; j++) {
                anim[index][j] = Utils.getInstance().importImage("src/main/resources/images/objs/dog/Idle"+j+".png", -1, -1);
                anim[index][j] = Utils.getInstance().flipImage(anim[index][j]);
            }
        }

        return anim;
    }

    // Spell
    public BufferedImage[] loadLightningAnimations() {
        BufferedImage[] anim = new BufferedImage[8];
        for (int i = 0; i < anim.length; i++) {
            anim[i] = Utils.getInstance().importImage("src/main/resources/images/spells/Lightning"+(i+1)+".png", -1, -1);
        }
        return anim;
    }

    public BufferedImage[] loadFlashAnimations() {
        BufferedImage[] anim = new BufferedImage[17];
        for (int i = 0; i < anim.length; i++) {
            anim[i] = Utils.getInstance().importImage("src/main/resources/images/spells/flash/Flash"+i+".png", -1, -1);
            anim[i] = Utils.getInstance().rotateImage(anim[i], Math.PI/2);
        }
        return anim;
    }

    public BufferedImage[] loadLightningBall(int type) {
        String index = type == 1 ? "" : "2";
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/objs/lightningBall"+index+".png", -1, -1);
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
            anim[i] = Utils.getInstance().importImage("src/main/resources/images/menu/background/Background"+i+".png", GAME_WIDTH, GAME_HEIGHT);
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
