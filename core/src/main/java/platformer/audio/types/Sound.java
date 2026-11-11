package platformer.audio.types;

import platformer.utils.ValueEnum;

/**
 * Enum representing all available sound effects in the game.
 * Implements {@link platformer.utils.ValueEnum} to associate each enum constant with its corresponding file name (without the .wav extension).
 */
public enum Sound implements ValueEnum<String> {
    SLASH_1("SwordSlash1"),                     // 0
    SLASH_2("SwordSlash2"),                     // 1
    SLASH_3("SwordSlash3"),                     // 2
    ENEMY_HIT_1("EnemyHit1"),                   // 3
    ENEMY_HIT_2("EnemyHit2"),                   // 4
    GAME_OVER("GameOver"),                      // 5
    CRATE_BREAK_1("CrateBreak1"),               // 6
    CRATE_BREAK_2("CrateBreak2"),               // 7
    SKELETON_DEATH_1("SkeletonDeath"),          // 8
    DASH("Dash"),                               // 9
    ARROW("ArrowLaunch"),                       // 10
    ENEMY_BLOCK_1("EnemyBlock1"),               // 11
    ENEMY_BLOCK_2("EnemyBlock2"),               // 12
    SWORD_BLOCK_1("SwordBlock1"),               // 13
    SWORD_BLOCK_2("SwordBlock2"),               // 14
    SWORD_BLOCK_3("SwordBlock3"),               // 15
    FIRE_SPELL_1("Flames"),                     // 16
    GHOUL_HIDE("GhoulHide"),                    // 17
    GHOUL_REVEAL("GhoulReveal"),                // 18
    GHOUL_DEATH("GhoulDeath"),                  // 19
    COIN_PICK("CoinPick"),                      // 20
    SHOP_BUY("Buy"),                            // 21
    LIGHTNING_1("Lightning1"),                  // 22
    LIGHTNING_2("Lightning2"),                  // 23
    LIGHTNING_3("Lightning3"),                  // 24
    FIREBALL("FireballLaunch"),                 // 25
    CRAFTING("Crafting"),                       // 26
    BTN_CLICK("ButtonClick"),                   // 27
    LANCER_CHARGE("LancerCharge"),              // 28
    LANCER_CUT_1("LancerCut1"),                 // 29
    LANCER_CUT_2("LancerCut2"),                 // 30
    LANCER_GRUNT_1("LancerGrunt1"),             // 31
    LANCER_GRUNT_2("LancerGrunt2"),             // 32
    LANCER_GRUNT_3("LancerGrunt3"),             // 33
    LANCER_HAH_1("LancerHah1"),                 // 34
    LANCER_HAH_2("LancerHah2"),                 // 35
    LANCER_HAH_3("LancerHah3"),                 // 36
    LANCER_HAH_4("LancerHah4"),                 // 37
    LANCER_METAL_SLASH_1("LancerMetalSlash1"),  // 38
    LANCER_METAL_SLASH_2("LancerMetalSlash2"),  // 39
    LANCER_ORB_LAUNCH("LancerOrbLaunch"),       // 40
    LANCER_SLASH_1("LancerSlash1"),             // 41
    LANCER_SLASH_2("LancerSlash2"),             // 42
    LANCER_TRANSFORMING("LancerTransforming");  // 43

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