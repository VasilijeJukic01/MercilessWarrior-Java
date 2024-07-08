package platformer.state;

/**
 * Different states that the game can be in while it is being played.
 * Each state corresponds to a different aspect of the game, such as pausing, game over, shopping, blacksmithing, dialogue, dying, saving, inventory, crafting, and looting.
 * The game's behavior changes depending on the current playing state.
 */
public enum PlayingState {
    PAUSE,
    GAME_OVER,
    SHOP,
    BLACKSMITH,
    DIALOGUE,
    DYING,
    SAVE,
    INVENTORY,
    CRAFTING,
    LOOTING
}
