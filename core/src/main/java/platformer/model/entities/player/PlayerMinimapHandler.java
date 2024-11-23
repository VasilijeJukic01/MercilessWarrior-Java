package platformer.model.entities.player;

import platformer.model.minimap.MinimapManager;

import static platformer.constants.Constants.TILES_SIZE;

public class PlayerMinimapHandler {

    private final Player player;

    private final MinimapManager minimapManager;
    private boolean activateMinimap;
    private int previousTileX;
    private int previousTileY;

    public PlayerMinimapHandler(Player player, MinimapManager minimapManager) {
        this.player = player;
        this.minimapManager = minimapManager;
    }

    public void update() {
        if (!activateMinimap) return;

        int tileX = (int) (player.getHitBox().x / TILES_SIZE);
        int tileY = (int) (player.getHitBox().y / TILES_SIZE);

        if (tileX != previousTileX || tileY != previousTileY) {
            int dx = tileX - previousTileX;
            int dy = tileY - previousTileY;
            minimapManager.updatePlayerPosition(dx, dy);
            previousTileX = tileX;
            previousTileY = tileY;
        }
    }

    public void activateMinimap(boolean activateMinimap) {
        this.activateMinimap = activateMinimap;
        previousTileX = 0;
        previousTileY = -1;
    }
}
