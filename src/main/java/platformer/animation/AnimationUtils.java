package platformer.animation;

import platformer.model.Tiles;
import platformer.model.entities.enemies.EnemySize;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;

public class AnimationUtils {

    public static AnimationUtils instance = null;

    private AnimationUtils() {}

    public BufferedImage[][] loadPlayerAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][13];

        // 0 Idle anim
        BufferedImage[] idleAnim = new BufferedImage[8];
        for (int i = 0; i < idleAnim.length; i++) {
            idleAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Idle/Fire_Warrior_Idle"+(i+1)+".png", w, h);
        }
        anim[0] = idleAnim;

        // 1 Run anim
        BufferedImage[] runAnim = new BufferedImage[8];
        for (int i = 0; i < runAnim.length; i++) {
            runAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Run/Fire_Warrior_Run"+(i+1)+".png", w, h);
        }
        anim[1] = runAnim;

        // 2 Jump anim
        BufferedImage[] jumpAnim = new BufferedImage[3];
        for (int i = 0; i < jumpAnim.length; i++) {
            jumpAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Jump/Fire_Warrior_Jump"+(i+1)+".png", w, h);
        }
        anim[2] = jumpAnim;

        // 3 Fall anim
        BufferedImage[] fallAnim = new BufferedImage[3];
        for (int i = 0; i < fallAnim.length; i++) {
            fallAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Jump/Fire_Warrior_Fall"+(i+1)+".png", w, h);
        }
        anim[3] = fallAnim;

        // 4 Attack 1 anim
        BufferedImage[] attack1 = new BufferedImage[4];
        for (int i = 0; i < attack1.length; i++) {
            attack1[i] = Utils.getInstance().importImage("src/main/resources/images/player/Attack_1/Fire_Warrior_Attack1_"+(i+1)+".png", w, h);
        }
        anim[4] = attack1;

        // 5 Attack 2 anim
        BufferedImage[] attack2 = new BufferedImage[4];
        for (int i = 0; i < attack2.length; i++) {
            attack2[i] = Utils.getInstance().importImage("src/main/resources/images/player/Attack_2/Fire_Warrior_Attack2_"+(i+1)+".png", w, h);
        }
        anim[5] = attack2;

        // 6 Attack 3 anim
        BufferedImage[]  attack3 = new BufferedImage[5];
        for (int i = 0; i < attack3.length; i++) {
            attack3[i] = Utils.getInstance().importImage("src/main/resources/images/player/Attack_3/Fire_Warrior_Attack3_"+(i+1)+".png", w, h);
        }
        anim[6] = attack3;

        // 8 Hit anim
        BufferedImage[] hitAnim = new BufferedImage[4];
        for (int i = 0; i < hitAnim.length; i++) {
            hitAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Hit/Fire_Warrior_Hit"+(i+1)+".png", w, h);
        }
        anim[8] = hitAnim;

        // 9 Death anim
        BufferedImage[] deathAnim = new BufferedImage[11];
        for (int i = 0; i < deathAnim.length; i++) {
            deathAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/Death/Fire_Warrior_Death"+(i+1)+".png", w, h);
        }
        anim[9] = deathAnim;

        // 12 Wall anim
        BufferedImage[] wallAnim = new BufferedImage[4];
        for (int i = 0; i < wallAnim.length; i++) {
            wallAnim[i] = Utils.getInstance().importImage("src/main/resources/images/player/WallHang/Fire_Warrior_WallHang"+(i+1)+".png", w, h);
        }
        anim[12] = wallAnim;

        return anim;
    }

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

        // 2 Jump anim
        BufferedImage[] jumpAnim = new BufferedImage[3];
        for (int i = 0; i < jumpAnim.length; i++) {
            jumpAnim[i] = Utils.getInstance().importImage("src/main/resources/images/enemies/Skeleton/JumpFall/Skeleton_Jump"+i+".png", w, h);
        }
        anim[2] = jumpAnim;

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

    public BufferedImage[][] loadEffects() {
        BufferedImage[][] anim = new BufferedImage[6][6];
        int index = 0;

        // Double Jump Effect
        BufferedImage[] doubleJump = new BufferedImage[4];
        int djW = (int)(57.5*Tiles.SCALE.getValue());
        int djH = (int)(25*Tiles.SCALE.getValue());
        for (int i = 0; i < 4; i++) {
            doubleJump[i] = Utils.getInstance().importImage("src/main/resources/images/player/DoubleJump/doubleJump"+i+".png", djW, djH);
        }
        anim[index++] = doubleJump;

        // Dust Effect
        BufferedImage[] dust = new BufferedImage[6];
        int dustW = (int)(120*Tiles.SCALE.getValue());
        int dustH = (int)(70*Tiles.SCALE.getValue());
        for (int i = 0; i < 6; i++) {
            dust[i] = Utils.getInstance().importImage("src/main/resources/images/particles/runningDust/runningDust"+i+".png", dustW, dustH);
        }
        anim[index] = dust;

        return anim;
    }

    public BufferedImage[][] loadObjects() {
        BufferedImage[][] anim = new BufferedImage[17][17];
        int index = 0;

        // Potions
        BufferedImage potionSprite = Utils.getInstance().importImage("src/main/resources/images/objs/potions_sprites.png", -1, -1);

        for (int i = 0; i < 2; i++, index++) {
            for (int j = 0; j < 7; j++) {
                anim[index][j] = potionSprite.getSubimage(12*j, 16*i, 12, 16);
            }
        }

        // Containers
        BufferedImage containerSprite = Utils.getInstance().importImage("src/main/resources/images/objs/objects_sprites.png", -1, -1);

        for (int i = 0; i < 2; i++, index++) {
            for (int j = 0; j < 8; j++) {
                anim[index][j] = containerSprite.getSubimage(40*j, 30*i, 40, 30);
            }
        }

        // Spikes
        BufferedImage spikeSprite = Utils.getInstance().importImage("src/main/resources/images/objs/spikes.png", -1, -1);

        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 10; j++) {
                anim[index][j] = spikeSprite.getSubimage(32*j, 32*i, 32, 32);
            }
        }

        // Arrow Launchers
        BufferedImage arrowLauncherSprite = Utils.getInstance().importImage("src/main/resources/images/objs/arrowTrap.png", -1, -1);

        for (int i = 0; i < 1; i++, index++) {
            for (int j = 0; j < 16; j++) {
                anim[index][j] = arrowLauncherSprite.getSubimage((96*j)+27, 32*i, 32, 32);
            }
        }

        return anim;
    }

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
