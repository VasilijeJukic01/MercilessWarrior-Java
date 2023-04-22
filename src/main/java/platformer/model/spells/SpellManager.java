package platformer.model.spells;

import platformer.model.Tiles;
import platformer.model.entities.Player;
import platformer.state.PlayingState;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class SpellManager {

    private final PlayingState playingState;
    private final Flames flames;

    public SpellManager(PlayingState playingState) {
        this.playingState = playingState;
        Rectangle2D.Double hitBox = playingState.getPlayer().getHitBox();
        this.flames = new Flames(SpellType.FLAME_1, (int)hitBox.x, (int)hitBox.x,  (int)(hitBox.width*2.5),  (int)(hitBox.height-8.5*Tiles.SCALE.getValue()));
    }

    private void updateFlames() {
        Player player = playingState.getPlayer();
        flames.getHitBox().x = playingState.getPlayer().getHitBox().x - flames.getXOffset();
        flames.getHitBox().y = playingState.getPlayer().getHitBox().y - flames.getYOffset();
        if (player.getFlipSign() == 1) flames.getHitBox().x = flames.getHitBox().x + flames.getHitBox().width + (int)(38*Tiles.SCALE.getValue());
        flames.setAlive(playingState.getPlayer().isSpell());
    }

    // Core
    public void update() {
        updateFlames();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flames.isAlive()) flames.render(g, xLevelOffset, yLevelOffset);
    }

    public Flames getFlames() {
        return flames;
    }
}
