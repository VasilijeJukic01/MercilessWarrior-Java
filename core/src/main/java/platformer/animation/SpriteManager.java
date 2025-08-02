package platformer.animation;

import platformer.model.entities.enemies.EnemyType;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.NpcType;
import platformer.utils.CollectionUtils;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * A singleton asset manager responsible for loading, caching, and providing access to all game sprites and animations.
 * It ensures that spritesheets are loaded from disk only once to improve performance and centralize asset management.
 */
public class SpriteManager {

    private static volatile SpriteManager instance;

    // Caches
    private BufferedImage[][] playerAnimations, playerTransformAnimations;
    private final Map<EnemyType, BufferedImage[][]> enemyAnimations = new HashMap<>();
    private final Map<ObjType, BufferedImage[]> objectAnimations = new HashMap<>();
    private final Map<NpcType, BufferedImage[]> npcAnimations = new HashMap<>();
    private BufferedImage[][] coinAnimations;

    private BufferedImage[] lightningBallAnimations;
    private BufferedImage[] fireballAnimations;
    private BufferedImage[] energyBallAnimations;
    private BufferedImage[][] roricProjectileAnimations;
    private BufferedImage[] skyBeamAnimations;

    private BufferedImage[] roricAuraAnimations;

    private SpriteManager() {}

    public static SpriteManager getInstance() {
        if (instance == null) {
            synchronized (SpriteManager.class) {
                if (instance == null) {
                    instance = new SpriteManager();
                }
            }
        }
        return instance;
    }

    /**
     * Loads all necessary game assets into memory. This should be called once during the game's initialization phase.
     */
    public void loadAllAssets() {
        playerAnimations = loadPlayerSpriteSheet(PLAYER_SHEET);
        playerTransformAnimations = loadPlayerSpriteSheet(PLAYER_TRANSFORM_SHEET);
        loadEnemyAnimations();
        loadObjectAnimations();
        loadNpcAnimations();
        loadProjectileAndSpellAnimations();
        loadEffectAnimations();
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
        BufferedImage sprite = ImageUtils.importImage(basePath, -1, -1);
        BufferedImage[] animation = new BufferedImage[frames];
        int yOffset = y * row;
        for (int i = offset; i < frames+offset; i++) {
            animation[i-offset] = ImageUtils.resizeImage(sprite.getSubimage(i * x, yOffset, x, y), width, height);
        }
        return animation;
    }

    /**
     * Gets the number of frames for a specific animation of a given enemy type.
     *
     * @param type The type of the enemy.
     * @param anim The animation state.
     * @return The number of frames in the specified animation, or 1 as a fallback.
     */
    public int getAnimFrames(EnemyType type, Anim anim) {
        BufferedImage[][] anims = enemyAnimations.get(type);
        if (anims != null && anim.ordinal() < anims.length) {
            BufferedImage[] specificAnim = anims[anim.ordinal()];
            if (specificAnim != null) return specificAnim.length;
        }
        return 1;
    }

    // Player
    private BufferedImage[][] loadPlayerSpriteSheet(String sheet) {
        BufferedImage[][] anim = new BufferedImage[18][18];
        int w = PLAYER_WIDTH, h = PLAYER_HEIGHT;

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
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(sheet, 5, 18, w, h, 3, PLAYER_W, PLAYER_H);

        return anim;
    }

    // Enemy
    private void loadEnemyAnimations() {
        loadSkeletonAnimations();
        loadGhoulAnimations();
        loadKnightAnimations();
        loadWraithAnimations();
        loadLancerAnimations();
        loadRoricAnimations();
    }

    private void loadSkeletonAnimations() {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(SKELETON_SHEET,      4, 0, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SKELETON_SHEET,       8, 2, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(SKELETON_SHEET,      2, 3, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SKELETON_SHEET,  6, 4, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(SKELETON_SHEET,     6, 5, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(SKELETON_SHEET,       4, 7, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SKELETON_SHEET,     10, 6, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(SKELETON_SHEET,      8, 1, SKELETON_WIDTH, SKELETON_HEIGHT, 0, SKELETON_W, SKELETON_H);

        enemyAnimations.put(EnemyType.SKELETON, anim);
    }

    private void loadGhoulAnimations() {
        BufferedImage[][] anim = new BufferedImage[17][20];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 0, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(GHOUL_SHEET, 6, 2, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 5, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(GHOUL_SHEET, 4, 4, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(GHOUL_SHEET, 8, 7, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(GHOUL_SHEET, 6, 1, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.HIDE.ordinal()] = loadFromSprite(GHOUL_SHEET, 19, 6, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = loadFromSprite(GHOUL_SHEET, 19, 6, GHOUL_WIDTH, GHOUL_HEIGHT, 0, GHOUL_W, GHOUL_H);
        anim[Anim.REVEAL.ordinal()] = CollectionUtils.reverseArray(anim[Anim.REVEAL.ordinal()]);

        enemyAnimations.put(EnemyType.GHOUL, anim);
    }

    private void loadKnightAnimations() {
        BufferedImage[][] anim = new BufferedImage[13][13];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(KNIGHT_SHEET,     8, 0, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(KNIGHT_SHEET,      8, 2, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(KNIGHT_SHEET, 7, 3, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(KNIGHT_SHEET,      3, 5, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(KNIGHT_SHEET,    12,6, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(KNIGHT_SHEET,     8, 1, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);

        enemyAnimations.put(EnemyType.KNIGHT, anim);
    }

    private void loadWraithAnimations() {
        BufferedImage[][] anim = new BufferedImage[13][17];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(WRAITH_SHEET,     10, 0, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(WRAITH_SHEET,      8,  1, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(WRAITH_SHEET, 10, 2, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(WRAITH_SHEET, 10, 3, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(WRAITH_SHEET,      5,  4, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(WRAITH_SHEET,    16, 5, WRAITH_WIDTH, WRAITH_HEIGHT, 0, WRAITH_W, WRAITH_H);
        anim[Anim.WALK.ordinal()] =  anim[Anim.IDLE.ordinal()];

        enemyAnimations.put(EnemyType.WRAITH, anim);
    }

    private void loadLancerAnimations() {
        BufferedImage[][] anim = new BufferedImage[20][25];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(LANCER_SHEET,        8, 0, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(LANCER_SHEET,         8, 2, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(LANCER_SHEET,    5, 10, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(LANCER_SHEET,    5, 11, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(LANCER_SHEET,    6, 12, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(LANCER_SHEET,       16, 16, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(LANCER_SHEET,         4, 23, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(LANCER_SHEET,       9, 24, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(LANCER_SHEET,     14, 13, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(LANCER_SHEET,     11, 14, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.SPELL_3.ordinal()] = loadFromSprite(LANCER_SHEET,     22, 15, LANCER_WIDTH, LANCER_HEIGHT, 0, SW_W, SW_H);
        anim[Anim.SPELL_4.ordinal()] = loadFromSprite(LANCER_SHEET,     2, 15, LANCER_WIDTH, LANCER_HEIGHT, 1, SW_W, SW_H);

        enemyAnimations.put(EnemyType.LANCER, anim);
    }

    /**
     * This boss is a timed fight, so it does not have death and hit animations.
     */
    private void loadRoricAnimations() {
        BufferedImage[][] anim = new BufferedImage[21][22];

        anim[Anim.IDLE.ordinal()] = loadFromSprite(RORIC_SHEET, 12, 0, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(RORIC_SHEET, 10, 1, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.JUMP_FALL.ordinal()] = loadFromSprite(RORIC_SHEET, 22, 6, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(RORIC_SHEET, 10, 10, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(RORIC_SHEET, 15, 11, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.ATTACK_3.ordinal()] = anim[Anim.IDLE.ordinal()];
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(RORIC_SHEET, 19, 14, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(RORIC_SHEET, 17, 13, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(RORIC_SHEET, 10, 7, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);
        anim[Anim.SPELL_3.ordinal()] = loadFromSprite(RORIC_SHEET, 12, 12, RORIC_WIDTH, RORIC_HEIGHT, 0, RORIC_W, RORIC_H);

        enemyAnimations.put(EnemyType.RORIC, anim);
    }

    // Objects
    private void loadObjectAnimations() {
        // Potions
        objectAnimations.put(ObjType.STAMINA_POTION, loadFromSprite(POTIONS_SHEET, 7, 0, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H));
        objectAnimations.put(ObjType.HEAL_POTION, loadFromSprite(POTIONS_SHEET, 7, 1, POTION_WID, POTION_HEI, 0, POTION_W, POTION_H));
        // Containers
        objectAnimations.put(ObjType.BOX, loadFromSprite(CONTAINERS_SHEET, 8, 0, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H));
        objectAnimations.put(ObjType.BARREL, loadFromSprite(CONTAINERS_SHEET, 8, 1, CONTAINER_WID, CONTAINER_HEI, 0, CONTAINER_W, CONTAINER_H));
        // Traps
        objectAnimations.put(ObjType.ARROW_TRAP_RIGHT, loadFromSprite(ARROW_TRAP_SHEET, 16, 0, ARROW_TRAP_WID, ARROW_TRAP_HEI, 1, AT_W, AT_H));
        objectAnimations.put(ObjType.ARROW_TRAP_LEFT, objectAnimations.get(ObjType.ARROW_TRAP_RIGHT));
        objectAnimations.put(ObjType.SMASH_TRAP, loadFromSprite(SMASH_TRAP_SHEET, 14, 0, SMASH_TRAP_WID, SMASH_TRAP_HEI, 0, ST_W, ST_H));
        objectAnimations.put(ObjType.RORIC_TRAP, loadFromSprite(RORIC_SPELLS_SHEET, 8, 6, RORIC_TRAP_WID, RORIC_TRAP_HEI, 0, RORIC_PROJECTILE_W, RORIC_PROJECTILE_H));
        // Spikes
        BufferedImage[] spikeUpFrames = loadFromSprite(SPIKES_SHEET, 10, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        objectAnimations.put(ObjType.SPIKE_UP, spikeUpFrames);
        if (spikeUpFrames.length > 5) {
            objectAnimations.put(ObjType.SPIKE_DOWN, new BufferedImage[]{ImageUtils.rotateImage(spikeUpFrames[5], 180)});
            objectAnimations.put(ObjType.SPIKE_LEFT, new BufferedImage[]{ImageUtils.rotateImage(spikeUpFrames[5], 270)});
            objectAnimations.put(ObjType.SPIKE_RIGHT, new BufferedImage[]{ImageUtils.rotateImage(spikeUpFrames[5], 90)});
        }
        // Interactive Objects
        objectAnimations.put(ObjType.SHOP, loadFromSprite(SHOP_SHEET, 6, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H));
        objectAnimations.put(ObjType.BLOCKER, loadFromSprite(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H));
        objectAnimations.put(ObjType.BLACKSMITH, loadFromSprite(BS_SHEET, 8, 0, BLACKSMITH_WID, BLACKSMITH_HEI, 0, BLACKSMITH_W, BLACKSMITH_H));
        objectAnimations.put(ObjType.DOG, loadFromSprite(DOG_SHEET, 8, 0, DOG_WID, DOG_HEI, 0, DOG_W, DOG_H));
        objectAnimations.put(ObjType.SAVE_TOTEM, loadFromSprite(TOTEM_SHEET, 1, 0, SAVE_TOTEM_WID, SAVE_TOTEM_HEI, 0, TOTEM_W, TOTEM_H));
        objectAnimations.put(ObjType.JUMP_PAD, loadFromSprite(JUMP_PAD_SHEET, 20, 0, JUMP_PAD_WID, JUMP_PAD_HEI, 0, JUMP_PAD_W, JUMP_PAD_H));
        objectAnimations.put(ObjType.LOOT, loadFromSprite(LOOT_IMG, 1, 0, LOOT_WID, LOOT_HEI, 0, LOOT_W, LOOT_H));
        objectAnimations.put(ObjType.TABLE, loadFromSprite(TABLE_IMG, 1, 0, TABLE_WID, TABLE_HEI, 0, TABLE_W, TABLE_H));
        objectAnimations.put(ObjType.BOARD, loadFromSprite(BOARD_IMG, 1, 0, BOARD_WID, BOARD_HEI, 0, BOARD_W, BOARD_H));
        objectAnimations.put(ObjType.HERB, loadFromSprite(HERB_IMG, 1, 0, HERB_WID, HERB_HEI, 0, HERB_W, HERB_H));
        // Decorations & Static Objects
        objectAnimations.put(ObjType.CANDLE, loadFromSprite(CANDLE_IMG, 1, 0, CANDLE_WID, CANDLE_HEI, 0, CANDLE_W, CANDLE_H));
        // Tiled / Animated Textures
        objectAnimations.put(ObjType.LAVA, loadFromSprite(LAVA_SHEET, 16, 0, LAVA_WID, LAVA_HEI, 0, LAVA_W, LAVA_H));
        objectAnimations.put(ObjType.BRICK, loadFromSprite(BRICK_SHEET, 8, 0, BRICK_WID, BRICK_HEI, 0, BRICK_W, BRICK_H));
        // Coins
        loadCoinAnimations();
        objectAnimations.put(ObjType.COIN, coinAnimations[0]);
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
    private void loadNpcAnimations() {
        npcAnimations.put(NpcType.ANITA,         loadSingleNpcAnimation(ANITA_SHEET, 4, 0, NpcType.ANITA));
        npcAnimations.put(NpcType.NIKOLAS,       loadSingleNpcAnimation(NIKOLAS_SHEET, 4, 0, NpcType.NIKOLAS));
        npcAnimations.put(NpcType.SIR_DEJANOVIC, loadSingleNpcAnimation(SIR_DEJANOVIC_SHEET, 4, 0, NpcType.SIR_DEJANOVIC));
        npcAnimations.put(NpcType.KRYSANTHE,     loadSingleNpcAnimation(KRYSANTHE_SHEET, 8, 0, NpcType.KRYSANTHE));
        npcAnimations.put(NpcType.RORIC,         loadFromSprite(RORIC_SHEET, 12, 0, NpcType.RORIC.getWid(), NpcType.RORIC.getHei(), 0, RORIC_W, RORIC_H));
    }

    /**
     * A helper method to load the animation for a single NPC type.
     *
     * @param sheet  The resource path to the NPC's spritesheet.
     * @param frames The number of animation frames.
     * @param row    The row on the spritesheet to read from.
     * @param type   The {@link NpcType} to load, providing dimension information.
     * @return An array of {@link BufferedImage} containing the loaded animation frames.
     */
    private BufferedImage[] loadSingleNpcAnimation(String sheet, int frames, int row, NpcType type) {
        return loadFromSprite(sheet, frames, row, type.getWid(), type.getHei(), 0, type.getSpriteW(), type.getSpriteH());
    }

    // Projectiles & Spells
    private void loadProjectileAndSpellAnimations() {
        lightningBallAnimations = loadFromSprite(LIGHTNING_BALL_1_SHEET, 9, 0, LB_WID, LB_HEI, 0, LIGHTNING_BALL_W, LIGHTNING_BALL_H);
        fireballAnimations = loadFromSprite(FIREBALL_SHEET, 9, 0, FB_WID, FB_HEI,0, FIREBALL_W, FIREBALL_H);
        energyBallAnimations = loadFromSprite(LIGHTNING_BALL_2_SHEET, 9, 0, LB_WID, LB_HEI, 0, LIGHTNING_BALL_W, LIGHTNING_BALL_H);

        roricProjectileAnimations = new BufferedImage[3][];
        roricProjectileAnimations[0] = loadFromSprite(RORIC_SPELLS_SHEET, 5, 8, RORIC_BEAM_WID, RORIC_BEAM_HEI, 0, RORIC_PROJECTILE_W, RORIC_PROJECTILE_H);
        roricProjectileAnimations[1] = loadFromSprite(RORIC_SPELLS_SHEET, 13, 7, RORIC_RAIN_WID, RORIC_RAIN_HEI, 0, RORIC_PROJECTILE_W, RORIC_PROJECTILE_H);
        roricProjectileAnimations[2] = loadFromSprite(CELESTIAL_PROJECTILE_SHEET, 4, 0, CELESTIAL_ORB_WID, CELESTIAL_ORB_HEI, 0, CELESTIAL_PROJECTILE_W, CELESTIAL_PROJECTILE_H);

        // Sky Beam
        BufferedImage[] originalFrames = roricProjectileAnimations[0];
        BufferedImage[] beamFrames = new BufferedImage[4];
        System.arraycopy(originalFrames, 0, beamFrames, 0, 4);
        BufferedImage[] reversedFrames = CollectionUtils.reverseArray(beamFrames.clone());
        skyBeamAnimations = new BufferedImage[8];
        System.arraycopy(reversedFrames, 0, skyBeamAnimations, 0, 4);
        System.arraycopy(beamFrames, 0, skyBeamAnimations, 4, 4);
    }

    // Effects
    private void loadEffectAnimations() {
        roricAuraAnimations = loadFromSprite(RORIC_AURA_SHEET, 4, 0, RORIC_AURA_WID, RORIC_AURA_HEI, 0, RORIC_AURA_W, RORIC_AURA_H);
    }

    public BufferedImage[][] getPlayerAnimations(boolean isTransformed) {
        return isTransformed ? playerTransformAnimations : playerAnimations;
    }

    public BufferedImage[][] getEnemyAnimations(EnemyType type) {
        return enemyAnimations.get(type);
    }

    public BufferedImage[] getObjectAnimations(ObjType type) {
        return objectAnimations.get(type);
    }

    public BufferedImage[] getNpcAnimations(NpcType type) {
        return npcAnimations.get(type);
    }

    public BufferedImage[][] getCoinAnimations() {
        return coinAnimations;
    }

    public BufferedImage[] getLightningBallAnimations() {
        return lightningBallAnimations;
    }

    public BufferedImage[] getEnergyBallAnimations() {
        return energyBallAnimations;
    }

    public BufferedImage[] getFireballAnimations() {
        return fireballAnimations;
    }

    public BufferedImage[][] getRoricProjectileAnimations() {
        return roricProjectileAnimations;
    }

    public BufferedImage[] getSkyBeamAnimations() {
        return skyBeamAnimations;
    }

    public BufferedImage[] getRoricAuraAnimations() {
        return roricAuraAnimations;
    }

}
