package platformer.audio;

import platformer.utils.ValueEnum;

public enum Sound implements ValueEnum<String> {
    SLASH_1("SwordSlash1"),             // 0
    SLASH_2("SwordSlash2"),             // 1
    SLASH_3("SwordSlash3"),             // 2
    ENEMY_HIT_1("EnemyHit1"),           // 3
    ENEMY_HIT_2("EnemyHit2"),           // 4
    GAME_OVER("GameOver"),              // 5
    CRATE_BREAK_1("CrateBreak1"),       // 6
    CRATE_BREAK_2("CrateBreak2"),       // 7
    SKELETON_DEATH_1("SkeletonDeath"),  // 8
    DASH("Dash"),                       // 9
    ARROW("ArrowLaunch"),               // 10
    ENEMY_BLOCK_1("EnemyBlock1"),       // 11
    ENEMY_BLOCK_2("EnemyBlock2"),       // 12
    SWORD_BLOCK_1("SwordBlock1"),       // 13
    SWORD_BLOCK_2("SwordBlock2"),       // 14
    SWORD_BLOCK_3("SwordBlock3"),       // 15
    FIRE_SPELL_1("Flames"),             // 16
    GHOUL_HIDE("GhoulHide"),            // 17
    GHOUL_REVEAL("GhoulReveal"),        // 18
    GHOUL_DEATH("GhoulDeath"),          // 19
    COIN_PICK("CoinPick"),              // 20
    SHOP_BUY("Buy"),                    // 21
    LIGHTNING_1("Lightning1"),          // 22
    LIGHTNING_2("Lightning2"),          // 23
    LIGHTNING_3("Lightning3"),          // 24
    SW_ROAR_1("Boss1Roar1"),            // 25
    SW_ROAR_2("Boss1Roar2"),            // 26
    SW_ROAR_3("Boss1Roar3"),            // 27
    FIREBALL("FireballLaunch"),         // 28
    CRAFTING("Crafting"),               // 29
    BTN_CLICK("ButtonClick");           // 30

    private final String value;

    Sound(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
