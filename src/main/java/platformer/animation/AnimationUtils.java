package platformer.animation;

import platformer.model.Tiles;
import platformer.model.entities.enemies.EnemySize;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;

public class AnimationUtils {

    public static AnimationUtils instance = null;

    private AnimationUtils() {}

    // Player
    public BufferedImage[][] loadPlayerAnimations(int w, int h, String sheet) {
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/player/"+sheet+"Sheet.png", -1, -1);
        BufferedImage[][] anim = new BufferedImage[16][16];

        // 0 Idle anim
        BufferedImage[] idleAnim = new BufferedImage[8];
        for (int i = 0; i < idleAnim.length; i++) {
            idleAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 0, 144, 80), w, h);
        }
        anim[0] = idleAnim;

        // 1 Run anim
        BufferedImage[] runAnim = new BufferedImage[8];
        for (int i = 0; i < runAnim.length; i++) {
            runAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 80, 144, 80), w, h);
        }
        anim[1] = runAnim;

        // 2 Jump anim
        BufferedImage[] jumpAnim = new BufferedImage[3];
        for (int i = 0; i < jumpAnim.length; i++) {
            jumpAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 6*80, 144, 80), w, h);
        }
        anim[2] = jumpAnim;

        // 3 Fall anim
        BufferedImage[] fallAnim = new BufferedImage[3];
        for (int i = 0; i < fallAnim.length; i++) {
            fallAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 8*80, 144, 80), w, h);
        }
        anim[3] = fallAnim;

        // 4 Attack 1 anim
        BufferedImage[] attack1 = new BufferedImage[4];
        for (int i = 0; i < attack1.length; i++) {
            attack1[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 10*80, 144, 80), w, h);
        }
        anim[4] = attack1;

        // 5 Attack 2 anim
        BufferedImage[] attack2 = new BufferedImage[4];
        for (int i = 0; i < attack2.length; i++) {
            attack2[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 11*80, 144, 80), w, h);
        }
        anim[5] = attack2;

        // 6 Attack 3 anim
        BufferedImage[]  attack3 = new BufferedImage[5];
        for (int i = 0; i < attack3.length; i++) {
            attack3[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 12*80, 144, 80), w, h);
        }
        anim[6] = attack3;

        // 7 Block anim
        BufferedImage[]  block = new BufferedImage[6];
        for (int i = 0; i < block.length; i++) {
            block[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 14*80, 144, 80), w, h);
        }
        anim[7] = block;

        // 8 Hit anim
        BufferedImage[] hitAnim = new BufferedImage[4];
        for (int i = 0; i < hitAnim.length; i++) {
            hitAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 23*80, 144, 80), w, h);
        }
        anim[8] = hitAnim;

        // 9 Death anim
        BufferedImage[] deathAnim = new BufferedImage[11];
        for (int i = 0; i < deathAnim.length; i++) {
            deathAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 24*80, 144, 80), w, h);
        }
        anim[9] = deathAnim;

        // 12 Wall anim
        BufferedImage[] wallAnim = new BufferedImage[4];
        for (int i = 0; i < wallAnim.length; i++) {
            wallAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 21*80, 144, 80), w, h);
        }
        anim[12] = wallAnim;

        // 13 Transform anim
        BufferedImage[] transformAnim = new BufferedImage[12];
        for (int i = 0; i < transformAnim.length; i++) {
            transformAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*144, 20*80, 144, 80), w, h);
        }
        anim[13] = transformAnim;

        // 14 Spell 1 anim
        BufferedImage[] spell1Anim = new BufferedImage[13];
        for (int i = 1; i < spell1Anim.length+1; i++) {
            spell1Anim[i-1] = Utils.getInstance().resize(sprite.getSubimage(i*144, 19*80, 144, 80), w, h);
        }
        anim[14] = spell1Anim;

        return anim;
    }

    // Enemy
    public BufferedImage[][] loadSkeletonAnimations() {
        BufferedImage[][] anim = new BufferedImage[13][13];

        // Size
        int w = EnemySize.SKELETON_WIDTH.getValue();
        int h = EnemySize.SKELETON_HEIGHT.getValue();

        // 0 Idle anim
        BufferedImage[] idleAnim = new BufferedImage[4];
        for (int i = 0; i < idleAnim.length; i++) {
            idleAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Idle/Skeleton_Idle"+i+".png", w, h);
        }
        anim[0] = idleAnim;

        // 1 Run anim
        BufferedImage[] runAnim = new BufferedImage[8];
        for (int i = 0; i < runAnim.length; i++) {
            runAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Run/Skeleton_Run"+i+".png", w, h);
        }
        anim[1] = runAnim;

        // 3 Fall anim
        BufferedImage[] fallAnim = new BufferedImage[2];
        for (int i = 0; i < fallAnim.length; i++) {
            fallAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/JumpFall/Skeleton_Fall"+i+".png", w, h);
        }
        anim[3] = fallAnim;

        // 4 Attack anim
        BufferedImage[] attack = new BufferedImage[6];
        for (int i = 0; i < attack.length; i++) {
            attack[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Attack/Skeleton_Attack"+i+".png", w, h);
        }
        anim[4] = attack;

        // 7 Block anim
        BufferedImage[] block = new BufferedImage[6];
        for (int i = 0; i < block.length; i++) {
            block[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Block/Skeleton_Block"+i+".png", w, h);
        }
        anim[7] = block;

        // 8 Hit anim
        BufferedImage[] hit = new BufferedImage[4];
        for (int i = 0; i < hit.length; i++) {
            hit[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Hit/Skeleton_Hit"+i+".png", w, h);
        }
        anim[8] = hit;

        // 9 Death anim
        BufferedImage[] death = new BufferedImage[10];
        for (int i = 0; i < death.length; i++) {
            death[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Death/Skeleton_Death"+i+".png", w, h);
        }
        anim[9] = death;

        // 11 Walk anim
        BufferedImage[] walk = new BufferedImage[8];
        for (int i = 0; i < walk.length; i++) {
            walk[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/Walk/Skeleton_Walk"+i+".png", w, h);
        }
        anim[11] = walk;

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation() {
        BufferedImage[][] anim = new BufferedImage[17][20];

        // Size
        int w = EnemySize.GHOUL_WIDTH.getValue();
        int h = EnemySize.GHOUL_HEIGHT.getValue();

        // 0 Idle anim
        BufferedImage[] idleAnim = new BufferedImage[8];
        for (int i = 0; i < idleAnim.length; i++) {
            idleAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Idle/Ghoul_Idle"+i+".png", w, h);
        }
        anim[0] = idleAnim;

        // 1 Chase anim
        BufferedImage[] chaseAnim = new BufferedImage[6];
        for (int i = 0; i < chaseAnim.length; i++) {
            chaseAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Chase/Ghoul_Chase"+i+".png", w, h);
        }
        anim[1] = chaseAnim;

        // 4 Attack anim
        BufferedImage[] attackAnim = new BufferedImage[8];
        for (int i = 0; i < attackAnim.length; i++) {
            attackAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Attack/Ghoul_Attack"+i+".png", w, h);
        }
        anim[4] = attackAnim;

        // 8 Hit anim
        BufferedImage[] hitAnim = new BufferedImage[4];
        for (int i = 0; i < hitAnim.length; i++) {
            hitAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Hit/Ghoul_Hit"+i+".png", w, h);
        }
        anim[8] = hitAnim;

        // 9 Death anim
        BufferedImage[] deathAnim = new BufferedImage[8];
        for (int i = 0; i < deathAnim.length; i++) {
            deathAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Death/Ghoul_Death"+i+".png", w, h);
        }
        anim[9] = deathAnim;

        // 11 Move anim
        BufferedImage[] moveAnim = new BufferedImage[6];
        for (int i = 0; i < moveAnim.length; i++) {
            moveAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Move/Ghoul_Move"+i+".png", w, h);
        }
        anim[11] = moveAnim;

        // 15 Hide anim
        BufferedImage[] hideAnim = new BufferedImage[19];
        for (int i = 0; i < hideAnim.length; i++) {
            hideAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Exile/Ghoul_Exile"+i+".png", w, h);
        }
        anim[15] = hideAnim;

        // 16 Reveal anim
        BufferedImage[] revelAnim = new BufferedImage[19];
        for (int i = revelAnim.length-1; i >= 0; i--) {
            revelAnim[revelAnim.length-1-i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Ghoul/Exile/Ghoul_Exile"+i+".png", w, h);
        }
        anim[16] = revelAnim;

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations() {
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/enemies/Bosses/SpearWoman.png", -1, -1);
        BufferedImage[][] anim = new BufferedImage[20][25];

        // Size
        int w = EnemySize.SW_WIDTH.getValue();
        int h = EnemySize.SW_HEIGHT.getValue();

        // 0 Idle anim
        BufferedImage[] idleAnim = new BufferedImage[8];
        for (int i = 0; i < idleAnim.length; i++) {
            idleAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 0, 128, 115), w, h);
        }
        anim[0] = idleAnim;

        // 1 Run anim
        BufferedImage[] runAnim = new BufferedImage[8];
        for (int i = 0; i < runAnim.length; i++) {
            runAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*2, 128, 115), w, h);
        }
        anim[1] = runAnim;

        // 4 Attack 1 anim
        BufferedImage[] attack1Anim = new BufferedImage[5];
        for (int i = 0; i < attack1Anim.length; i++) {
            attack1Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*10, 128, 115), w, h);
        }
        anim[4] = attack1Anim;

        // 5 Attack 2 anim
        BufferedImage[] attack2Anim = new BufferedImage[5];
        for (int i = 0; i < attack2Anim.length; i++) {
            attack2Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*11, 128, 115), w, h);
        }
        anim[5] = attack2Anim;

        // 6 Attack 3 anim
        BufferedImage[] attack3Anim = new BufferedImage[6];
        for (int i = 0; i < attack3Anim.length; i++) {
            attack3Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*12, 128, 115), w, h);
        }
        anim[6] = attack3Anim;

        // 7 Block anim
        BufferedImage[] blockAnim = new BufferedImage[16];
        for (int i = 0; i < blockAnim.length; i++) {
            blockAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*16, 128, 115), w, h);
        }
        anim[7] = blockAnim;

        // 8 Hit anim
        BufferedImage[] hitAnim = new BufferedImage[4];
        for (int i = 0; i < hitAnim.length; i++) {
            hitAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*23, 128, 115), w, h);
        }
        anim[8] = hitAnim;

        // 9 Death anim
        BufferedImage[] deathAnim = new BufferedImage[9];
        for (int i = 0; i < deathAnim.length; i++) {
            deathAnim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*24, 128, 115), w, h);
        }
        anim[9] = deathAnim;

        // 14 Spell 1 anim
        BufferedImage[] spell1Anim = new BufferedImage[14];
        for (int i = 0; i < spell1Anim.length; i++) {
            spell1Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*13, 128, 115), w, h);
        }
        anim[14] = spell1Anim;

        // 17 Spell 2 anim
        BufferedImage[] spell2Anim = new BufferedImage[11];
        for (int i = 0; i < spell2Anim.length; i++) {
            spell2Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*14, 128, 115), w, h);
        }
        anim[17] = spell2Anim;

        // 18 Spell 3 anim
        BufferedImage[] spell3Anim = new BufferedImage[22];
        for (int i = 0; i < spell3Anim.length; i++) {
            spell3Anim[i] = Utils.getInstance().resize(sprite.getSubimage(i*128, 115*15, 128, 115), w, h);
        }
        anim[18] = spell3Anim;

        // 19 Spell 4 anim
        BufferedImage[] spell4Anim = new BufferedImage[2];
        for (int i = 0; i < spell4Anim.length; i++) {
            spell4Anim[i] = Utils.getInstance().resize(sprite.getSubimage((i+1)*128, 115*15, 128, 115), w, h);
        }
        anim[19] = spell4Anim;

        return anim;
    }

    // Effects
    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        int index = 0;

        // Double Jump Effect
        BufferedImage[] doubleJump = new BufferedImage[4];
        int djW = (int)(57.5*Tiles.SCALE.getValue());
        int djH = (int)(25*Tiles.SCALE.getValue());
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

    public BufferedImage[] loadLightningBall() {
        BufferedImage sprite = Utils.getInstance().importImage("src/main/resources/images/objs/lightningBall.png", -1, -1);
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
            anim[i] = Utils.getInstance().importImage("src/main/resources/images/menu/background/Background"+i+".png", (int)Tiles.GAME_WIDTH.getValue(),
                    (int)Tiles.GAME_HEIGHT.getValue());
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
