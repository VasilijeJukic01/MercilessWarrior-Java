package platformer.model.entities.enemies.boss.roric;

import platformer.animation.Anim;
import platformer.event.EventBus;
import platformer.event.events.roric.RoricCloneEvent;
import platformer.event.events.roric.RoricEffectEvent;
import platformer.event.events.roric.RoricTeleportEvent;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.spells.SpellManager;

import java.awt.*;
import java.util.Random;
import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canMoveHere;
import static platformer.physics.CollisionDetector.getGroundY;

/**
 * Handles the AI, attack patterns, and state transitions for the {@link Roric} boss.
 * This class acts as the "cognitive layer" for the boss, implementing a state machine that dictates Roric's behavior.
 * It makes decisions based on the current state, and player position, abstracting this complex logic from the core {@code Roric} entity class.
 *
 * @see Roric
 * @see RoricState
 */
public class RoricAttackHandler {

    private final Roric roric;
    private final RoricPhaseManager phaseManager;

    // Aerial State & Repositioning
    private RoricState queuedActionAfterJump = null;
    private boolean isFloating = false;
    private boolean isRepositioning = false;
    private boolean aerialAttackPerformedThisJump = false;
    private double repositionTargetX = 0;
    private static final double REPOSITION_SPEED = 3.5 * SCALE;
    private static final double REPOSITION_DISTANCE = 6.5 * TILES_SIZE;
    private static final double MIN_DISTANCE_AFTER_TELEPORT = 3 * TILES_SIZE;
    private static final int ARENA_EDGE_BUFFER_TILES = 3;
    private static final int ARENA_CENTER_BUFFER_TILES = 2;

    // Skyfall Barrage State
    private int skyfallBeamCount = 0;
    private int skyfallBeamTimer = 0;
    private static final int SKYFALL_BEAM_COOLDOWN = 200;
    private static final double PREDICTION_FACTOR = 75.0;

    // Celestial Rain State
    private boolean isSpawningVolley = false;
    private int celestialRainTimer = 0;
    private int volleyTimer = 0;
    private double currentSpawnAngle = 0;
    private int orbInVolleyIndex = 0;
    private int orbSpawnTimer = 0;
    private static final int ORB_SPAWN_COOLDOWN = 5;
    private static final int PROJECTILES_PER_VOLLEY = 10;
    private static final int VOLLEY_COOLDOWN = 25;
    private static final int CELESTIAL_RAIN_DURATION = 5500;

    // Arrow Strike State
    private static final double BLADE_STRIKE_TELEPORT_COOLDOWN = 9;

    // Beam Attack State
    private boolean beamChargeEventPublished = false;
    private boolean beamFiredThisAttack = false;

    private final Random random = new Random();

    public RoricAttackHandler(Roric roric, RoricPhaseManager phaseManager) {
        this.roric = roric;
        this.phaseManager = phaseManager;
    }

    /**
     * The main update loop for the AI. It acts as the central hub of the state machine, executing behavior corresponding to Roric's current {@link RoricState}.
     *
     * @param levelData        The level's collision map.
     * @param player           The player entity.
     * @param spellManager     Manager for creating spell effects.
     * @param enemyManager     Manager for creating enemy entities (clones).
     */
    public void update(int[][] levelData, Player player, SpellManager spellManager, EnemyManager enemyManager) {
        switch (roric.getState()) {
            case IDLE:
                handleIdleState(levelData);
                break;
            case JUMPING:
                handleJumpingState();
                break;
            case ARROW_STRIKE:
                handleBladeStrikeAttack(player);
                break;
            case AERIAL_ATTACK:
                if (!roric.isInAir()) {
                    finishAnimation();
                    return;
                }
                handleAerialAttack(levelData, player);
                break;
            case REPOSITIONING:
                handleRepositioning(player);
                break;
            case ARROW_ATTACK:
                handleArrowAttack();
                break;
            case BEAM_ATTACK:
                handleBeamAttack(spellManager);
                break;
            case ARROW_RAIN:
                break;
            case PHANTOM_BARRAGE:
                handlePhantomBarrage(enemyManager, levelData);
                break;
            case SKYFALL_BARRAGE:
                handleSkyfallBarrage(player, spellManager, levelData);
                break;
            case CELESTIAL_RAIN:
                handleCelestialRain();
                break;
            case COOLDOWN:
                handleCooldownState();
                break;
        }
    }

    /**
     * The decision-making node of the state machine. In the IDLE state, Roric waits for his global
     * attack cooldown. Once ready, he randomly chooses and transitions to a new attack state.
     *
     * @param levelData The level's collision map.
     */
    private void handleIdleState(int[][] levelData) {
        if (!roric.isStart() || roric.getCooldown()[0] > 0) return;

        Player player = roric.getCurrentPlayerTarget();
        if (player == null) return;

        RoricState nextAttack = phaseManager.chooseNextAttack();
        switch (nextAttack) {
            case JUMPING:
                roric.setState(RoricState.JUMPING);
                roric.jump(levelData);
                break;
            case BEAM_ATTACK:
                repositionForRangedAttack(RoricState.BEAM_ATTACK, levelData, player);
                beamFiredThisAttack = false;
                beamChargeEventPublished = false;
                break;
            case ARROW_RAIN:
                repositionForRangedAttack(RoricState.ARROW_RAIN, levelData, player);
                break;
            case PHANTOM_BARRAGE:
                roric.setState(RoricState.PHANTOM_BARRAGE);
                roric.setEnemyAction(Anim.SPELL_4);
                break;
            case ARROW_STRIKE:
                teleport(levelData, player, 1);
                targetPlayer(player);
                roric.setState(RoricState.ARROW_STRIKE);
                roric.setAttackCooldown(BLADE_STRIKE_TELEPORT_COOLDOWN * phaseManager.getCooldownModifier());
                roric.setEnemyAction(Anim.IDLE);
                roric.setAttackCheck(false);
                break;
            case ARROW_ATTACK:
                repositionForRangedAttack(RoricState.ARROW_ATTACK, levelData, player);
                break;
            case SKYFALL_BARRAGE:
                roric.setState(RoricState.SKYFALL_BARRAGE);
                startSkyfallBarrage();
                break;
            case CELESTIAL_RAIN:
                startCelestialRain(levelData);
                break;
        }
    }

    /**
     * Handles Roric's jumping state. If he has sufficient air speed, he transitions to the AERIAL_ATTACK state.
     * This is where he can perform aerial maneuvers and attacks.
     */
    private void handleJumpingState() {
        if (queuedActionAfterJump == null) {
            if (roric.getAirSpeed() >= 0 && !isFloating && !aerialAttackPerformedThisJump) {
                isFloating = true;
                roric.setAirSpeed(0);
                roric.setXSpeed(0);
                roric.setState(RoricState.AERIAL_ATTACK);
                aerialAttackPerformedThisJump = true;
            }
        }
    }

    /**
     * Handles the logic for the Blade Strike attack. The attack hitbox becomes active only during
     * specific animation frames (5 through 8) to synchronize with the visual sword swing.
     * The `attackCheck` flag ensures damage is applied only once per swing.
     *
     * @param player The player entity to check for hits.
     */
    private void handleBladeStrikeAttack(Player player) {
        if (roric.getCooldown()[0] > 0) return;
        if (roric.getEnemyAction() != Anim.ATTACK_1) {
            roric.setEnemyAction(Anim.ATTACK_1);
            roric.setAttackCheck(false);
        }
        if (roric.getAnimIndex() >= 5 && roric.getAnimIndex() <= 8 && !roric.isAttackCheck()) {
            roric.checkPlayerHit(roric.getAttackBox(), player);
            roric.setAttackCheck(true);
        }
    }

    /**
     * Handles Roric's primary aerial maneuver. He attempts to fire a trap arrow at the player.
     * If the player is in a geometric "blind spot" directly underneath him, he will perform a quick dash to reposition.
     *
     * @param levelData         The level's collision map.
     * @param player            The player entity.
     */
    private void handleAerialAttack(int[][] levelData, Player player) {
        if (isRepositioning) return;

        if (roric.getEnemyAction() != Anim.SPELL_2) {
            if (isPlayerInBlindSpot(player)) {
                roric.setState(RoricState.REPOSITIONING);
                setupRepositioning(player, levelData);
            }
            else roric.prepareToFire(player);
        }

        if (roric.getEnemyAction() == Anim.SPELL_2) {
            if (roric.getAnimIndex() == 6 && !roric.isAttackCheck()) {
                boolean dropTrap = phaseManager.shouldAerialAttackDropTrap();
                ProjectileFactory.createRoricAngledArrow(roric, player, dropTrap);
                roric.setAttackCheck(true);
            }
        }
    }

    /**
     * Executes a repositioning maneuver (jump or teleport) before a ranged attack, based on the current phase.
     *
     * @param attackState The attack Roric should perform after repositioning.
     * @param levelData The level's collision data.
     * @param player The player entity.
     */
    private void repositionForRangedAttack(RoricState attackState, int[][] levelData, Player player) {
        RoricPhaseManager.RoricPhase phase = phaseManager.getCurrentPhase();
        boolean shouldTeleport = false;

        switch (phase) {
            case INTRO:
                // Phase 1: 50/50 jump or teleport
                if (random.nextBoolean()) shouldTeleport = true;
                break;
            case ASSAULT:
                // Phase 2: 30/70 jump or teleport
                shouldTeleport = random.nextDouble() <= 0.7;
                break;
            case FINALE:
                // Phase 5: Always teleport
                shouldTeleport = true;
                break;
            default:
                // Phases 3 and 4 will also teleport
                shouldTeleport = true;
                break;
        }

        if (shouldTeleport) {
            teleport(levelData, player, 8);
            targetPlayer(player);
            roric.setState(attackState);
            switch (attackState) {
                case BEAM_ATTACK: roric.setEnemyAction(Anim.SPELL_1); break;
                case ARROW_RAIN:
                    roric.setEnemyAction(Anim.SPELL_3);
                    roric.getSpellManager().startArrowRainTelegraph(player);
                    break;
                case ARROW_ATTACK: roric.setEnemyAction(Anim.ATTACK_2); break;
            }
        }
        else {
            this.queuedActionAfterJump = attackState;
            roric.setState(RoricState.JUMPING);
            roric.jump(levelData);
        }
    }

    /**
     * Calculates the target position for an aerial repositioning dash.
     *
     * @param player The player entity.
     * @param levelData The level's collision map.
     */
    private void setupRepositioning(Player player, int[][] levelData) {
        isRepositioning = true;
        boolean playerIsToTheLeft = player.getHitBox().getCenterX() < roric.getHitBox().getCenterX();
        double idealDashTarget = playerIsToTheLeft ? roric.getHitBox().x + REPOSITION_DISTANCE : roric.getHitBox().x - REPOSITION_DISTANCE;
        double fallbackDashTarget = playerIsToTheLeft ? roric.getHitBox().x - REPOSITION_DISTANCE : roric.getHitBox().x + REPOSITION_DISTANCE;

        if (canDashTo(idealDashTarget, levelData)) repositionTargetX = idealDashTarget;
        else if (canDashTo(fallbackDashTarget, levelData)) repositionTargetX = fallbackDashTarget;
        else {
            isRepositioning = false;
            roric.setState(RoricState.AERIAL_ATTACK);
            roric.prepareToFire(player);
            return;
        }
        roric.setAnimIndex(9);
    }

    /**
     * Executes the aerial dash, performing a linear interpolation of the position towards the target.
     *
     * @param player The player entity.
     */
    private void handleRepositioning(Player player) {
        boolean finishedReposition = false;
        if (isRepositioning) {
            EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.REPOSITIONING));
        }
        if (roric.getHitBox().x < repositionTargetX) {
            roric.getHitBox().x += REPOSITION_SPEED;
            if (roric.getHitBox().x >= repositionTargetX) finishedReposition = true;
        }
        else {
            roric.getHitBox().x -= REPOSITION_SPEED;
            if (roric.getHitBox().x <= repositionTargetX) finishedReposition = true;
        }

        if (finishedReposition) {
            roric.getHitBox().x = repositionTargetX;
            isRepositioning = false;
            roric.setState(RoricState.AERIAL_ATTACK);
            roric.prepareToFire(player);
        }
    }

    /**
     * Fires a straight projectile during the ATTACK_2 animation.
     */
    private void handleArrowAttack() {
        if (roric.getAnimIndex() == 9 && !roric.isAttackCheck()) {
            ProjectileFactory.createRoricArrow(roric, phaseManager.getArrowSpeedMultiplier());
            roric.setAttackCheck(true);
        }
    }

    /**
     * Activates a beam attack during the SPELL_1 animation.
     */
    private void handleBeamAttack(SpellManager spellManager) {
        if (roric.getAnimIndex() == 1 && !beamChargeEventPublished) {
            EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.BEAM_CHARGE_START));
            beamChargeEventPublished = true;
        }
        if (roric.getAnimIndex() == 9 && !beamFiredThisAttack) {
            spellManager.activateRoricBeam(roric);
            EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.BEAM_CHARGE_END));
            beamFiredThisAttack = true;
        }
    }

    /**
     * Initiates the Phantom Barrage attack. Roric jumps and spawns a clone that fires at the player.
     *
     * @param enemyManager Manager used to spawn the Roric clone.
     * @param levelData    The level's collision map.
     */
    private void handlePhantomBarrage(EnemyManager enemyManager, int[][] levelData) {
        if (roric.getAnimIndex() == 0 && !roric.isAttackCheck()) {
            roric.setAttackCheck(true);
            Player player = roric.getCurrentPlayerTarget();
            if (player == null) return;

            double playerX = player.getHitBox().getCenterX();
            double groundY = getGroundY((levelData.length * TILES_SIZE) / 2.0, 0, levelData);
            boolean roricGoesRight = playerX < (levelData.length * TILES_SIZE) / 2.0;
            double leftSideX = 2.0 * TILES_SIZE;
            double rightSideX = (levelData.length - 4.0) * TILES_SIZE;
            int spawnX = roricGoesRight ? (int)leftSideX : (int)rightSideX;
            int spawnY = (int)(groundY - TILES_SIZE * 2 );
            teleportToSide();
            targetPlayer(player);

            EventBus.getInstance().publish(new RoricCloneEvent(new Point(spawnX, (int)roric.getHitBox().getCenterY())));

            roric.setState(RoricState.JUMPING);
            roric.jump(levelData);
            enemyManager.spawnRoricClone(spawnX, spawnY);
            roric.setAttackCooldown(15 * phaseManager.getCooldownModifier());
        }
    }

    /**
     * Handles the Skyfall Barrage attack, where Roric is off-screen.
     * It periodically spawns sky beams that target the player's predicted future position using a simple kinematic model with a stochastic element.
     *
     * @param player       The player entity.
     * @param spellManager The spell manager to create the beams.
     * @param levelData    The level's collision map.
     */
    private void handleSkyfallBarrage(Player player, SpellManager spellManager, int[][] levelData) {
        skyfallBeamTimer++;
        if (skyfallBeamTimer >= SKYFALL_BEAM_COOLDOWN) {
            skyfallBeamTimer = 0;
            if (skyfallBeamCount < 4) {
                double playerSpeedX = player.getHorizontalSpeed();
                double targetX = player.getHitBox().getCenterX() + playerSpeedX * PREDICTION_FACTOR * (random.nextDouble() + 0.5);
                targetX = Math.max(0, Math.min(targetX, levelData.length * TILES_SIZE));
                spellManager.spawnSkyBeamAt((int) targetX);
                skyfallBeamCount++;
            }
            else reappear();
        }
    }

    /**
     * Makes Roric reappear on one side of the arena after the Skyfall Barrage.
     */
    private void reappear() {
        roric.setVisible(true);

        Player player = roric.getCurrentPlayerTarget();
        if (player != null) {
            double playerX = player.getHitBox().getCenterX();
            double arenaCenterX = (roric.getLevelDataForAI().length * TILES_SIZE) / 2.0;
            if (playerX < arenaCenterX) {
                roric.getHitBox().x = 20 * TILES_SIZE;
                roric.setDirection(Direction.LEFT);
            }
            else {
                roric.getHitBox().x = 5 * TILES_SIZE;
                roric.setDirection(Direction.RIGHT);
            }
        }
        // Fallback
        else {
            roric.getHitBox().x = 12.5 * TILES_SIZE;
        }

        roric.setState(RoricState.IDLE);
        roric.setEnemyAction(Anim.IDLE);
        roric.setAttackCooldown(RORIC_IDLE_COOLDOWN * phaseManager.getCooldownModifier());
    }

    /**
     * Handles the Celestial Rain attack. Roric becomes stationary and spawns rotating volleys of celestial orbs
     * in a "bullet hell" spiral pattern. The angle of each volley is incremented to create the rotation.
     */
    private void handleCelestialRain() {
        celestialRainTimer++;
        if (isSpawningVolley) {
            orbSpawnTimer++;
            if (orbSpawnTimer >= ORB_SPAWN_COOLDOWN) {
                orbSpawnTimer = 0;
                double angleIncrement = Math.toRadians(360.0 / PROJECTILES_PER_VOLLEY);
                double angle = currentSpawnAngle + (orbInVolleyIndex * angleIncrement);
                ProjectileFactory.createCelestialOrb(roric, angle);
                orbInVolleyIndex++;
                if (orbInVolleyIndex >= PROJECTILES_PER_VOLLEY) {
                    isSpawningVolley = false;
                    currentSpawnAngle += Math.toRadians(25);
                }
            }
        }
        else {
            volleyTimer++;
            if (volleyTimer >= VOLLEY_COOLDOWN) {
                volleyTimer = 0;
                isSpawningVolley = true;
                orbInVolleyIndex = 0;
            }
        }
        if (celestialRainTimer >= CELESTIAL_RAIN_DURATION) stopCelestialRain();
    }

    private void handleCooldownState() {
        if (roric.getCooldown()[0] <= 0) roric.setState(RoricState.IDLE);
    }

    /**
     * Initiates the Skyfall Barrage by making Roric disappear.
     */
    private void startSkyfallBarrage() {
        roric.setVisible(false);
        skyfallBeamCount = 0;
        skyfallBeamTimer = 0;
        roric.setAttackCooldown(8);
    }

    /**
     * Initiates the Celestial Rain attack by moving Roric to the center of the arena.
     */
    public void startCelestialRain(int[][] levelData) {
        roric.getHitBox().x = (levelData.length * TILES_SIZE) / 2.0 - (roric.getHitBox().width / 2.0);
        roric.getHitBox().y = 4 * TILES_SIZE;
        roric.setInAir(true);
        roric.setAirSpeed(0);
        roric.setState(RoricState.CELESTIAL_RAIN);
        celestialRainTimer = 0;
        volleyTimer = 0;
        currentSpawnAngle = 0;
        EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.CELESTIAL_RAIN_START));
    }

    /**
     * Stops the Celestial Rain attack, resetting the state and cooldowns.
     */
    private void stopCelestialRain() {
        isSpawningVolley = false;
        roric.setState(RoricState.IDLE);
        roric.setEnemyAction(Anim.IDLE);
        roric.setAttackCooldown(RORIC_IDLE_COOLDOWN);
        EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.CELESTIAL_RAIN_END));
    }

    /**
     * Determines if the player is in Roric's "blind spot" directly below him.
     * This is a geometric check using trigonometry. It defines a conical region beneath Roric where he cannot effectively aim his aerial projectiles.
     * The condition `dx/dy < tan(30°)`checks if the angle from the vertical is less than 30 degrees.
     *
     * @param player The player entity.
     * @return True if the player is in the blind spot, false otherwise.
     */
    private boolean isPlayerInBlindSpot(Player player) {
        double dx = Math.abs(player.getHitBox().getCenterX() - roric.getHitBox().getCenterX());
        double dy = player.getHitBox().getCenterY() - roric.getHitBox().getCenterY();
        return (dy > 0 && (dx / dy) < Math.tan(Math.toRadians(30)));
    }

    private boolean canDashTo(double targetX, int[][] levelData) {
        return canMoveHere(targetX, roric.getHitBox().y, roric.getHitBox().width, roric.getHitBox().height, levelData);
    }

    private void targetPlayer(Player player) {
        if (player.getHitBox().getCenterX() < roric.getHitBox().getCenterX()) {
            roric.setDirection(Direction.LEFT);
        }
        else roric.setDirection(Direction.RIGHT);
    }

    /**
     * Called when an attack animation finishes. It transitions Roric to the next logical state.
     * For most attacks, this is back to IDLE. For aerial attacks, it transitions back to JUMPING to allow Roric to complete his fall.
     */
    public void finishAnimation() {
        if (roric.getState() == RoricState.ARROW_RAIN) {
            SpellManager spellManager = roric.getSpellManager();
            if (spellManager != null) {
                spellManager.activateArrowRain();
                spellManager.stopArrowRainTelegraph();
            }
        }

        roric.setAttackCheck(false);
        roric.setAnimIndex(0);

        if (roric.getState() == RoricState.AERIAL_ATTACK || roric.getState() == RoricState.REPOSITIONING) {
            isFloating = false;
            roric.setState(RoricState.JUMPING);
            roric.setEnemyAction(Anim.JUMP_FALL);
            roric.setAnimIndex(9);
        }
        else {
            roric.setState(RoricState.IDLE);
            roric.setEnemyAction(Anim.IDLE);
            roric.setAttackCooldown(RORIC_IDLE_COOLDOWN * phaseManager.getCooldownModifier());
            if (phaseManager.getCurrentPhase() == RoricPhaseManager.RoricPhase.BRIDGE)
                if (random.nextInt(100) < 30) teleportToSide();
        }
    }

    /**
     * Helper method to teleport Roric near the player. It attempts to teleport to either the left or right
     * of the player, picking a valid location that is not inside a wall.
     *
     * @param levelData The level's collision map.
     * @param player    The player entity to teleport near.
     * @param tiles     The distance, in tiles, to teleport away from the player.
     */
    private void teleport(int[][] levelData, Player player, int tiles) {
        Random rand = new Random();
        double playerX = player.getHitBox().x;
        double rightTeleport = playerX + tiles * TILES_SIZE;
        double leftTeleport = playerX - tiles * TILES_SIZE;
        int k = rand.nextInt(2);

        double targetX = roric.getHitBox().x;
        if (k == 0 && canMoveHere(rightTeleport, roric.getHitBox().y, roric.getHitBox().width, roric.getHitBox().height, levelData))
            targetX = rightTeleport;
        else if (canMoveHere(leftTeleport, roric.getHitBox().y, roric.getHitBox().width, roric.getHitBox().height, levelData))
            targetX = leftTeleport;

        performTeleport(targetX, roric.getHitBox().y);
        roric.setDirection((playerX < roric.getHitBox().x) ? Direction.LEFT : Direction.RIGHT);
    }

    /**
     * Teleports Roric to a strategic, semi-random location on either the left or right side of the arena.
     * <p>
     * This method implements tactical repositioning. It first randomly selects a target side (left or right).
     * It then calculates a potential teleport location within that side's safe zone.
     * To prevent Roric from teleporting directly onto the player, it checks if this location is too close. If it is,
     * Roric will forcefully teleport to the *other* side of the arena instead. This ensures the player
     * always has a minimum amount of space to react, making the attack feel fair but unpredictable.
     */
    public void teleportToSide() {
        int[][] levelData = roric.getLevelDataForAI();
        Player player = roric.getCurrentPlayerTarget();
        if (levelData == null || player == null) return;

        int levelWidthInTiles = levelData.length;
        int centerTile = levelWidthInTiles / 2;
        int leftZoneStart = ARENA_EDGE_BUFFER_TILES;
        int leftZoneEnd = centerTile - ARENA_CENTER_BUFFER_TILES;
        int rightZoneStart = centerTile + ARENA_CENTER_BUFFER_TILES;
        int rightZoneEnd = levelWidthInTiles - ARENA_EDGE_BUFFER_TILES;

        boolean tryRightSideFirst = random.nextBoolean();
        double potentialTargetX = tryRightSideFirst ? calcRandomXInZone(rightZoneStart, rightZoneEnd) : calcRandomXInZone(leftZoneStart, leftZoneEnd);

        boolean finalGoToRight = tryRightSideFirst;
        double distance = Math.abs(player.getHitBox().getCenterX() - potentialTargetX);
        if (distance < MIN_DISTANCE_AFTER_TELEPORT) finalGoToRight = !tryRightSideFirst;

        double finalTargetX = finalGoToRight ? calcRandomXInZone(rightZoneStart, rightZoneEnd) : calcRandomXInZone(leftZoneStart, leftZoneEnd);

        performTeleport(finalTargetX, roric.getHitBox().y);
        targetPlayer(player);
    }

    /**
     * Calculates a random horizontal pixel coordinate within a specified tile-based zone.
     *
     * @param startTile The starting tile index of the zone (inclusive).
     * @param endTile   The ending tile index of the zone (inclusive).
     * @return A random double value representing the x-coordinate in pixels.
     */
    private double calcRandomXInZone(int startTile, int endTile) {
        int range = endTile - startTile;
        if (range <= 0) return startTile * TILES_SIZE;
        int randomTile = startTile + random.nextInt(range);
        return randomTile * TILES_SIZE;
    }

    private void performTeleport(double newX, double newY) {
        EventBus.getInstance().publish(new RoricTeleportEvent(new Point((int) roric.getHitBox().getCenterX(), (int) roric.getHitBox().getCenterY()), false));
        roric.getHitBox().x = newX;
        roric.getHitBox().y = newY;
        EventBus.getInstance().publish(new RoricTeleportEvent(new Point((int) roric.getHitBox().getCenterX(), (int) roric.getHitBox().getCenterY()), true));
    }

    /**
     * Called when Roric lands on the ground, resetting aerial state flags and executing any queued actions.
     */
    public boolean onLanding() {
        this.aerialAttackPerformedThisJump = false;
        this.isFloating = false;

        EventBus.getInstance().publish(new RoricEffectEvent(roric, RoricEffectEvent.RoricEffectType.LAND));

        if (queuedActionAfterJump != null) {
            Player player = roric.getCurrentPlayerTarget();
            if (player != null) targetPlayer(player);
            roric.setState(queuedActionAfterJump);
            switch (queuedActionAfterJump) {
                case BEAM_ATTACK: roric.setEnemyAction(Anim.SPELL_1); break;
                case ARROW_RAIN:
                    roric.setEnemyAction(Anim.SPELL_3);
                    roric.getSpellManager().startArrowRainTelegraph(player);
                    break;
                case ARROW_ATTACK: roric.setEnemyAction(Anim.ATTACK_2); break;
            }
            queuedActionAfterJump = null;
            return true;
        }
        return false;
    }

    /**
     * Forces Roric to immediately stop his current action and return to an idle state.
     * This is crucial for clean phase transitions.
     */
    public void interruptAndIdle() {
        if (phaseManager.getCurrentPhase() == RoricPhaseManager.RoricPhase.ASSAULT) return;
        isFloating = false;
        isSpawningVolley = false;
        isRepositioning = false;
        roric.setAttackCheck(false);
        skyfallBeamCount = 0;
        celestialRainTimer = 0;

        roric.getSpellManager().stopArrowRainTelegraph();
        roric.setState(RoricState.COOLDOWN);
        roric.setEnemyAction(Anim.IDLE);
        if (phaseManager.getCurrentPhase() == RoricPhaseManager.RoricPhase.FINALE) {
            roric.setAttackCooldown(RORIC_IDLE_COOLDOWN * 4);
        }
        else roric.setAttackCooldown(0);
        roric.setVisible(true);
    }

    /**
     * Initiates Roric's scripted opening attack sequence.
     * This method is called once at the very beginning of the fight.
     *
     * @param player The player to target.
     */
    public void startOpeningAttack(Player player) {
        int[][] levelData = roric.getLevelDataForAI();
        double centerX = (levelData.length * TILES_SIZE) / 2.0;
        double startY = roric.getHitBox().y;
        performTeleport(centerX, startY);
        targetPlayer(player);
        roric.setState(RoricState.BEAM_ATTACK);
        roric.setEnemyAction(Anim.SPELL_1);
        beamFiredThisAttack = false;
    }

    /**
     * Resets the handler's state variables for a new fight.
     */
    public void reset() {
        isFloating = false;
        isRepositioning = false;
        aerialAttackPerformedThisJump = false;
        skyfallBeamCount = 0;
        skyfallBeamTimer = 0;
        celestialRainTimer = 0;
        isSpawningVolley = false;
        roric.setVisible(true);
        queuedActionAfterJump = null;
    }

    public boolean isFloating() {
        return isFloating;
    }
}