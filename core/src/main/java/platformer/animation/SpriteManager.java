package platformer.animation;

import platformer.core.loading.LoadingProgressTracker;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.loader.EnemyDefinition;
import platformer.model.entities.enemies.loader.EnemyRegistry;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.NpcType;
import platformer.utils.CollectionUtils;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final Map<String, BufferedImage> rawSheets = new ConcurrentHashMap<>();
    private BufferedImage[][] playerAnimations, playerTransformAnimations;
    private final Map<EnemyType, BufferedImage[][]> enemyAnimations = new ConcurrentHashMap<>();
    private final Map<ObjType, BufferedImage[]> objectAnimations = new ConcurrentHashMap<>();
    private final Map<NpcType, BufferedImage[]> npcAnimations = new ConcurrentHashMap<>();
    private final Map<NpcType, BufferedImage[][]> followerAnimations = new HashMap<>();
    private BufferedImage[][] coinAnimations;

    private BufferedImage[] fireballAnimations;
    private BufferedImage[][] roricProjectileAnimations;

    private BufferedImage[] lightningAnimations;
    private BufferedImage[] skyBeamAnimations;

    private BufferedImage[] roricAuraAnimations;

    private BufferedImage arrowImage;
    private BufferedImage roricArrowImage;

    private final Map<NpcType, BufferedImage> npcIcons = new HashMap<>();

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
        EnemyRegistry enemyRegistry = EnemyRegistry.getInstance();

        List<Runnable> loadTasks = new ArrayList<>();
        loadTasks.add(() -> playerAnimations = loadPlayerSpriteSheet(PLAYER_SHEET));
        loadTasks.add(() -> playerTransformAnimations = loadPlayerSpriteSheet(PLAYER_TRANSFORM_SHEET));
        for (EnemyType type : enemyRegistry.getRegisteredTypes()) {
            loadTasks.add(() -> loadEnemyAnimations(type, enemyRegistry.get(type)));
        }
        loadTasks.add(this::loadObjectAnimations);
        loadTasks.add(this::loadNpcAnimations);
        loadTasks.add(this::loadFollowerAnimations);
        loadTasks.add(this::loadProjectileAndSpellAnimations);
        loadTasks.add(this::loadEffectAnimations);

        AtomicInteger completedTasks = new AtomicInteger(0);
        double startProgress = 0.05;
        double totalProgressAllocated = 0.90;

        CompletableFuture<?>[] futures = loadTasks.stream().map(task ->
                CompletableFuture.runAsync(() -> {
                    task.run();
                    int done = completedTasks.incrementAndGet();
                    double progress = startProgress + ((double) done / loadTasks.size()) * totalProgressAllocated;
                    LoadingProgressTracker.getInstance().updateProgress(progress);
                })
        ).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        rawSheets.clear();
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
        BufferedImage sprite = rawSheets.computeIfAbsent(basePath, path -> ImageUtils.importImage(path, -1, -1));
        BufferedImage[] animation = new BufferedImage[frames];
        int yOffset = y * row;
        for (int i = offset; i < frames + offset; i++) {
            animation[i - offset] = ImageUtils.resizeImage(sprite.getSubimage(i * x, yOffset, x, y), width, height);
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
        BufferedImage[][] anim = new BufferedImage[26][18];
        int w = PLAYER_WIDTH, h = PLAYER_HEIGHT;

        anim[Anim.IDLE.ordinal()] = loadFromSprite(sheet, 8, 0, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(sheet, 8, 1, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.DASH.ordinal()] = loadFromSprite(sheet, 7, 4, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.JUMP.ordinal()] = loadFromSprite(sheet, 3, 6, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(sheet, 3, 8, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.AIR_ATTACK.ordinal()] = loadFromSprite(sheet, 5, 9, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(sheet, 4, 10, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(sheet, 4, 11, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.ATTACK_3.ordinal()] = loadFromSprite(sheet, 5, 12, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.BLOCK.ordinal()] = loadFromSprite(sheet, 8, 13, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.PARRY.ordinal()] = loadFromSprite(sheet, 4, 14, w, h, 2, PLAYER_W, PLAYER_H);
        anim[Anim.CROUCH.ordinal()] = loadFromSprite(sheet, 5, 15, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.CROUCH_ATTACK.ordinal()] = loadFromSprite(sheet, 5, 16, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.HIT.ordinal()] = loadFromSprite(sheet, 4, 23, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.DEATH.ordinal()] = loadFromSprite(sheet, 11, 24, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.WALL.ordinal()] = loadFromSprite(sheet, 4, 21, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.TRANSFORM.ordinal()] = loadFromSprite(sheet, 12, 20, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_1.ordinal()] = loadFromSprite(sheet, 13, 19, w, h, 0, PLAYER_W, PLAYER_H);
        anim[Anim.SPELL_2.ordinal()] = loadFromSprite(sheet, 5, 18, w, h, 3, PLAYER_W, PLAYER_H);

        return anim;
    }

    // Enemy
    private void loadEnemyAnimations(EnemyType type, EnemyDefinition def) {
        if (def == null || def.getSpriteSheet() == null || def.getAnimations() == null) return;

        BufferedImage[][] anim = new BufferedImage[Anim.values().length][];
        int drawW = def.getDrawWidth(), drawH = def.getDrawHeight();
        int frameW = def.getSpriteFrameW(), frameH = def.getSpriteFrameH();
        for (Map.Entry<String, EnemyDefinition.AnimSpec> entry : def.getAnimations().entrySet()) {
            EnemyDefinition.AnimSpec spec = entry.getValue();
            if (spec.getAliasOf() != null) continue;

            Anim animEnum = Anim.valueOf(entry.getKey().toUpperCase());
            BufferedImage[] frames = loadFromSprite(def.getSpriteSheet(), spec.getFrames(), spec.getRow(), drawW, drawH, spec.getOffset(), frameW, frameH);
            if (spec.isReverse()) CollectionUtils.reverseArray(frames);
            anim[animEnum.ordinal()] = frames;
        }

        for (Map.Entry<String, EnemyDefinition.AnimSpec> entry : def.getAnimations().entrySet()) {
            EnemyDefinition.AnimSpec spec = entry.getValue();
            if (spec.getAliasOf() == null) continue;
            Anim animEnum = Anim.valueOf(entry.getKey().toUpperCase());
            Anim targetEnum = Anim.valueOf(spec.getAliasOf().toUpperCase());
            anim[animEnum.ordinal()] = anim[targetEnum.ordinal()];
        }
        enemyAnimations.put(type, anim);
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
        BufferedImage[] spikes = loadFromSprite(SPIKES_SHEET, 1, 0, SPIKE_WID, SPIKE_HEI, 0, SPIKES_W, SPIKES_H);
        objectAnimations.put(ObjType.SPIKE_UP, spikes);
        objectAnimations.put(ObjType.SPIKE_DOWN, new BufferedImage[]{ImageUtils.rotateImage(spikes[0], 180)});
        objectAnimations.put(ObjType.SPIKE_LEFT, new BufferedImage[]{ImageUtils.rotateImage(spikes[0], 270)});
        objectAnimations.put(ObjType.SPIKE_RIGHT, new BufferedImage[]{ImageUtils.rotateImage(spikes[0], 90)});
        // Interactive Objects
        objectAnimations.put(ObjType.SHOP, loadFromSprite(SHOP_SHEET, 1, 0, SHOP_WID, SHOP_HEI, 0, SHOP_W, SHOP_H));
        objectAnimations.put(ObjType.BLOCKER, loadFromSprite(BLOCKER_SHEET, 12, 0, BLOCKER_WID, BLOCKER_HEI, 0, BLOCKER_W, BLOCKER_H));
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
        npcAnimations.put(NpcType.ANITA,          loadSingleNpcAnimation(ANITA_SHEET, 4, 0, NpcType.ANITA));
        npcAnimations.put(NpcType.NIKOLAS,        loadSingleNpcAnimation(NIKOLAS_SHEET, 4, 0, NpcType.NIKOLAS));
        npcAnimations.put(NpcType.SIR_DEJANOVIC,  loadSingleNpcAnimation(SIR_DEJANOVIC_SHEET, 8, 0, NpcType.SIR_DEJANOVIC));
        npcAnimations.put(NpcType.KRYSANTHE,      loadSingleNpcAnimation(KRYSANTHE_SHEET, 8, 0, NpcType.KRYSANTHE));
        npcAnimations.put(NpcType.RORIC,          loadFromSprite(RORIC_SHEET, 12, 0, NpcType.RORIC.getWid(), NpcType.RORIC.getHei(), 0, RORIC_W, RORIC_H));
        npcAnimations.put(NpcType.MITCH_OF_ASSIS, loadSingleNpcAnimation(MITCH_SHEET, 5, 0, NpcType.MITCH_OF_ASSIS));
        npcAnimations.put(NpcType.ANDELLIA,       loadSingleNpcAnimation(ANDELLIA_SHEET, 4, 0, NpcType.ANDELLIA));
        npcAnimations.put(NpcType.EERYN,          loadSingleNpcAnimation(EERYN_SHEET, 4, 0, NpcType.EERYN));
        npcAnimations.put(NpcType.BLACKSMITH,     loadSingleNpcAnimation(BS_SHEET, 8, 0, NpcType.BLACKSMITH));
        npcAnimations.put(NpcType.DOG,            loadSingleNpcAnimation(DOG_SHEET, 8, 0, NpcType.DOG));

        for (NpcType type : NpcType.values()) {
            String path = AVATAR_PATH.replace("$", type.name());
            BufferedImage portrait = ImageUtils.importImage(path, -1, -1);
            npcIcons.put(type, portrait);
        }
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

    // Follower
    private void loadFollowerAnimations() {
        loadAnitaFollower();
        loadDejanovicFollower();
    }

    private void loadAnitaFollower() {
        BufferedImage[][] anim = new BufferedImage[25][];
        anim[Anim.IDLE.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 8, 0, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 8, 1, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.JUMP.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 3, 2, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.FALL.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 3, 3, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 5, 9, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(ANITA_FOLLOWER_SHEET, 7, 10, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.SPELL_1.ordinal()]  = loadFromSprite(ANITA_FOLLOWER_SHEET, 18, 11, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.HIT.ordinal()]  = loadFromSprite(ANITA_FOLLOWER_SHEET, 5, 14, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.BLOCK.ordinal()]  = loadFromSprite(ANITA_FOLLOWER_SHEET, 10, 13, FLW_WIDTH, FLW_HEIGHT, 1, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);
        anim[Anim.DEATH.ordinal()]  = loadFromSprite(ANITA_FOLLOWER_SHEET, 10, 15, FLW_WIDTH, FLW_HEIGHT, 0, ANITA_FOLLOWER_W, ANITA_FOLLOWER_H);

        followerAnimations.put(NpcType.ANITA, anim);
    }

    private void loadDejanovicFollower() {
        BufferedImage[][] anim = new BufferedImage[25][];
        anim[Anim.IDLE.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET,     8, 0, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.WALK.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET,     8, 1, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.RUN.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET,      8, 2, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.JUMP.ordinal()] = anim[Anim.RUN.ordinal()];
        anim[Anim.FALL.ordinal()] = anim[Anim.RUN.ordinal()];
        anim[Anim.ATTACK_1.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET, 8, 3, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.ATTACK_2.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET, 8, 4, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.SPELL_1.ordinal()] = anim[Anim.ATTACK_2.ordinal()];
        anim[Anim.HIT.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET,      5, 5, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);
        anim[Anim.BLOCK.ordinal()] = anim[Anim.IDLE.ordinal()];
        anim[Anim.DEATH.ordinal()] = loadFromSprite(SIR_DEJANOVIC_SHEET,    12, 6, KNIGHT_WIDTH, KNIGHT_HEIGHT, 0, KNIGHT_W, KNIGHT_H);

        followerAnimations.put(NpcType.SIR_DEJANOVIC, anim);
    }

    // Projectiles & Spells
    private void loadProjectileAndSpellAnimations() {
        lightningAnimations = loadFromSprite(LIGHTNING_SHEET, 8, 0, LIGHTNING_WIDTH, LIGHTNING_HEIGHT, 0, LIGHTNING_W, LIGHTNING_H);
        fireballAnimations = loadFromSprite(FIREBALL_SHEET, 9, 0, FB_WID, FB_HEI,0, FIREBALL_W, FIREBALL_H);

        roricProjectileAnimations = new BufferedImage[3][];
        roricProjectileAnimations[0] = loadFromSprite(RORIC_SPELLS_SHEET, 5, 8, RORIC_BEAM_WID, RORIC_BEAM_HEI, 0, RORIC_PROJECTILE_W, RORIC_PROJECTILE_H);
        roricProjectileAnimations[1] = loadFromSprite(RORIC_SPELLS_SHEET, 13, 7, RORIC_RAIN_WID, RORIC_RAIN_HEI, 0, RORIC_PROJECTILE_W, RORIC_PROJECTILE_H);
        roricProjectileAnimations[2] = loadFromSprite(CELESTIAL_PROJECTILE_SHEET, 4, 0, CELESTIAL_ORB_WID, CELESTIAL_ORB_HEI, 0, CELESTIAL_PROJECTILE_W, CELESTIAL_PROJECTILE_H);

        arrowImage = ImageUtils.importImage(ARROW_IMG, ARROW_WID, ARROW_HEI);
        roricArrowImage = ImageUtils.importImage(RORIC_ARROW_IMG, ARROW_WID, ARROW_HEI);

        // Sky Beam
        BufferedImage[] originalFrames = roricProjectileAnimations[0];
        BufferedImage[] rotatedBeamFrames = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
            rotatedBeamFrames[i] = ImageUtils.rotateImage(originalFrames[i], 90);
        }
        BufferedImage[] reversedFrames = CollectionUtils.reverseArray(rotatedBeamFrames.clone());
        skyBeamAnimations = new BufferedImage[8];
        System.arraycopy(reversedFrames, 0, skyBeamAnimations, 0, 4);
        System.arraycopy(rotatedBeamFrames, 0, skyBeamAnimations, 4, 4);
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

    public BufferedImage[][] getFollowerAnimations(NpcType type) {
        return followerAnimations.get(type);
    }

    public BufferedImage[][] getCoinAnimations() {
        return coinAnimations;
    }

    public BufferedImage[] getLightningAnimations() {
        return lightningAnimations;
    }

    public BufferedImage[] getRoricBeamAnimations() {
        return roricProjectileAnimations[0];
    }

    public BufferedImage[] getArrowRainAnimations() {
        return roricProjectileAnimations[1];
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

    public BufferedImage getArrowImage() {
        return arrowImage;
    }

    public BufferedImage getRoricArrowImage() {
        return roricArrowImage;
    }

    public BufferedImage getNpcPortrait(NpcType type) {
        return npcIcons.get(type);
    }

}
