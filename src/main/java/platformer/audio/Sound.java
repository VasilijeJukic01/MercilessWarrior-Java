package platformer.audio;

import platformer.utils.ValueEnum;

public enum Sound implements ValueEnum<String> {
    SLASH_1("slash1"),                  // 0
    SLASH_2("slash2"),                  // 1
    SLASH_3("slash3"),                  // 2
    ENEMY_HIT_1("enemyHit1"),           // 3
    ENEMY_HIT_2("enemyHit2"),           // 4
    GAME_OVER("gameOver"),              // 5
    CRATE_BREAK_1("crateBreak1"),       // 6
    CRATE_BREAK_2("crateBreak2"),       // 7
    SKELETON_DEATH_1("skeletonD1"),     // 8
    DASH("dash"),                       // 9
    ARROW("arrowSound"),                // 10
    ENEMY_BLOCK_1("enemyBlock1"),       // 11
    ENEMY_BLOCK_2("enemyBlock2"),       // 12
    SWORD_BLOCK_1("swordBlock1"),       // 13
    SWORD_BLOCK_2("swordBlock2"),       // 14
    SWORD_BLOCK_3("swordBlock3"),       // 15
    FIRE_SPELL_1("fireSpell1"),         // 16
    GHOUL_HIDE("ghoulHide"),            // 17
    GHOUL_REVEAL("ghoulReveal"),        // 18
    GHOUL_DEATH("ghoulDeath"),          // 19
    COIN_PICK("coin"),                  // 20
    SHOP_BUY("buySound"),               // 21
    LIGHTNING_1("lightning1"),          // 22
    LIGHTNING_2("lightning2"),          // 23
    LIGHTNING_3("lightning3"),          // 24
    SW_ROAR_1("swRoar1"),               // 25
    SW_ROAR_2("swRoar2"),               // 26
    SW_ROAR_3("swRoar3"),               // 27
    FIREBALL("fireball");               // 28

    private final String value;

    Sound(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
