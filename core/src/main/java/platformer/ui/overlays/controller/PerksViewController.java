package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.perks.Perk;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.PerksOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Handles mouse and spatial keyboard navigation for the graph-based Mastery Perks tree.
 */
public class PerksViewController {

    private final GameContext context;
    private final PerksOverlay perksOverlay;
    private int selectedSlotNumber = 0;
    private int hoveredSlot = -1;

    public PerksViewController(GameContext context, PerksOverlay perksOverlay) {
        this.context = context;
        this.perksOverlay = perksOverlay;
        findFirstAvailablePerk();
    }

    private void findFirstAvailablePerk() {
        context.getPerksManager().getPerks().stream()
                .min(Comparator.comparingInt(Perk::getSlot))
                .ifPresent(p -> this.selectedSlotNumber = p.getSlot());
    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, perksOverlay.getButtons());
        if (changeSlot(e) && SwingUtilities.isRightMouseButton(e)) {
            upgrade();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) return;
        for (AbstractButton button : perksOverlay.getButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY -> upgrade();
                    case CLOSE, LEAVE -> EventBus.getInstance().publish(new OverlayChangeEvent(null));
                    default -> {}
                }
                break;
            }
        }
        Arrays.stream(perksOverlay.getButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, perksOverlay.getButtons());
        hoveredSlot = -1;
        for (Perk perk : context.getPerksManager().getPerks()) {
            if (perksOverlay.getNodeBounds(perk).contains(e.getPoint())) {
                hoveredSlot = perk.getSlot();
                break;
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            upgrade();
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> navigateSpatial(0, -1);
            case KeyEvent.VK_DOWN -> navigateSpatial(0, 1);
            case KeyEvent.VK_LEFT -> navigateSpatial(-1, 0);
            case KeyEvent.VK_RIGHT -> navigateSpatial(1, 0);
        }
    }

    /**
     * Finds the nearest perk node in the requested 2D direction (dx, dy).
     */
    private void navigateSpatial(int dirX, int dirY) {
        Perk current = perksOverlay.getPerkAtSlot(selectedSlotNumber);
        if (current == null) return;
        Point curPt = perksOverlay.getNodeCenter(current);

        Perk bestCandidate = null;
        double bestScore = Double.MAX_VALUE;

        for (Perk candidate : context.getPerksManager().getPerks()) {
            if (candidate.getSlot() == current.getSlot()) continue;
            Point pt = perksOverlay.getNodeCenter(candidate);
            double vx = pt.x - curPt.x;
            double vy = pt.y - curPt.y;

            double proj = vx * dirX + vy * dirY;
            if (proj <= 4) continue;

            double perp = Math.abs(vx * dirY - vy * dirX);
            double score = proj + perp * 2.2;
            if (score < bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate != null) {
            selectedSlotNumber = bestCandidate.getSlot();
        }
    }

    private boolean changeSlot(MouseEvent e) {
        for (Perk perk : context.getPerksManager().getPerks()) {
            if (perksOverlay.getNodeBounds(perk).contains(e.getPoint())) {
                this.selectedSlotNumber = perk.getSlot();
                return true;
            }
        }
        return false;
    }

    private void upgrade() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
        context.getPerksManager().upgrade(selectedSlotNumber);
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    public int getSlotNumber() {
        return selectedSlotNumber;
    }

    public int getHoveredSlot() {
        return hoveredSlot;
    }
}