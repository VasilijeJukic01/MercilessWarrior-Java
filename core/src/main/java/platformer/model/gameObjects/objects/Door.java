package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Door extends GameObject implements Interactable {

    private boolean active;
    private final String destination;

    public Door(ObjType objType, int xPos, int yPos, String destination) {
        super(objType, xPos, yPos);
        this.destination = destination;

        initHitBox(TILES_SIZE * 1.5, TILES_SIZE * 1.5);
        hitBox.y -= (TILES_SIZE * 0.5);
        hitBox.x += (7 * SCALE );
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void onEnter(Player player) {
        this.active = true;
    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {
        this.active = false;
    }

    @Override
    public String getInteractionPrompt() {
        return "Door:" + destination;
    }
}