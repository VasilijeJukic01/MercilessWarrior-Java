package platformer.ui.dnd;

import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.Constants.SCALE;
import static platformer.constants.UI.ITEM_SIZE;

/**
 * Manages the state and rendering for a drag-and-drop operation within the UI.
 * It tracks the item being dragged, its origin, and its current position, differentiating between a mouse press ("armed") and an actual drag gesture.
 */
public class DragAndDropManager {

    private InventoryItem draggedItem;
    private DragSourceType source;
    private int sourceIndex;
    private Point dragPosition;
    private boolean dragging;

    private Point pressPosition;

    /**
     * Initiates the visual dragging state for an item.
     * This should be called after a drag has been confirmed (mouse moved past a threshold).
     *
     * @param item        The item being dragged.
     * @param source      The source panel of the item.
     * @param sourceIndex The index of the item in its source container.
     */
    public void startDrag(InventoryItem item, DragSourceType source, int sourceIndex) {
        this.draggedItem = item;
        this.source = source;
        this.sourceIndex = sourceIndex;
        this.dragging = true;
    }

    /**
     * Resets the manager, ending any current drag operation and clearing all state.
     */
    public void stopDrag() {
        this.dragging = false;
        this.draggedItem = null;
        this.source = null;
        this.sourceIndex = -1;
        this.pressPosition = null;
    }

    /**
     * Updates the screen position of the dragged item.
     *
     * @param x The new x-coordinate of the mouse.
     * @param y The new y-coordinate of the mouse.
     */
    public void updatePosition(int x, int y) {
        if (dragging) {
            if (dragPosition == null) dragPosition = new Point();
            dragPosition.setLocation(x, y);
        }
    }

    public void render(Graphics g) {
        if (!dragging || draggedItem == null || dragPosition == null) return;

        BufferedImage itemModel = draggedItem.getModel();
        ItemData itemData = draggedItem.getData();
        if (itemModel == null || itemData == null) return;
        int renderX = dragPosition.x - ITEM_SIZE / 2;
        int renderY = dragPosition.y - ITEM_SIZE / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.setColor(itemData.rarity.getColor());
        g2d.fillRoundRect(renderX - 2, renderY - 2, ITEM_SIZE + 4, ITEM_SIZE + 4, 5, 5);
        g2d.drawImage(itemModel, renderX, renderY, ITEM_SIZE, ITEM_SIZE, null);

        if (itemData.stackable) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            String amount = String.valueOf(draggedItem.getAmount());
            int amountX = renderX + ITEM_SIZE - g.getFontMetrics().stringWidth(amount) - (int)(2 * SCALE);
            int amountY = renderY + ITEM_SIZE - (int)(2 * SCALE);
            g.drawString(amount, amountX, amountY);
        }
        g2d.dispose();
    }

    /**
     * "Arms" a potential drag operation by recording the initial mouse press position.
     * A drag is not considered active until the mouse moves past a defined threshold.
     *
     * @param pressPoint The point where the mouse was initially pressed.
     */
    public void armDrag(Point pressPoint) {
        this.pressPosition = pressPoint;
    }

    public boolean isArmed() {
        return this.pressPosition != null;
    }

    public Point getPressPosition() {
        return pressPosition;
    }

    public boolean isDragging() {
        return dragging;
    }

    public InventoryItem getDraggedItem() {
        return draggedItem;
    }

    public DragSourceType getSource() {
        return source;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }
}