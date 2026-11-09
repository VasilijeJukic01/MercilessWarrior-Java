package platformer.ui.buttons;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.InputStream;

import static platformer.constants.Constants.SCALE;

/**
 * Renders runic typography and crisp vector icons onto the blank stone button plaques.
 */
public final class UIButtonRenderer {

    private static final Color NORMAL_COLOR = new Color(218, 226, 224);
    private static final Color HOVER_COLOR = new Color(235, 255, 255);
    private static final Color HOVER_GLOW = new Color(0, 240, 230, 120);
    private static final Color PRESSED_COLOR = new Color(145, 160, 160);
    private static final Color SHADOW_COLOR = new Color(10, 16, 18, 220);
    private static final Color MUTE_RED = new Color(255, 85, 85);

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
}