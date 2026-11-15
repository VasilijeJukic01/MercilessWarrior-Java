package platformer.audio.types;

import platformer.utils.ValueEnum;

/**
 * Enum representing all available sound effects in the game.
 * Implements {@link platformer.utils.ValueEnum} to associate each enum constant with its corresponding file name (without the .wav extension).
 */
public enum Sound implements ValueEnum<String> {
    // Player
    SLASH_1("player/SwordSlash1"),
    SLASH_2("player/SwordSlash2"),
    SLASH_3("player/SwordSlash3"),
    DASH("player/Dash"),

    // Combat
    ENEMY_HIT_1("combat/EnemyHit1"),
    ENEMY_HIT_2("combat/EnemyHit2"),
    ENEMY_BLOCK_1("combat/EnemyBlock1"),
    ENEMY_BLOCK_2("combat/EnemyBlock2"),
    SWORD_BLOCK_1("combat/SwordBlock1"),
    SWORD_BLOCK_2("combat/SwordBlock2"),
    SWORD_BLOCK_3("combat/SwordBlock3"),

    // Environment
    CRATE_BREAK_1("env/CrateBreak1"),
    CRATE_BREAK_2("env/CrateBreak2"),
    ARROW("env/ArrowLaunch"),
    COIN_PICK("env/CoinPick"),
    CRAFTING("env/Crafting"),

    // UI
    GAME_OVER("ui/GameOver"),
    SHOP_BUY("ui/Buy"),
    BTN_CLICK("ui/ButtonClick"),

    // Magic
    FIRE_SPELL_1("magic/Flames"),
    LIGHTNING_1("magic/Lightning1"),
    LIGHTNING_2("magic/Lightning2"),
    LIGHTNING_3("magic/Lightning3"),
    FIREBALL("magic/FireballLaunch"),

    // Enemies
    GHOUL_HIDE("enemies/GhoulHide"),
    GHOUL_REVEAL("enemies/GhoulReveal"),
    GHOUL_DEATH("enemies/GhoulDeath"),
    SKELETON_DEATH_1("enemies/SkeletonDeath"),

    // Lancer
    LANCER_CHARGE("bosses/lancer/LancerCharge"),
    LANCER_CUT_1("bosses/lancer/LancerCut1"),
    LANCER_CUT_2("bosses/lancer/LancerCut2"),
    LANCER_GRUNT_1("bosses/lancer/LancerGrunt1"),
    LANCER_GRUNT_2("bosses/lancer/LancerGrunt2"),
    LANCER_GRUNT_3("bosses/lancer/LancerGrunt3"),
    LANCER_HAH_1("bosses/lancer/LancerHah1"),
    LANCER_HAH_2("bosses/lancer/LancerHah2"),
    LANCER_HAH_3("bosses/lancer/LancerHah3"),
    LANCER_HAH_4("bosses/lancer/LancerHah4"),
    LANCER_METAL_SLASH_1("bosses/lancer/LancerMetalSlash1"),
    LANCER_METAL_SLASH_2("bosses/lancer/LancerMetalSlash2"),
    LANCER_ORB_LAUNCH("bosses/lancer/LancerOrbLaunch"),
    LANCER_SLASH_1("bosses/lancer/LancerSlash1"),
    LANCER_SLASH_2("bosses/lancer/LancerSlash2"),
    LANCER_TRANSFORMING("bosses/lancer/LancerTransforming"),

    // Anita
    ANITA_ATTACK_1("followers/anita/AnitaAttack1"),
    ANITA_ATTACK_2("followers/anita/AnitaAttack2"),
    ANITA_HIT_1("followers/anita/AnitaHit1"),
    ANITA_HIT_2("followers/anita/AnitaHit2"),
    ANITA_KNOCKDOWN("followers/anita/AnitaKnockdown"),
    ANITA_REVIVE("followers/anita/AnitaRevive"),
    ANITA_SLASH_1("followers/anita/AnitaSlash1"),
    ANITA_SLASH_2("followers/anita/AnitaSlash2"),
    ANITA_WIND_SPELL("followers/anita/AnitaWindSpell");


    private static final String PREFIX = "sfx/";
    private final String value;

    Sound(String fileName) {
        this.value = PREFIX + fileName;
    }

    @Override
    public String getValue() {
        return value;
    }
}