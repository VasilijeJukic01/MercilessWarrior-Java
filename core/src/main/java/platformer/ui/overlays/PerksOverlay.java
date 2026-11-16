package platformer.ui.overlays;

import platformer.core.GameContext;
import platformer.model.perks.Perk;
import platformer.model.perks.PerksManager;
import platformer.state.types.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.controller.PerksViewController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Graph-based Skill Tree renderer for the Mastery Perks overlay.
 * Node positions are computed from each Perk's normalized (treeX, treeY) coordinates.
 */
public class PerksOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    public static final int NODE_SIZE = scale(34);
    public static final int ULTIMATE_NODE_SIZE = scale(40);

    private final PerksViewController controller;
    private final GameState gameState;
    private final PerksManager perksManager;

    private Rectangle2D.Double overlay, treePanel, inspectorPanel;
    private final MediumButton[] buttons;

    public PerksOverlay(GameContext context) {
        this.perksManager = context.getPerksManager();
        this.gameState = context.getGameState();
        this.buttons = new MediumButton[2];
        initPanels();
        loadButtons();
        this.controller = new PerksViewController(context, this);
    }

    private void initPanels() {
        this.overlay = new Rectangle2D.Double(PERKS_OVERLAY_X, PERKS_OVERLAY_Y, PERKS_OVERLAY_WID, PERKS_OVERLAY_HEI);
        this.treePanel = new Rectangle2D.Double(scale(82), scale(108), scale(446), scale(258));
        this.inspectorPanel = new Rectangle2D.Double(scale(538), scale(108), scale(210), scale(258));
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(BUY_PERK_BTN_X, BUY_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.BUY);
        buttons[1] = new MediumButton(LEAVE_PERK_BTN_X, LEAVE_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.CLOSE);
    }

    /**
     * Computes the exact screen center (x, y) of a Perk node inside treePanel.
     */
    public Point getNodeCenter(Perk p) {
        int padX = scale(26);
        int padTop = scale(28);
        int padBottom = scale(22);
        double usableW = treePanel.width - padX * 2;
        double usableH = treePanel.height - padTop - padBottom;

        int cx = (int) Math.round(treePanel.x + padX + p.getTreeX() * usableW);
        int cy = (int) Math.round(treePanel.y + padTop + p.getTreeY() * usableH);
        return new Point(cx, cy);
    }

    public int getNodeSize(Perk p) {
        return (p.getUnlocks() == null || p.getUnlocks().isEmpty()) ? ULTIMATE_NODE_SIZE : NODE_SIZE;
    }

    public Rectangle getNodeBounds(Perk p) {
        Point c = getNodeCenter(p);
        int size = getNodeSize(p);
        return new Rectangle(c.x - size / 2, c.y - size / 2, size, size);
    }

    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderSubPanels(g2d);
        renderBranchColumnsAndHeaders(g2d);
        renderConstellationEdges(g2d);
        renderPerkNodes(g2d);
        renderPerkInspector(g2d);
        renderButtons(g);
    }

    private void renderOverlay(Graphics2D g2d) {
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "MASTERY PERKS", PERKS_OVERLAY_X + PERKS_OVERLAY_WID / 2, PERKS_TEXT_Y - scale(14));
    }

    private void renderSubPanels(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) treePanel.x, (int) treePanel.y, (int) treePanel.width, (int) treePanel.height);
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) inspectorPanel.x, (int) inspectorPanel.y, (int) inspectorPanel.width, (int) inspectorPanel.height);

        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("SKILL TREE CONSTELLATION", (int) treePanel.x + scale(6), (int) treePanel.y - scale(5));
        g2d.drawString("RUNE INSPECTOR", (int) inspectorPanel.x + scale(6), (int) inspectorPanel.y - scale(5));
    }

    private void renderBranchColumnsAndHeaders(Graphics2D g2d) {
        Graphics2D gBg = (Graphics2D) g2d.create();
        gBg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gBg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String[] branches = {"BLADEMASTER", "BULWARK", "PYROMANCER"};
        double[] centerTreeX = {0.17, 0.50, 0.83};

        int padX = scale(26);
        double usableW = treePanel.width - padX * 2;

        gBg.setColor(new Color(55, 195, 190, 28));
        int div1X = (int) Math.round(treePanel.x + padX + 0.335 * usableW);
        int div2X = (int) Math.round(treePanel.x + padX + 0.665 * usableW);
        gBg.drawLine(div1X, (int) treePanel.y + scale(10), div1X, (int) (treePanel.y + treePanel.height - scale(10)));
        gBg.drawLine(div2X, (int) treePanel.y + scale(10), div2X, (int) (treePanel.y + treePanel.height - scale(10)));

        gBg.setFont(UIButtonRenderer.getFont(9.5f * SCALE));
        FontMetrics fm = gBg.getFontMetrics();
        int headerY = (int) treePanel.y + scale(17);

        for (int i = 0; i < branches.length; i++) {
            int cx = (int) Math.round(treePanel.x + padX + centerTreeX[i] * usableW);
            Color col = getBranchColor(branches[i]);
            RadialGradientPaint aura = new RadialGradientPaint(
                    new Point(cx, (int) (treePanel.y + treePanel.height * 0.52)),
                    scale(90),
                    new float[]{0f, 1f},
                    new Color[]{new Color(col.getRed(), col.getGreen(), col.getBlue(), 22), new Color(col.getRed(), col.getGreen(), col.getBlue(), 0)}
            );
            gBg.setPaint(aura);
            gBg.fillOval(cx - scale(90), (int) (treePanel.y + treePanel.height * 0.52) - scale(90), scale(180), scale(180));

            int tw = fm.stringWidth(branches[i]);
            gBg.setColor(new Color(8, 12, 14, 220));
            gBg.drawString(branches[i], cx - tw / 2 + 1, headerY + 1);
            gBg.setColor(col);
            gBg.drawString(branches[i], cx - tw / 2, headerY);
        }
        gBg.dispose();
    }

    /**
     * Draws directed constellation lines between each perk and the perks in its unlocks[] list.
     */
    private void renderConstellationEdges(Graphics2D g2d) {
        Graphics2D gLine = (Graphics2D) g2d.create();
        gLine.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Perk> perks = perksManager.getPerks();
        for (Perk from : perks) {
            if (from.getUnlocks() == null) continue;
            Point p1 = getNodeCenter(from);
            for (int targetSlot : from.getUnlocks()) {
                Perk to = getPerkAtSlot(targetSlot);
                if (to == null) continue;
                if (to.getSlot() < from.getSlot() && to.getUnlocks() != null && to.getUnlocks().contains(from.getSlot())) {
                    continue;
                }
                Point p2 = getNodeCenter(to);
                drawEdge(gLine, p1.x, p1.y, p2.x, p2.y, from, to);
            }
        }
        gLine.dispose();
    }

    private void drawEdge(Graphics2D g2d, int x1, int y1, int x2, int y2, Perk a, Perk b) {
        boolean bothUpgraded = a.isUpgraded() && b.isUpgraded();
        boolean activePath = (a.isUpgraded() && !b.isLocked()) || (b.isUpgraded() && !a.isLocked());
        Color branchCol = getBranchColor(a.getBranch());

        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        if (bothUpgraded) {
            g2d.setColor(new Color(255, 200, 55, 85));
            g2d.setStroke(new BasicStroke(scale(4.5), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
            g2d.setColor(new Color(255, 230, 120, 240));
            g2d.setStroke(new BasicStroke(scale(2.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);

            drawDiamondNode(g2d, midX, midY, scale(3.5), new Color(255, 235, 140));
        }
        else if (activePath) {
            g2d.setColor(new Color(branchCol.getRed(), branchCol.getGreen(), branchCol.getBlue(), 75));
            g2d.setStroke(new BasicStroke(scale(3.8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
            g2d.setColor(new Color(branchCol.getRed(), branchCol.getGreen(), branchCol.getBlue(), 215));
            g2d.setStroke(new BasicStroke(scale(1.6), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);

            drawDiamondNode(g2d, midX, midY, scale(2.8), branchCol);
        }
        else {
            g2d.setColor(new Color(52, 70, 76, 175));
            g2d.setStroke(new BasicStroke(scale(1.4), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);

            drawDiamondNode(g2d, midX, midY, scale(2.2), new Color(65, 85, 92, 180));
        }
    }

    private void drawDiamondNode(Graphics2D g2d, int cx, int cy, int r, Color color) {
        Polygon diamond = new Polygon(
                new int[]{cx, cx + r, cx, cx - r},
                new int[]{cy - r, cy, cy + r, cy},
                4
        );
        g2d.setColor(color);
        g2d.fillPolygon(diamond);
    }

    private void renderPerkNodes(Graphics2D g2d) {
        for (Perk p : perksManager.getPerks()) {
            Rectangle bounds = getNodeBounds(p);
            Color branchCol = getBranchColor(p.getBranch());

            Color fillCol = null;
            Color borderCol = null;
            if (p.isUpgraded()) {
                fillCol = new Color(235, 165, 30);
                borderCol = new Color(255, 220, 95);
            }
            else if (!p.isLocked()) {
                fillCol = new Color(branchCol.getRed() / 2, branchCol.getGreen() / 2, branchCol.getBlue() / 2);
                borderCol = branchCol;
            }

            boolean hovered = controller.getHoveredSlot() == p.getSlot();
            UIButtonRenderer.drawRunicSlot(g2d, bounds.x, bounds.y, bounds.width, fillCol, borderCol, hovered);

            int iconPad = bounds.width / 4;
            int iconSize = bounds.width - iconPad * 2;
            g2d.drawImage(p.getImage(), bounds.x + iconPad, bounds.y + iconPad, iconSize, iconSize, null);
            if (p.isLocked()) {
                g2d.setColor(new Color(6, 10, 12, 185));
                g2d.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, scale(4), scale(4));
            }

            if (!p.isUpgraded()) UIButtonRenderer.drawStackCount(g2d, p.getCost(), bounds.x, bounds.y, bounds.width);
            if (controller.getSlotNumber() == p.getSlot()) {
                UIButtonRenderer.drawSelectedSlotFrame(g2d, bounds.x, bounds.y, bounds.width);
            }
        }
    }

    private void renderPerkInspector(Graphics2D g2d) {
        int leftX = (int) inspectorPanel.x + scale(12);
        int rightX = (int) (inspectorPanel.x + inspectorPanel.width - scale(12));
        int y = (int) inspectorPanel.y + scale(18);

        int tokens = gameState.getPlayer().getUpgradeTokens();
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Upgrade Tokens:", leftX, y);

        String tokenStr = String.valueOf(tokens);
        g2d.setColor(new Color(95, 245, 235));
        int tw = g2d.getFontMetrics().stringWidth(tokenStr);
        g2d.drawString(tokenStr, rightX - tw, y);

        y += scale(8);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(leftX, y, rightX, y);
        y += scale(14);

        Perk selectedPerk = getPerkAtSlot(controller.getSlotNumber());
        if (selectedPerk == null) return;

        Color branchCol = getBranchColor(selectedPerk.getBranch());

        int previewS = scale(38);
        int previewX = (int) (inspectorPanel.x + (inspectorPanel.width - previewS) / 2);
        Color pFill = selectedPerk.isUpgraded() ? new Color(235, 165, 30) : (!selectedPerk.isLocked() ? branchCol : null);
        Color pBorder = selectedPerk.isUpgraded() ? new Color(255, 215, 85) : (!selectedPerk.isLocked() ? branchCol : null);
        UIButtonRenderer.drawRunicSlot(g2d, previewX, y, previewS, pFill, pBorder, false);
        g2d.drawImage(selectedPerk.getImage(), previewX + scale(7), y + scale(7), previewS - scale(14), previewS - scale(14), null);

        y += previewS + scale(15);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_DIALOGUE));
        g2d.setColor(Color.WHITE);
        FontMetrics fmName = g2d.getFontMetrics();
        g2d.drawString(selectedPerk.getName(), (int) (inspectorPanel.x + (inspectorPanel.width - fmName.stringWidth(selectedPerk.getName())) / 2), y);

        y += scale(14);

        // Branch + Status Badge
        String statusText;
        Color statusColor;
        if (selectedPerk.isUpgraded()) {
            statusText = selectedPerk.getBranch() + " • MASTERED";
            statusColor = new Color(255, 215, 80);
        }
        else if (selectedPerk.isLocked()) {
            statusText = selectedPerk.getBranch() + " • LOCKED";
            statusColor = new Color(255, 105, 105);
        }
        else {
            statusText = selectedPerk.getBranch() + " • AVAILABLE";
            statusColor = branchCol;
        }
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        int sw = g2d.getFontMetrics().stringWidth(statusText);
        UIButtonRenderer.drawBadge(g2d, statusText, (int) (inspectorPanel.x + (inspectorPanel.width - sw) / 2 - scale(5)), y, statusColor, statusColor);

        y += scale(18);

        if (!selectedPerk.isUpgraded()) {
            g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            g2d.setColor(INV_TEXT_LABEL);
            g2d.drawString("Token Cost:", leftX, y);
            String costStr = String.valueOf(selectedPerk.getCost());
            g2d.setColor(tokens >= selectedPerk.getCost() ? new Color(120, 255, 130) : new Color(255, 90, 90));
            g2d.drawString(costStr, rightX - g2d.getFontMetrics().stringWidth(costStr), y);
            y += scale(16);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        g2d.setColor(INV_TEXT_DESC);
        List<String> lines = wrapText(selectedPerk.getDescription(), rightX - leftX, g2d.getFontMetrics());
        for (String line : lines) {
            g2d.drawString(line, leftX, y);
            y += g2d.getFontMetrics().getHeight();
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        g2d.drawString("Right-Click or Enter to Upgrade", leftX, (int) (inspectorPanel.y + inspectorPanel.height - scale(6)));
    }

    private Color getBranchColor(String branch) {
        if (branch == null) return new Color(90, 235, 230);
        return switch (branch.toUpperCase()) {
            case "BLADEMASTER" -> new Color(255, 115, 95);
            case "BULWARK" -> new Color(95, 220, 255);
            case "PYROMANCER" -> new Color(255, 180, 70);
            default -> new Color(90, 235, 230);
        };
    }

    public Perk getPerkAtSlot(int slot) {
        for (Perk p : perksManager.getPerks()) {
            if (p.getSlot() == slot) return p;
        }
        return null;
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.render(g));
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (fm.stringWidth(currentLine + " " + word) <= maxWidth) {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent e) {
        controller.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        controller.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        controller.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controller.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controller.keyReleased(e);
    }

    @Override
    public void reset() {}

    public MediumButton[] getButtons() {
        return buttons;
    }
}