package platformer.model.entities.player;

import platformer.model.minimap.MinimapManager;

public class PlayerMinimapHandler {

    private final Player player;

    private final MinimapManager minimapManager;
    private boolean activateMinimap;

    public PlayerMinimapHandler(Player player, MinimapManager minimapManager) {
        this.player = player;
        this.minimapManager = minimapManager;
    }

    public void update() {
        if (!activateMinimap) return;
        minimapManager.updatePlayerPosition(player.getHitBox().getCenterX(), player.getHitBox().getCenterY());
    }

    public void activateMinimap(boolean activateMinimap) {
        this.activateMinimap = activateMinimap;
    }
}
