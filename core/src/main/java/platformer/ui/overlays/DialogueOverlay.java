package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.model.dialogue.Dialogue;
import platformer.model.dialogue.question.Question;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.AnimConstants.DIALOGUE_ANIM_SPEED;
import static platformer.constants.AnimConstants.DIALOGUE_ARR_ANIM_SPEED;
import static platformer.constants.Constants.*;

/**
 * Renders the in-game dialogue overlay.
 */
public class DialogueOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private static final int BOX_WID = scale(356);
    private static final int BOX_HEI = scale(84);
    private static final int BOX_X = (GAME_WIDTH - BOX_WID) / 2;
    private static final int BOX_Y = scale(346);

    private static final int PORTRAIT_SIZE = scale(52);
    private static final int PORTRAIT_FRAME_PAD = scale(3);

    private static final Color PANEL_TOP = new Color(11, 19, 23, 238);
    private static final Color PANEL_BOTTOM = new Color(19, 32, 38, 244);
    private static final Color OUTER_BORDER = new Color(118, 142, 148, 215);
    private static final Color DEFAULT_ACCENT = new Color(56, 229, 223);

    private List<String> dialogues = new ArrayList<>();
    private Question question;
    private int dialogueIndex = 0;
    private int currentLetterIndex = 0;
    private int animTick, arrowAnimTick;

    private String visibleText = "";
    private boolean changeText = true;
    private boolean onLastQuestion;

    private BufferedImage currPortrait;
    private GameObject intersectionObject;

    private final Font bodyFont = new Font("Arial", Font.PLAIN, FONT_DIALOGUE);
    private final Font promptFont = new Font("Arial", Font.BOLD, FONT_LIGHT);

    public DialogueOverlay() { }

    @Override
    public void update() {
        if (dialogues == null || dialogues.isEmpty()) return;
        animTick++;
        arrowAnimTick++;
        if (animTick >= DIALOGUE_ANIM_SPEED) {
            animTick = 0;
            if (currentLetterIndex < dialogues.get(dialogueIndex).length()) {
                currentLetterIndex++;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        if (dialogues == null || dialogues.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        NpcType speakerType = (intersectionObject instanceof Npc npc) ? npc.getNpcType() : null;
        Color accentColor = (speakerType != null && speakerType.getNameColor() != null) ? speakerType.getNameColor() : DEFAULT_ACCENT;

        renderMainPlaque(g2d, accentColor);
        if (speakerType != null) renderSpeakerNameplate(g2d, speakerType.getName(), accentColor);
        if (currPortrait != null) renderPortraitFrame(g2d, accentColor);
        renderDialogueTextAndPrompts(g2d, accentColor);

        g2d.dispose();
    }

    private void renderMainPlaque(Graphics2D g2d, Color accentColor) {
        int arc = scale(8);

        g2d.setColor(new Color(0, 0, 0, 110));
        g2d.fillRoundRect(BOX_X + scale(2), BOX_Y + scale(3), BOX_WID, BOX_HEI, arc, arc);

        GradientPaint bg = new GradientPaint(BOX_X, BOX_Y, PANEL_TOP, BOX_X, BOX_Y + BOX_HEI, PANEL_BOTTOM);
        g2d.setPaint(bg);
        g2d.fillRoundRect(BOX_X, BOX_Y, BOX_WID, BOX_HEI, arc, arc);

        int glowR = scale(110);
        RadialGradientPaint topGlow = new RadialGradientPaint(
                new Point(BOX_X + BOX_WID / 3, BOX_Y + scale(10)),
                glowR,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26), new Color(0, 0, 0, 0)}
        );
        g2d.setPaint(topGlow);
        g2d.fillRoundRect(BOX_X, BOX_Y, BOX_WID, BOX_HEI, arc, arc);

        g2d.setColor(OUTER_BORDER);
        g2d.setStroke(new BasicStroke(1.6f));
        g2d.drawRoundRect(BOX_X, BOX_Y, BOX_WID, BOX_HEI, arc, arc);

        int inset = scale(2.5);
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 85));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(BOX_X + inset, BOX_Y + inset, BOX_WID - inset * 2, BOX_HEI - inset * 2, arc - 2, arc - 2);

        int footerLineY = BOX_Y + BOX_HEI - scale(21);
        int lineRight = (currPortrait != null) ? (getPortraitFrameX() - scale(10)) : (BOX_X + BOX_WID - scale(14));
        GradientPaint divider = new GradientPaint(
                BOX_X + scale(14), footerLineY, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 95),
                lineRight, footerLineY, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 10)
        );
        g2d.setPaint(divider);
        g2d.drawLine(BOX_X + scale(14), footerLineY, lineRight, footerLineY);

        drawCornerBrackets(g2d, BOX_X, BOX_Y, BOX_WID, BOX_HEI, scale(7), new Color(185, 210, 215, 215));
    }

    private void renderSpeakerNameplate(Graphics2D g2d, String speakerName, Color accentColor) {
        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();

        int textW = fm.stringWidth(speakerName);
        int tabH = scale(18);
        int tabW = textW + scale(26);
        int tabX = BOX_X + scale(12);
        int tabY = BOX_Y - tabH + scale(4);
        int arc = scale(5);

        GradientPaint tabBg = new GradientPaint(tabX, tabY, new Color(14, 24, 30, 248), tabX, tabY + tabH, new Color(22, 36, 44, 248));
        g2d.setPaint(tabBg);
        g2d.fillRoundRect(tabX, tabY, tabW, tabH, arc, arc);

        g2d.setColor(OUTER_BORDER);
        g2d.setStroke(new BasicStroke(1.4f));
        g2d.drawRoundRect(tabX, tabY, tabW, tabH, arc, arc);

        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 145));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(tabX + 1, tabY + 1, tabW - 2, tabH - 2, arc, arc);

        int diamondCx = tabX + scale(9);
        int diamondCy = tabY + tabH / 2;
        int dr = scale(3);
        Polygon diamond = new Polygon(
                new int[]{diamondCx, diamondCx + dr, diamondCx, diamondCx - dr},
                new int[]{diamondCy - dr, diamondCy, diamondCy + dr, diamondCy},
                4
        );
        g2d.setColor(accentColor);
        g2d.fillPolygon(diamond);

        int nameX = tabX + scale(16);
        int nameY = tabY + (tabH + (int) (fm.getAscent() * 0.74f)) / 2;
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.drawString(speakerName, nameX + 1, nameY + 1);
        g2d.setColor(accentColor.brighter());
        g2d.drawString(speakerName, nameX, nameY);
    }

    private int getPortraitFrameX() {
        int frameSize = PORTRAIT_SIZE + PORTRAIT_FRAME_PAD * 2;
        return BOX_X + BOX_WID - frameSize - scale(10);
    }

    private void renderPortraitFrame(Graphics2D g2d, Color accentColor) {
        int frameSize = PORTRAIT_SIZE + PORTRAIT_FRAME_PAD * 2;
        int fx = getPortraitFrameX();
        int fy = BOX_Y - scale(10);
        int arc = scale(7);

        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRoundRect(fx + scale(2), fy + scale(2), frameSize, frameSize, arc, arc);

        g2d.setColor(new Color(12, 20, 24, 248));
        g2d.fillRoundRect(fx, fy, frameSize, frameSize, arc, arc);

        int imgX = fx + PORTRAIT_FRAME_PAD;
        int imgY = fy + PORTRAIT_FRAME_PAD;

        Shape oldClip = g2d.getClip();
        g2d.setClip(new RoundRectangle2D.Double(imgX, imgY, PORTRAIT_SIZE, PORTRAIT_SIZE, arc - 2, arc - 2));
        g2d.drawImage(currPortrait, imgX, imgY, PORTRAIT_SIZE, PORTRAIT_SIZE, null);
        g2d.setClip(oldClip);

        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(imgX, imgY, PORTRAIT_SIZE, PORTRAIT_SIZE, arc - 2, arc - 2);

        g2d.setColor(OUTER_BORDER);
        g2d.setStroke(new BasicStroke(1.6f));
        g2d.drawRoundRect(fx, fy, frameSize, frameSize, arc, arc);

        drawCornerBrackets(g2d, fx, fy, frameSize, frameSize, scale(5), accentColor);
    }

    private void renderDialogueTextAndPrompts(Graphics2D g2d, Color accentColor) {
        g2d.setFont(bodyFont);
        String dialogue = dialogues.get(dialogueIndex);

        int textStartX = BOX_X + scale(15);
        int textStartY = BOX_Y + scale(22);
        int maxLineWidth = (currPortrait != null) ? (getPortraitFrameX() - textStartX - scale(12)) : (BOX_WID - scale(30));

        if (changeText) {
            visibleText = createWrappedText(g2d, dialogue, maxLineWidth);
        }

        int safeEnd = Math.max(0, Math.min(currentLetterIndex, visibleText.length()));
        String currentSlice = visibleText.substring(0, safeEnd).replace("~", "");
        String[] lines = currentSlice.split("\n", -1);

        int lineHeight = g2d.getFontMetrics().getHeight() + scale(1);
        int y = textStartY;
        for (String line : lines) {
            g2d.setColor(new Color(0, 0, 0, 210));
            g2d.drawString(line, textStartX + 1, y + 1);
            g2d.setColor(new Color(235, 245, 246));
            g2d.drawString(line, textStartX, y);
            y += lineHeight;
        }

        if (currentLetterIndex >= dialogue.length()) {
            int promptY = BOX_Y + BOX_HEI - scale(7);
            if (question != null && dialogueIndex == dialogues.size() - 1) renderQuestionPrompts(g2d, textStartX, promptY);
            else renderContinuePrompt(g2d, textStartX, promptY, accentColor);
        }
    }

    private void renderContinuePrompt(Graphics2D g2d, int x, int baselineY, Color accentColor) {
        KeyboardController kc = Framework.getInstance().getKeyboardController();
        String keyAttack = (kc != null) ? kc.getKeyName("Attack").toUpperCase() : "X";

        int nextX = drawKeycapBadge(g2d, keyAttack, x, baselineY, DEFAULT_ACCENT);
        g2d.setFont(promptFont);
        g2d.setColor(new Color(165, 195, 200));
        g2d.drawString("Continue", nextX + scale(5), baselineY);

        int arrowRightX = (currPortrait != null) ? (getPortraitFrameX() - scale(16)) : (BOX_X + BOX_WID - scale(18));
        int bounce = (int) Math.round(Math.sin(arrowAnimTick / (double) DIALOGUE_ARR_ANIM_SPEED) * scale(2.0));
        drawBouncingChevron(g2d, arrowRightX, baselineY - scale(3) + bounce, accentColor);
    }

    private void renderQuestionPrompts(Graphics2D g2d, int x, int baselineY) {
        KeyboardController kc = Framework.getInstance().getKeyboardController();
        String keyAccept = (kc != null) ? kc.getKeyName("Accept").toUpperCase() : "Y";
        String keyDecline = (kc != null) ? kc.getKeyName("Decline").toUpperCase() : "N";

        Color acceptCol = new Color(90, 235, 130);
        Color declineCol = new Color(255, 105, 105);

        int afterAcceptBadge = drawKeycapBadge(g2d, keyAccept, x, baselineY, acceptCol);
        g2d.setFont(promptFont);
        g2d.setColor(acceptCol);
        g2d.drawString("Accept", afterAcceptBadge + scale(5), baselineY);

        int declineStartX = afterAcceptBadge + scale(5) + g2d.getFontMetrics().stringWidth("Accept") + scale(16);
        int afterDeclineBadge = drawKeycapBadge(g2d, keyDecline, declineStartX, baselineY, declineCol);
        g2d.setFont(promptFont);
        g2d.setColor(declineCol);
        g2d.drawString("Decline", afterDeclineBadge + scale(5), baselineY);
    }

    private int drawKeycapBadge(Graphics2D g2d, String keyText, int x, int baselineY, Color borderColor) {
        g2d.setFont(UIButtonRenderer.getFont(7.5f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();
        int badgeH = scale(11);
        int badgeW = Math.max(badgeH, fm.stringWidth(keyText) + scale(6));
        int badgeY = baselineY - badgeH + scale(2);
        int arc = scale(3);

        g2d.setColor(new Color(10, 18, 22, 240));
        g2d.fillRoundRect(x, badgeY, badgeW, badgeH, arc, arc);

        g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1.1f));
        g2d.drawRoundRect(x, badgeY, badgeW, badgeH, arc, arc);

        g2d.setColor(Color.WHITE);
        int tx = x + (badgeW - fm.stringWidth(keyText)) / 2;
        int ty = badgeY + (badgeH + (int) (fm.getAscent() * 0.72f)) / 2;
        g2d.drawString(keyText, tx, ty);
        return x + badgeW;
    }

    private void drawBouncingChevron(Graphics2D g2d, int cx, int cy, Color color) {
        double w = 4.5 * SCALE;
        double h = 3.5 * SCALE;
        Path2D.Double chevron = new Path2D.Double();
        chevron.moveTo(cx - w, cy - h * 0.5);
        chevron.lineTo(cx, cy + h * 0.6);
        chevron.lineTo(cx + w, cy - h * 0.5);
        chevron.lineTo(cx, cy - h * 0.1);
        chevron.closePath();

        g2d.setColor(color);
        g2d.fill(chevron);
    }

    private void drawCornerBrackets(Graphics2D g2d, int x, int y, int w, int h, int len, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.drawLine(x, y, x + len, y);
        g2d.drawLine(x, y, x, y + len);
        g2d.drawLine(x + w, y, x + w - len, y);
        g2d.drawLine(x + w, y, x + w, y + len);
        g2d.drawLine(x, y + h, x + len, y + h);
        g2d.drawLine(x, y + h, x, y + h - len);
        g2d.drawLine(x + w, y + h, x + w - len, y + h);
        g2d.drawLine(x + w, y + h, x + w, y + h - len);
    }

    @Override
    public void reset() {
        dialogueIndex = 0;
        resetAnimation();
        onLastQuestion = false;
    }

    private void resetAnimation() {
        currentLetterIndex = animTick = 0;
        changeText = true;
    }

    private String createWrappedText(Graphics g, String dialogue, int maxLineWidth) {
        StringBuilder text = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        String[] words = dialogue.split(" ");

        for (String word : words) {
            String potentialLine = currentLine + (!currentLine.isEmpty() ? " " : "") + word;
            if (g.getFontMetrics().stringWidth(potentialLine) <= maxLineWidth) {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
            else {
                text.append(currentLine).append("\n");
                currentLine = new StringBuilder(word);
            }
        }
        text.append(currentLine);
        changeText = false;
        return text.toString();
    }

    public boolean skipLetterAnim() {
        if (currentLetterIndex < dialogues.get(dialogueIndex).length()) {
            currentLetterIndex = dialogues.get(dialogueIndex).length();
            return true;
        }
        return false;
    }

    public boolean next() {
        if (dialogues == null || dialogues.isEmpty()) return false;
        if (skipLetterAnim()) return true;
        if (question != null && dialogueIndex == dialogues.size() - 2) {
            onLastQuestion = true;
        }
        if (dialogueIndex >= dialogues.size() - 1) {
            if (onLastQuestion) {
                onLastQuestion = false;
                return false;
            }
            return false;
        }
        dialogueIndex++;
        resetAnimation();
        return true;
    }

    private void configurePortrait() {
        currPortrait = null;
        if (intersectionObject instanceof Npc npc) {
            currPortrait = SpriteManager.getInstance().getNpcPortrait(npc.getNpcType());
        }
    }

    void checkLastQuestion() {
        if (this.question != null && this.dialogues != null && this.dialogues.size() == 1) {
            this.onLastQuestion = true;
        }
    }

    public void setDialogues(Dialogue dialogue, GameObject intersectionObject) {
        this.dialogues = dialogue.getLines();
        this.question = dialogue.getQuestion();
        this.intersectionObject = intersectionObject;
        configurePortrait();
        reset();
        checkLastQuestion();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public Question getQuestion() {
        return question;
    }

    public boolean isOnLastQuestion() {
        return onLastQuestion;
    }
}
