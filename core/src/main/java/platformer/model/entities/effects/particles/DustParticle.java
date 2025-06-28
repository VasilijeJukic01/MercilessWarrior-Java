package platformer.model.entities.effects.particles;

import platformer.model.entities.Entity;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;

public class DustParticle {

    private Rectangle2D.Double particleShape;
    private final DustType type;

    private double xSpeed;
    private double ySpeed;
    private double gravity;
    private boolean active = true;

    private float initialAlpha;
    private float currentAlpha;
    private float alphaFadeSpeed;
    private Color particleColor = DUST_COLOR;

    private final Entity target;
    private Point2D.Double offset;
    private double angle;
    private double pulsePhase;

    private int lineLength;
    private double lineAngle;

    public DustParticle(int x, int y, int size, DustType type, int playerFlipSign, Entity target) {
        this.type = type;
        this.target = target;
        this.particleShape = new Rectangle2D.Double(x, y, size, size);
        this.initialAlpha = 0.7f;
        this.currentAlpha = initialAlpha;

        switch (type) {
            case IMPACT -> initImpact();
            case DASH -> initDash(x, y, playerFlipSign);
            case RUNNING -> initRunning();
            case WALL_SLIDE -> initWallSlide(playerFlipSign);
            case WALL_JUMP -> initWallJump(playerFlipSign);
            case CRITICAL_HIT -> initCriticalHit(x, y);
            case PLAYER_HIT -> initPlayerHit();
            case SW_TELEPORT -> initTeleport();
            case SW_CHANNELING_AURA -> initChannelingAura(x, y, size);
            case SW_AURA_PULSE -> initAuraPulse(x, y, size);
            case SW_AURA_CRACKLE -> initAuraCrackle(x, y, size);
            case SW_DASH_SLASH -> initDashSlash();
            default -> initDefault(x, y, size);
        }
    }

    private void initImpact() {
        Random rand = new Random();
        this.ySpeed = -1 * (rand.nextDouble() + 0.3) * SCALE;
        this.xSpeed = (rand.nextDouble() - 0.5) * 0.8 * SCALE;
        this.gravity = 0.08 * SCALE;
        this.alphaFadeSpeed = 0.02f;
    }

    private void initDash(int x, int y, int playerFlipSign) {
        Random rand = new Random();
        int streakWidth = (int)((rand.nextInt(10) + 15) * SCALE);
        int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
        this.particleShape = new Rectangle2D.Double(x, y, streakWidth, streakHeight);
        this.particleColor = DUST_COLOR_DASH;
        this.ySpeed = (rand.nextDouble() - 0.5) * 0.15 * SCALE;
        this.xSpeed = -playerFlipSign * (rand.nextDouble() * 1.5 + 0.5) * SCALE;
        this.gravity = 0;
        this.alphaFadeSpeed = 0.05f;
    }

    private void initRunning() {
        Random rand = new Random();
        this.ySpeed = -0.4 * (rand.nextDouble()) * SCALE;
        this.xSpeed = (rand.nextDouble() - 0.5) * 0.1 * SCALE;
        this.gravity = 0.03 * SCALE;
        this.alphaFadeSpeed = 0.015f;
    }

    private void initWallSlide(int playerFlipSign) {
        Random rand = new Random();
        this.xSpeed = (rand.nextDouble() * 0.5) * SCALE * -playerFlipSign;
        this.ySpeed = (rand.nextDouble() * 0.5) * SCALE;
        this.gravity = 0.08 * SCALE;
        this.alphaFadeSpeed = 0.02f;
        this.particleColor = DUST_COLOR;
        double size = (rand.nextInt(4) + 4) * SCALE;
        this.particleShape.width = size;
        this.particleShape.height = size;
    }

    private void initWallJump(int playerFlipSign) {
        Random rand = new Random();
        this.xSpeed = (rand.nextDouble() * 1.5 + 0.5) * SCALE * -playerFlipSign;
        this.ySpeed = -(rand.nextDouble() * 1.0 + 0.5) * SCALE;
        this.gravity = 0.08 * SCALE;
        this.alphaFadeSpeed = 0.04f;
        this.particleColor = DUST_COLOR;
        double size = (rand.nextInt(5) + 3) * SCALE;
        this.particleShape.width = size;
        this.particleShape.height = size;
    }

    private void initCriticalHit(int x, int y) {
        Random rand = new Random();
        int streakWidth = (int)((rand.nextInt(10) + 20) * SCALE);
        int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
        this.particleShape = new Rectangle2D.Double(x, y, streakWidth, streakHeight);
        this.particleColor = new Color(255, 60, 30);

        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 3.0 + 2.0) * SCALE;
        this.xSpeed = Math.cos(angle) * speed;
        this.ySpeed = Math.sin(angle) * speed;

        this.gravity = 0.05 * SCALE;
        this.alphaFadeSpeed = 0.04f;
        this.currentAlpha = 1.0f;
    }

    private void initPlayerHit() {
        Random rand = new Random();
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.5 + 1.0) * SCALE;
        this.xSpeed = Math.cos(angle) * speed;
        this.ySpeed = Math.sin(angle) * speed;
        this.gravity = 0.05 * SCALE;
        this.alphaFadeSpeed = 0.025f;
        this.particleColor = new Color(255, 40, 40);
        this.currentAlpha = 1.0f;
        double size = (rand.nextInt(3) + 4) * SCALE;
        this.particleShape.width = size;
        this.particleShape.height = size;
    }

    private void initTeleport() {
        Random rand = new Random();
        this.particleColor = new Color(150, 50, 255);
        this.currentAlpha = 0.9f;
        this.alphaFadeSpeed = 0.025f;

        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 1.0 + 0.5) * SCALE;
        this.xSpeed = Math.cos(angle) * speed;
        this.ySpeed = Math.sin(angle) * speed;
        this.gravity = 0;
    }

    private void initChannelingAura(int x, int y, int size) {
        Random rand = new Random();
        this.particleColor = new Color(0, 168, 255, 150);
        this.currentAlpha = 0.0f;
        this.alphaFadeSpeed = -0.025f;
        double radius = (rand.nextDouble() * 20 + 40) * SCALE;
        this.angle = rand.nextDouble() * 2 * Math.PI;
        this.offset = new Point2D.Double(radius, 0);
        this.particleShape = new Rectangle2D.Double(x, y, size, size);
        this.gravity = 0;
        this.xSpeed = 0.03;
        this.ySpeed = 0;
    }

    private void initAuraPulse(int x, int y, int size) {
        Random rand = new Random();
        this.particleColor = new Color(180, 224, 255, 200);
        this.currentAlpha = 0.0f;
        this.alphaFadeSpeed = -0.035f;
        double radius = (rand.nextDouble() * 15 + 10) * SCALE;
        this.angle = rand.nextDouble() * 2 * Math.PI;
        this.offset = new Point2D.Double(radius, 0);
        this.pulsePhase = rand.nextDouble() * Math.PI;
        this.particleShape = new Rectangle2D.Double(x, y, size, size);
        this.gravity = 0;
        this.xSpeed = 0;
        this.ySpeed = 0;
    }

    private void initAuraCrackle(int x, int y, int size) {
        Random rand = new Random();
        this.particleColor = new Color(255, 255, 255, 220);
        this.currentAlpha = 1.0f;
        this.alphaFadeSpeed = 0.15f;
        double speed = (rand.nextDouble() * 2.0 + 1.0) * SCALE;
        double sparkAngle = rand.nextDouble() * 2 * Math.PI;
        this.xSpeed = Math.cos(sparkAngle) * speed;
        this.ySpeed = Math.sin(sparkAngle) * speed;
        this.particleShape = new Rectangle2D.Double(x, y, size / 2.0, size / 2.0);
        this.gravity = 0;
    }

    private void initDashSlash() {
        Random rand = new Random();
        this.xSpeed = (rand.nextDouble() - 0.5) * 0.3 * SCALE;
        this.ySpeed = (rand.nextDouble() - 0.5) * 0.3 * SCALE;
        this.gravity = 0;
        this.alphaFadeSpeed = 0.06f;
        this.particleColor = new Color(0, 255, 240);
        this.currentAlpha = 0.9f;
        this.lineLength = (int)((rand.nextInt(10) + 8) * SCALE);
        this.lineAngle = rand.nextDouble() * Math.PI;
    }


    private void initDefault(int x, int y, int size) {
        Random rand = new Random();
        this.particleShape = new Rectangle2D.Double(x, y, size, size);
        this.particleColor = new Color(255, 255, 200);
        this.currentAlpha = 1.0f;
        this.alphaFadeSpeed = 0.035f;

        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.5 + 1.5) * SCALE;
        this.xSpeed = Math.cos(angle) * speed;
        this.ySpeed = Math.sin(angle) * speed;
        this.gravity = 0.08 * SCALE;
    }

    // Core
    public void update() {
        if (!active) return;

        switch (type) {
            case SW_CHANNELING_AURA:
                angle += xSpeed;
                double centerX = target.getHitBox().getCenterX();
                double centerY = target.getHitBox().getCenterY();
                particleShape.x = centerX + offset.x * Math.cos(angle);
                particleShape.y = centerY + offset.x * Math.sin(angle);
                break;
            case SW_AURA_PULSE:
                double cX = target.getHitBox().getCenterX();
                double cY = target.getHitBox().getCenterY();
                double pulseAmount = 15 * SCALE;
                double currentRadius = offset.x + Math.sin(pulsePhase) * pulseAmount;
                particleShape.x = cX + currentRadius * Math.cos(angle);
                particleShape.y = cY + currentRadius * Math.sin(angle);
                pulsePhase += 0.1;
                if (currentAlpha >= 0.9f) alphaFadeSpeed = 0.03f;
                break;
            default:
                particleShape.x += xSpeed;
                particleShape.y += ySpeed;
                particleShape.y += gravity;
                break;
        }
        currentAlpha -= alphaFadeSpeed;
        if (currentAlpha <= 0) {
            currentAlpha = 0;
            active = false;
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(particleColor.getRed()/255f, particleColor.getGreen()/255f, particleColor.getBlue()/255f, currentAlpha));

        if (type == DustType.SW_DASH_SLASH) renderDashSlashTrail(g2d, xLevelOffset, yLevelOffset);
        else g2d.fillRect((int) (particleShape.x - xLevelOffset), (int) (particleShape.y - yLevelOffset), (int) particleShape.width, (int) particleShape.height);
    }

    private void renderDashSlashTrail(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        int x1 = (int) (particleShape.x - xLevelOffset);
        int y1 = (int) (particleShape.y - yLevelOffset);
        int x2 = x1 + (int) (lineLength * Math.cos(lineAngle));
        int y2 = y1 + (int) (lineLength * Math.sin(lineAngle));

        g2d.setStroke(new BasicStroke(2 * SCALE));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(new BasicStroke(1));
    }

    // Getters and Setters
    public boolean isActive() {
        return active;
    }

    public Entity getTarget() {
        return target;
    }

    public DustType getType() {
        return type;
    }
}
