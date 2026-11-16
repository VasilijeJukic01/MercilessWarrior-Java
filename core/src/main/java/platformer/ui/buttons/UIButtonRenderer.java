package platformer.ui.buttons;

import platformer.core.Framework;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.InputStream;

import static platformer.constants.Constants.*;

/**
 * Renders runic typography, crisp vector icons, and procedural dark-slate UI plaques/slots.
 */
public final class UIButtonRenderer {

    private static final Color NORMAL_COLOR = new Color(218, 226, 224);
    private static final Color HOVER_COLOR = new Color(235, 255, 255);
    private static final Color HOVER_GLOW = new Color(0, 240, 230, 120);
    private static final Color PRESSED_COLOR = new Color(145, 160, 160);
    private static final Color SHADOW_COLOR = new Color(10, 16, 18, 220);
    private static final Color MUTE_RED = new Color(255, 85, 85);

    private static final Color SUBPANEL_BG_TOP = new Color(12, 20, 24, 225);
    private static final Color SUBPANEL_BG_BOTTOM = new Color(18, 30, 36, 235);
    private static final Color SUBPANEL_BORDER = new Color(95, 120, 125, 200);
    private static final Color SUBPANEL_TRIM = new Color(45, 195, 190, 65);

    private static final Color SLOT_BG_TOP = new Color(9, 15, 18, 235);
    private static final Color SLOT_BG_BOTTOM = new Color(17, 27, 32, 235);
    private static final Color SLOT_BORDER = new Color(75, 98, 104, 185);

    private static Font baseFont;

    static {
        try (InputStream is = UIButtonRenderer.class.getResourceAsStream("/fonts/ui_font.ttf")) {
            if (is != null) {
                baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Exception ignored) {}
        if (baseFont == null) {
            baseFont = new Font("Georgia", Font.BOLD, 16);
        }
    }

    private UIButtonRenderer() {}

    public static Font getFont(float size) {
        return baseFont.deriveFont(Font.BOLD, size);
    }

    public static void drawButtonLabel(Graphics g, String label, int x, int y, int width, int height, boolean mouseOver, boolean mousePressed) {
        if (label == null || label.isEmpty()) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float fontSize = Math.max(8f, height * (label.length() > 7 ? 0.30f : 0.35f));
        g2d.setFont(getFont(fontSize));
        FontMetrics fm = g2d.getFontMetrics();

        int textX = x + (width - fm.stringWidth(label)) / 2;
        int capHeight = (int) (fm.getAscent() * 0.72f);
        int textY = y + (height + capHeight) / 2 + (int) (1 * SCALE);
        if (mousePressed) textY += (int) Math.max(1, SCALE);

        if (mouseOver && !mousePressed) {
            g2d.setColor(HOVER_GLOW);
            g2d.drawString(label, textX - 1, textY);
            g2d.drawString(label, textX + 1, textY);
            g2d.drawString(label, textX, textY - 1);
            g2d.drawString(label, textX, textY + 1);
        }

        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(label, textX + 1, textY + 2);

        g2d.setColor(mousePressed ? PRESSED_COLOR : (mouseOver ? HOVER_COLOR : NORMAL_COLOR));
        g2d.drawString(label, textX, textY);
        g2d.dispose();
    }

    public static void drawSquareIcon(Graphics g, ButtonType type, boolean muted, int x, int y, int width, int height, boolean mouseOver, boolean mousePressed) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = x + width / 2;
        int cy = y + height / 2 + (mousePressed ? (int) Math.max(1, SCALE * 0.5) : 0);
        int r = (int) (Math.min(width, height) * 0.22);

        Color mainColor = mousePressed ? PRESSED_COLOR : (mouseOver ? HOVER_COLOR : NORMAL_COLOR);

        if (mouseOver && !mousePressed) {
            g2d.setColor(HOVER_GLOW);
            renderIconGeometry(g2d, type, muted, cx, cy, r, 3.2f * SCALE);
        }

        g2d.setColor(SHADOW_COLOR);
        renderIconGeometry(g2d, type, muted, cx + 1, cy + 1, r, 1.8f * SCALE);
        g2d.setColor(mainColor);
        renderIconGeometry(g2d, type, muted, cx, cy, r, 1.8f * SCALE);

        if (muted && (type == ButtonType.SFX || type == ButtonType.MUSIC)) {
            g2d.setColor(MUTE_RED);
            g2d.setStroke(new BasicStroke(2.0f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(cx - r, cy - r, cx + r, cy + r);
        }

        g2d.dispose();
    }

    private static void renderIconGeometry(Graphics2D g2d, ButtonType type, boolean muted, int cx, int cy, int r, float strokeWidth) {
        g2d.setStroke(new BasicStroke(Math.max(1.5f, strokeWidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case NEXT, CONTINUE -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx - r * 0.45, cy - r * 0.85);
                p.lineTo(cx + r * 0.55, cy);
                p.lineTo(cx - r * 0.45, cy + r * 0.85);
                g2d.draw(p);
            }
            case PREV -> {
                Path2D p = new Path2D.Double();
                p.moveTo(cx + r * 0.45, cy - r * 0.85);
                p.lineTo(cx - r * 0.55, cy);
                p.lineTo(cx + r * 0.45, cy + r * 0.85);
                g2d.draw(p);
            }
            case EXIT -> {
                int d = (int) (r * 0.65);
                g2d.drawLine(cx - d, cy - d, cx + d, cy + d);
                g2d.drawLine(cx + d, cy - d, cx - d, cy + d);
            }
            case RETRY -> {
                int arcR = (int) (r * 0.78);
                g2d.draw(new Arc2D.Double(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 45, 270, Arc2D.OPEN));
                int tipX = (int) (cx + arcR * Math.cos(Math.toRadians(45)));
                int tipY = (int) (cy - arcR * Math.sin(Math.toRadians(45)));
                g2d.drawLine(tipX, tipY, tipX - (int) (r * 0.45), tipY);
                g2d.drawLine(tipX, tipY, tipX, tipY + (int) (r * 0.45));
            }
            case L_BOARD -> {
                int w = Math.max(2, (int) (r * 0.48));
                int baseY = cy + (int) (r * 0.75);
                g2d.drawRect(cx - w / 2, cy - (int) (r * 0.55), w, (int) (r * 1.3));
                g2d.drawRect(cx - w / 2 - w, cy - (int) (r * 0.1), w, (int) (r * 0.85));
                g2d.drawRect(cx + w / 2, cy + (int) (r * 0.15), w, (int) (r * 0.6));
                g2d.drawLine(cx - w * 2, baseY, cx + w * 2, baseY);
            }
            case CREDITS -> {
                g2d.setFont(getFont(r * 1.85f));
                FontMetrics fm = g2d.getFontMetrics();
                String q = "?";
                g2d.drawString(q, cx - fm.stringWidth(q) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
            }
            case SFX -> {
                Path2D speaker = new Path2D.Double();
                speaker.moveTo(cx - r * 0.75, cy - r * 0.35);
                speaker.lineTo(cx - r * 0.3, cy - r * 0.35);
                speaker.lineTo(cx + r * 0.2, cy - r * 0.8);
                speaker.lineTo(cx + r * 0.2, cy + r * 0.8);
                speaker.lineTo(cx - r * 0.3, cy + r * 0.35);
                speaker.lineTo(cx - r * 0.75, cy + r * 0.35);
                speaker.closePath();
                g2d.fill(speaker);
                if (!muted) {
                    int waveR = (int) (r * 0.65);
                    g2d.draw(new Arc2D.Double(cx - waveR * 0.3, cy - waveR, waveR * 1.4, waveR * 2, -45, 90, Arc2D.OPEN));
                }
            }
            case MUSIC -> {
                int noteR = Math.max(2, (int) (r * 0.36));
                int leftX = cx - (int) (r * 0.55);
                int rightX = cx + (int) (r * 0.35);
                int bottomY = cy + (int) (r * 0.45);
                int topY = cy - (int) (r * 0.65);
                g2d.fillOval(leftX - noteR, bottomY - noteR / 2, noteR * 2, (int) (noteR * 1.4));
                g2d.fillOval(rightX - noteR, bottomY - noteR, noteR * 2, (int) (noteR * 1.4));
                g2d.drawLine(leftX + noteR - 1, bottomY, leftX + noteR - 1, topY + (int) (r * 0.2));
                g2d.drawLine(rightX + noteR - 1, bottomY - noteR / 2, rightX + noteR - 1, topY);
                g2d.drawLine(leftX + noteR - 1, topY + (int) (r * 0.2), rightX + noteR - 1, topY);
            }
            default -> {}
        }
    }

    public static void drawRunicPanel(Graphics g, Shape boundsShape) {
        Rectangle r = boundsShape.getBounds();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = (int) (8 * SCALE);
        GradientPaint bgPaint = new GradientPaint(r.x, r.y, new Color(14, 22, 26, 245), r.x, r.y + r.height, new Color(20, 32, 38, 248));
        g2d.setPaint(bgPaint);
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, arc, arc);

        // Outer weathered stone border
        g2d.setColor(new Color(125, 145, 148, 230));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, arc, arc);

        // Inner recessed cyan-slate trim
        int inset = (int) (4 * SCALE);
        g2d.setColor(new Color(45, 195, 190, 90));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(r.x + inset, r.y + inset, r.width - inset * 2, r.height - inset * 2, arc / 2, arc / 2);

        // Corner stone accents
        int cornerLen = (int) (12 * SCALE);
        g2d.setColor(new Color(175, 195, 198, 240));
        g2d.setStroke(new BasicStroke(2.5f));
        // Top-left
        g2d.drawLine(r.x + inset, r.y + inset, r.x + inset + cornerLen, r.y + inset);
        g2d.drawLine(r.x + inset, r.y + inset, r.x + inset, r.y + inset + cornerLen);
        // Top-right
        g2d.drawLine(r.x + r.width - inset, r.y + inset, r.x + r.width - inset - cornerLen, r.y + inset);
        g2d.drawLine(r.x + r.width - inset, r.y + inset, r.x + r.width - inset, r.y + inset + cornerLen);
        // Bottom-left
        g2d.drawLine(r.x + inset, r.y + r.height - inset, r.x + inset + cornerLen, r.y + r.height - inset);
        g2d.drawLine(r.x + inset, r.y + r.height - inset, r.x + inset, r.y + r.height - inset - cornerLen);
        // Bottom-right
        g2d.drawLine(r.x + r.width - inset, r.y + r.height - inset, r.x + r.width - inset - cornerLen, r.y + r.height - inset);
        g2d.drawLine(r.x + r.width - inset, r.y + r.height - inset, r.x + r.width - inset, r.y + r.height - inset - cornerLen);

        g2d.dispose();
    }

    /**
     * Renders a QuickUsePanel-styled runic sub-plaque for inner sections (Backpack, Equipment, Inspector, Stats).
     */
    public static void drawRunicSubPanel(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = scale(6);
        GradientPaint bg = new GradientPaint(x, y, SUBPANEL_BG_TOP, x, y + h, SUBPANEL_BG_BOTTOM);
        g2d.setPaint(bg);
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        g2d.setColor(SUBPANEL_BORDER);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, w, h, arc, arc);

        int inset = scale(2);
        g2d.setColor(SUBPANEL_TRIM);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x + inset, y + inset, w - inset * 2, h - inset * 2, arc / 2, arc / 2);

        int cLen = scale(6);
        g2d.setColor(new Color(175, 198, 202, 200));
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.drawLine(x, y, x + cLen, y);
        g2d.drawLine(x, y, x, y + cLen);
        g2d.drawLine(x + w, y, x + w - cLen, y);
        g2d.drawLine(x + w, y, x + w, y + cLen);
        g2d.drawLine(x, y + h, x + cLen, y + h);
        g2d.drawLine(x, y + h, x, y + h - cLen);
        g2d.drawLine(x + w, y + h, x + w - cLen, y + h);
        g2d.drawLine(x + w, y + h, x + w, y + h - cLen);
        g2d.dispose();
    }

    /**
     * Renders a recessed runic slot matching QuickUsePanel's slot design.
     */
    public static void drawRunicSlot(Graphics g, int x, int y, int size, Color rarityFill, Color rarityBorder, boolean hovered) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = scale(5);
        GradientPaint slotBg = new GradientPaint(x, y, SLOT_BG_TOP, x, y + size, SLOT_BG_BOTTOM);
        g2d.setPaint(slotBg);
        g2d.fillRoundRect(x, y, size, size, arc, arc);

        if (rarityFill != null) {
            int inset = scale(2);
            g2d.setColor(new Color(rarityFill.getRed(), rarityFill.getGreen(), rarityFill.getBlue(), 85));
            g2d.fillRoundRect(x + inset, y + inset, size - inset * 2, size - inset * 2, scale(4), scale(4));
        }

        if (hovered) {
            g2d.setColor(new Color(55, 235, 225, 40));
            g2d.fillRoundRect(x + 1, y + 1, size - 2, size - 2, arc, arc);
        }

        g2d.setColor(hovered ? new Color(110, 235, 230, 220) : SLOT_BORDER);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x, y, size, size, arc, arc);

        if (rarityBorder != null) {
            g2d.setColor(new Color(rarityBorder.getRed(), rarityBorder.getGreen(), rarityBorder.getBlue(), 165));
            g2d.setStroke(new BasicStroke(1.1f));
            g2d.drawRoundRect(x + 1, y + 1, size - 2, size - 2, scale(4), scale(4));
        }

        g2d.dispose();
    }

    /**
     * Renders an animated, glowing cyan runic selection frame around the active slot.
     */
    public static void drawSelectedSlotFrame(Graphics g, int x, int y, int size) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 180.0));
        int arc = scale(5);

        // Outer glow
        g2d.setColor(new Color(45, 240, 230, (int) (95 * pulse)));
        g2d.setStroke(new BasicStroke(scale(2.8)));
        g2d.drawRoundRect(x - 1, y - 1, size + 2, size + 2, arc + 2, arc + 2);

        // Cyan border
        g2d.setColor(new Color(175, 255, 250, 245));
        g2d.setStroke(new BasicStroke(scale(1.3)));
        g2d.drawRoundRect(x, y, size, size, arc, arc);

        // Corner ticks
        int c = scale(5);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(scale(1.6)));
        g2d.drawLine(x - 1, y - 1, x + c, y - 1);
        g2d.drawLine(x - 1, y - 1, x - 1, y + c);
        g2d.drawLine(x + size + 1, y - 1, x + size + 1 - c, y - 1);
        g2d.drawLine(x + size + 1, y - 1, x + size + 1, y + c);
        g2d.drawLine(x - 1, y + size + 1, x + c, y + size + 1);
        g2d.drawLine(x - 1, y + size + 1, x - 1, y + size + 1 - c);
        g2d.drawLine(x + size + 1, y + size + 1, x + size + 1 - c, y + size + 1);
        g2d.drawLine(x + size + 1, y + size + 1, x + size + 1, y + size + 1 - c);

        g2d.dispose();
    }

    /**
     * Draws a crisp shadowed stack count in the bottom-right corner of a slot.
     */
    public static void drawStackCount(Graphics g, int amount, int slotX, int slotY, int slotSize) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String text = String.valueOf(amount);
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = slotX + slotSize - fm.stringWidth(text) - scale(3);
        int ty = slotY + slotSize - scale(3);

        g2d.setColor(new Color(8, 12, 14, 235));
        g2d.drawString(text, tx + 1, ty + 1);
        g2d.setColor(new Color(235, 245, 245));
        g2d.drawString(text, tx, ty);
        g2d.dispose();
    }

    /**
     * Draws a small pill badge.
     */
    public static void drawBadge(Graphics g, String text, int x, int y, Color bgColor, Color textColor) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        FontMetrics fm = g2d.getFontMetrics();
        int padX = scale(5);
        int padY = scale(2);
        int w = fm.stringWidth(text) + padX * 2;
        int h = fm.getHeight() + padY;

        g2d.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 65));
        g2d.fillRoundRect(x, y - fm.getAscent() - padY / 2, w, h, scale(4), scale(4));
        g2d.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 160));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x, y - fm.getAscent() - padY / 2, w, h, scale(4), scale(4));

        g2d.setColor(textColor);
        g2d.drawString(text, x + padX, y);
        g2d.dispose();
    }

    public static void drawRunicHeader(Graphics g, String title, int centerX, int topY) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float fontSize = 22f * SCALE;
        g2d.setFont(getFont(fontSize));
        FontMetrics fm = g2d.getFontMetrics();

        int textW = fm.stringWidth(title);
        int textX = centerX - textW / 2;
        int textY = topY + (int) (24 * SCALE);

        g2d.setColor(new Color(0, 230, 220, 45));
        g2d.drawString(title, textX - 1, textY);
        g2d.drawString(title, textX + 1, textY);
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(title, textX + 2, textY + 2);

        GradientPaint textGrad = new GradientPaint(textX, textY - fm.getAscent(), new Color(240, 248, 246), textX, textY, new Color(165, 190, 192));
        g2d.setPaint(textGrad);
        g2d.drawString(title, textX, textY);

        // Decorative runic divider line + center diamond under the title
        int lineY = textY + (int) (9 * SCALE);
        int halfLineW = Math.max((int) (65 * SCALE), textW / 2 + (int) (15 * SCALE));
        int diamondR = Math.max(3, (int) (3.5 * SCALE));

        GradientPaint leftLine = new GradientPaint(centerX - halfLineW, lineY, new Color(45, 220, 215, 0), centerX - diamondR * 2, lineY, new Color(45, 220, 215, 190));
        g2d.setPaint(leftLine);
        g2d.setStroke(new BasicStroke(1.5f * SCALE));
        g2d.drawLine(centerX - halfLineW, lineY, centerX - diamondR * 2, lineY);

        GradientPaint rightLine = new GradientPaint(centerX + diamondR * 2, lineY, new Color(45, 220, 215, 190), centerX + halfLineW, lineY, new Color(45, 220, 215, 0));
        g2d.setPaint(rightLine);
        g2d.drawLine(centerX + diamondR * 2, lineY, centerX + halfLineW, lineY);

        Polygon diamond = new Polygon(new int[]{centerX, centerX + diamondR, centerX, centerX - diamondR}, new int[]{lineY - diamondR, lineY, lineY + diamondR, lineY}, 4);
        g2d.setColor(new Color(70, 245, 235, 220));
        g2d.fillPolygon(diamond);

        g2d.dispose();
    }

    /**
     * Renders a floating, bobbing runic interaction badge with a downward pointer arrow.
     */
    public static void drawInteractionPrompt(Graphics g, String actionLabel, int centerX, int baseY) {
        if (actionLabel == null || actionLabel.isEmpty()) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String keyText = "F";
        try {
            keyText = Framework.getInstance().getKeyboardController().getKeyName("Interact").toUpperCase();
        } catch (Exception ignored) {}

        double time = System.currentTimeMillis() * 0.0055;
        int bobOffset = (int) Math.round(Math.sin(time) * (2.5 * SCALE));
        float pulse = (float) (0.75 + 0.25 * Math.sin(time * 1.3));

        Font keyFont = getFont(8.5f * SCALE);
        Font labelFont = getFont(9.5f * SCALE);
        FontMetrics fmKey = g2d.getFontMetrics(keyFont);
        FontMetrics fmLabel = g2d.getFontMetrics(labelFont);

        int keyBoxSize = Math.max((int) (13 * SCALE), fmKey.stringWidth(keyText) + (int) (6 * SCALE));
        int gap = (int) (5 * SCALE);
        int padX = (int) (7 * SCALE);
        int padY = (int) (4 * SCALE);

        int labelW = fmLabel.stringWidth(actionLabel);
        int totalW = padX * 2 + keyBoxSize + gap + labelW;
        int totalH = keyBoxSize + padY * 2;

        int boxX = centerX - totalW / 2;
        int boxY = baseY - totalH + bobOffset;
        int arc = (int) (6 * SCALE);

        g2d.setColor(new Color(12, 20, 24, 225));
        g2d.fillRoundRect(boxX, boxY, totalW, totalH, arc, arc);
        g2d.setColor(new Color(55, 235, 225, (int) (185 * pulse)));
        g2d.setStroke(new BasicStroke(1.2f * SCALE));
        g2d.drawRoundRect(boxX, boxY, totalW, totalH, arc, arc);

        int triW = (int) (4 * SCALE);
        int triH = (int) (3.5 * SCALE);
        int triTopY = boxY + totalH;
        Polygon pointer = new Polygon(new int[]{centerX - triW, centerX + triW, centerX}, new int[]{triTopY, triTopY, triTopY + triH}, 3);
        g2d.setColor(new Color(55, 235, 225, (int) (185 * pulse)));
        g2d.fillPolygon(pointer);

        int keyX = boxX + padX;
        int keyY = boxY + padY;
        g2d.setColor(new Color(28, 46, 52, 240));
        g2d.fillRoundRect(keyX, keyY, keyBoxSize, keyBoxSize, (int) (4 * SCALE), (int) (4 * SCALE));
        g2d.setColor(new Color(140, 185, 190, 200));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(keyX, keyY, keyBoxSize, keyBoxSize, (int) (4 * SCALE), (int) (4 * SCALE));

        g2d.setFont(keyFont);
        g2d.setColor(new Color(80, 250, 240));
        int keyTextX = keyX + (keyBoxSize - fmKey.stringWidth(keyText)) / 2;
        int keyTextY = keyY + (keyBoxSize + (int) (fmKey.getAscent() * 0.72f)) / 2;
        g2d.drawString(keyText, keyTextX, keyTextY);

        g2d.setFont(labelFont);
        int labelX = keyX + keyBoxSize + gap;
        int labelY = boxY + (totalH + (int) (fmLabel.getAscent() * 0.72f)) / 2;
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(actionLabel, labelX + 1, labelY + 1);
        g2d.setColor(NORMAL_COLOR);
        g2d.drawString(actionLabel, labelX, labelY);

        g2d.dispose();
    }
}