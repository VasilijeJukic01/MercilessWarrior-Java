package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Enemy;

import java.awt.*;

public interface EnemyRenderer<T extends Enemy> {

    void render(Graphics g, T enemy, int xLevelOffset, int yLevelOffset);

}
