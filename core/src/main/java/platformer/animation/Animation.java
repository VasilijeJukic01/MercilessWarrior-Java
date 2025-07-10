package platformer.animation;

import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.NpcType;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class Animation {

    private static volatile Animation instance = null;
    private BufferedImage[][] coinAnimations;

    private Animation() {}

    public static Animation getInstance() {
        if (instance == null) {
            synchronized (Animation.class) {
                if (instance == null) {
                    instance = new Animation();
                }
            }
        }
        return instance;
    }

    // Animation loader
    /**
     * Load animation from sprite sheet
     *
     * @param basePath path to sprite sheet
     * @param frames number of frames in animation
     * @param row row of animation
     * @param width width of frame
     * @param height height of frame
     * @param offset offset of animation
     * @param x width of sprite
     * @param y height of sprite
     *
     * @return animation
     */
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
        BufferedImage[][] anim = new BufferedImage[18][18];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(sheet,       8, 0, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(sheet,        8, 1, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.JUMP.ordinal()] = loadFromSprite(sheet,       3, 6, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(sheet,       3, 8, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(sheet,   4, 10, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(sheet,   4, 11, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(sheet,   5, 12, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(sheet,      6, 14, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(sheet,        4, 23, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(sheet,      11, 24, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.WALL.ordinal()] = loadFromSprite(sheet,       4, 21, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.TRANSFORM.ordinal()] = loadFromSprite(sheet,  12, 20, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(sheet,    13, 19, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(sheet,    5, 18, w, h, 3, PLAYER_W, PLAYER_H);

        return anim;
    }

    // Enemy
    public BufferedImage[][] loadSkeletonAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(SKELETON_SHEET,      4, 0, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SKELETON_SHEET,       8, 2, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(SKELETON_SHEET,      2, 3, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SKELETON_SHEET,  6, 4, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(SKELETON_SHEET,     6, 5, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(SKELETON_SHEET,       4, 7, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SKELETON_SHEET,     10, 6, w, h, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(SKELETON_SHEET,      8, 1, w, h, 0, SKELETON_W, SKELETON_H);

        return anim;
    }

    public BufferedImage[][] loadGhoulAnimation(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[17][20];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(GHOUL_SHEET,     8, 0, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(GHOUL_SHEET,      6, 2, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 5, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(GHOUL_SHEET,      4, 4, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(GHOUL_SHEET,    8, 7, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(GHOUL_SHEET,     6, 1, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIDE.ordinal()] = loadFromSprite(GHOUL_SHEET,     19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = loadFromSprite(GHOUL_SHEET,   19, 6, w, h, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = Utils.getInstance().reverseArray(anim[Anim.REVEAL.ordinal()]);

        return anim;
    }

    public BufferedImage[][] loadKnightAnimation(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(KNIGHT_SHEET,     8, 0, w, h, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(KNIGHT_SHEET,      8, 2, w, h, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(KNIGHT_SHEET, 7, 3, w, h, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(KNIGHT_SHEET,      3, 5, w, h, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(KNIGHT_SHEET,    12,6, w, h, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(KNIGHT_SHEET,     8, 1, w, h, 0, KNIGHT_W, KNIGHT_H);

        return anim;
    }

    public BufferedImage[][] loadWraithAnimation(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[13][17];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(WRAITH_SHEET,     10, 0, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(WRAITH_SHEET,      8,  1, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(WRAITH_SHEET, 10, 2, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(WRAITH_SHEET, 10, 3, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(WRAITH_SHEET,      5,  4, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(WRAITH_SHEET,    16, 5, w, h, 0, WRAITH_W, WRAITH_H);
        anim[Anim.WALK.ordinal()] =  anim[Anim.IDLE.ordinal()];

        return anim;
    }

    // Boss
    public BufferedImage[][] loadSpearWomanAnimations(int w, int h) {
        BufferedImage[][] anim = new BufferedImage[20][25];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(SW_SHEET,        8, 0, w, h, 0, SW_W, SW_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SW_SHEET,         8, 2, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SW_SHEET,    5, 10, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(SW_SHEET,    5, 11, w, h, 0, SW_W, SW_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(SW_SHEET,    6, 12, w, h, 0, SW_W, SW_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(SW_SHEET,       16, 16, w, h, 0, SW_W, SW_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(SW_SHEET,         4, 23, w, h, 0, SW_W, SW_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SW_SHEET,       9, 24, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(SW_SHEET,     14, 13, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(SW_SHEET,     11, 14, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_3.ordinal()] = loadFromSprite(SW_SHEET,     22, 15, w, h, 0, SW_W, SW_H);
        anim[Anim.SPELL_4.ordinal()] = loadFromSprite(SW_SHEET,     2, 15, w, h, 1, SW_W, SW_H);

        return anim;
    }

    // Objects
    public BufferedImage[][] loadObjects() {
        BufferedImage[][] anim = new BufferedImage[26][17];

        anim[ObjType.STAMINA_POTION.ordinal()] = loadFromSprite(POTIONS_SHEET, 7, 0, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[ObjType.HEAL_POTION.ordinal()] = loadFromSprite(POTIONS_SHEET, 7, 1, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H);
        anim[ObjType.BOX.ordinal()] = loadFromSprite(CONTAINERS_SHEET, 8, 0, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[ObjType.BARREL.ordinal()] = loadFromSprite(CONTAINERS_SHEET, 8, 1, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H);
        anim[ObjType.SPIKE_UP.ordinal()] = loadFromSprite(SPIKES_SHEET, 10, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        anim[ObjType.ARROW_TRAP_RIGHT.ordinal()] = loadFromSprite(ARROW_TRAP_SHEET, 16, 0, ARROW_TRAP_WID, ARROW_TRAP_HEI, 1, AT_W, AT_H);
        anim[ObjType.ARROW_TRAP_LEFT.ordinal()] = anim[ObjType.ARROW_TRAP_RIGHT.ordinal()];
        loadCoinAnimations();
        anim[ObjType.COIN.ordinal()] = coinAnimations[0];
        anim[ObjType.SHOP.ordinal()] = loadFromSprite(SHOP_SHEET, 6, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H);
        anim[ObjType.BLOCKER.ordinal()] = loadFromSprite(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H);
        anim[ObjType.BLACKSMITH.ordinal()] = loadFromSprite(BS_SHEET, 8, 0, BLACKSMITH_WID, BLACKSMITH_HEI, 0, BLACKSMITH_W, BLACKSMITH_H);
        anim[ObjType.DOG.ordinal()] = loadFromSprite(DOG_SHEET, 8, 0, DOG_WID, DOG_HEI, 0, DOG_W, DOG_H);
        anim[ObjType.SAVE_TOTEM.ordinal()] = loadFromSprite(TOTEM_SHEET, 1, 0, SAVE_TOTEM_WID, SAVE_TOTEM_HEI, 0, TOTEM_W, TOTEM_H);
        anim[ObjType.SMASH_TRAP.ordinal()] = loadFromSprite(SMASH_TRAP_SHEET, 14, 0, SMASH_TRAP_WID, SMASH_TRAP_HEI, 0, ST_W, ST_H);
        anim[ObjType.CANDLE.ordinal()] = loadFromSprite(CANDLE_IMG, 1, 0, CANDLE_WID, CANDLE_HEI, 0, CANDLE_W, CANDLE_H);
        anim[ObjType.LOOT.ordinal()] = loadFromSprite(LOOT_IMG, 1, 0, LOOT_WID, LOOT_HEI, 0, LOOT_W, LOOT_H);
        anim[ObjType.TABLE.ordinal()] = loadFromSprite(TABLE_IMG, 1, 0, TABLE_WID, TABLE_HEI, 0, TABLE_W, TABLE_H);
        anim[ObjType.BOARD.ordinal()] = loadFromSprite(BOARD_IMG, 1, 0, BOARD_WID, BOARD_HEI, 0, BOARD_W, BOARD_H);
        anim[ObjType.LAVA.ordinal()] = loadFromSprite(LAVA_SHEET, 16, 0, LAVA_WID, LAVA_HEI, 0, LAVA_W, LAVA_H);
        anim[ObjType.BRICK.ordinal()] = loadFromSprite(BRICK_SHEET, 8, 0, BRICK_WID, BRICK_HEI, 0, BRICK_W, BRICK_H);
        anim[ObjType.JUMP_PAD.ordinal()] = loadFromSprite(JUMP_PAD_SHEET, 20, 0, JUMP_PAD_WID, JUMP_PAD_HEI, 0, JUMP_PAD_W, JUMP_PAD_H);
        anim[ObjType.SPIKE_DOWN.ordinal()] = new BufferedImage[]{Utils.getInstance().rotateImage(anim[ObjType.SPIKE_UP.ordinal()][5], 180)};
        anim[ObjType.SPIKE_LEFT.ordinal()] = new BufferedImage[]{Utils.getInstance().rotateImage(anim[ObjType.SPIKE_UP.ordinal()][5], 270)};
        anim[ObjType.SPIKE_RIGHT.ordinal()] = new BufferedImage[]{Utils.getInstance().rotateImage(anim[ObjType.SPIKE_UP.ordinal()][5], 90)};
        anim[ObjType.HERB.ordinal()] = loadFromSprite(HERB_IMG, 1, 0, HERB_WID, HERB_HEI, 0, HERB_W, HERB_H);

        return anim;
    }

    private void loadCoinAnimations() {
        coinAnimations = new BufferedImage[3][];
        // Bronze
        coinAnimations[0] = loadFromSprite(COIN_SHEET, 4, 0, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
        // Silver
        coinAnimations[1] = loadFromSprite(COIN_SHEET, 4, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
        // Gold
        coinAnimations[2] = loadFromSprite(COIN_SHEET, 4, 2, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H);
    }

    // NPC
    private BufferedImage[] loadNpcAnimation(String sheet, int frames, int row, NpcType type) {
        return loadFromSprite(sheet, frames, row, type.getWid(), type.getHei(), 0, type.getSpriteW(), type.getSpriteH());
    }

    public BufferedImage[][] loadNpcs() {
        BufferedImage[][] anim = new BufferedImage[4][6];

        anim[NpcType.ANITA.ordinal()] = loadNpcAnimation(ANITA_SHEET, 4, 0, NpcType.ANITA);
        anim[NpcType.NIKOLAS.ordinal()] = loadNpcAnimation(NIKOLAS_SHEET, 4, 0, NpcType.NIKOLAS);
        anim[NpcType.SIR_DEJANOVIC.ordinal()] = loadNpcAnimation(SIR_DEJANOVIC_SHEET, 4, 0, NpcType.SIR_DEJANOVIC);
        anim[NpcType.KRYSANTHE.ordinal()] = loadNpcAnimation(KRYSANTHE_SHEET, 8, 0, NpcType.KRYSANTHE);

        return anim;
    }

    public BufferedImage[] loadLightningBall(String sprite) {
        return loadFromSprite(sprite, 9, 0, LB_WID, LB_HEI, 0, LIGHTNING_BALL_W, LIGHTNING_BALL_H);
    }

    public BufferedImage[] loadFireBall() {
        return loadFromSprite(FIREBALL_SHEET, 9, 0, FB_WID, FB_HEI,0, FIREBALL_W, FIREBALL_H);
    }

    // Getters
    public BufferedImage[][] getCoinAnimations() {
        return coinAnimations;
    }

}
