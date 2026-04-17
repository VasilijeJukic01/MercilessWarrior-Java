package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.dialogue.Dialogue;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.dialogue.question.Question;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.AnimConstants.DIALOGUE_ANIM_SPEED;
import static platformer.constants.AnimConstants.DIALOGUE_ARR_ANIM_SPEED;
import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * This class handles the display of dialogues in the game, including the animation of text appearing and the interaction with the user.
 * <p>
 * The class implements the Overlay interface, which means it has methods for handling mouse and keyboard events, as well as updating and
 * rendering the overlay.
 */
public class DialogueOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private RoundRectangle2D dialogueBox;
    private List<String> dialogues;
    private Question question;
    private int dialogueIndex = 0;
    private int currentLetterIndex = 0;
    private int animTick, arrowAnimTick;

    private String visibleText = "";
    private boolean changeText = true;
    private boolean onLastQuestion;

    private RoundRectangle2D portraitBox;
    private Area combinedShape;
    private BufferedImage currPortrait;

    private GameObject intersectionObject;

    public DialogueOverlay() {
        init();
    }

    private void init() {
        this.dialogueBox = new RoundRectangle2D.Double(DIALOGUE_BOX_X, DIALOGUE_BOX_Y, DIALOGUE_BOX_WID, DIALOGUE_BOX_HEI, 25, 25);
        this.dialogues = new ArrayList<>();
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

    // Core
    /**
     * Updates the dialogue overlay state and handles the animation of dialogue text.
     */
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
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fill(combinedShape);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(combinedShape);
        renderPortrait(g2d);
        renderDialogue(g);
    }

    // Render
    private void renderDialogue(Graphics g) {
        if (dialogues == null || dialogues.isEmpty()) return;
        g.setFont(new Font("Arial", Font.PLAIN, FONT_DIALOGUE));

        String dialogue = dialogues.get(dialogueIndex);

        if (changeText) visibleText = createCurrentText(g, dialogue);

        if (intersectionObject instanceof Npc) {
            Npc npc = (Npc) intersectionObject;
            g.setColor(npc.getNpcType().getNameColor());
            g.drawString(npc.getNpcType().getName(), DIALOGUE_X - (int)(5 * SCALE), DIALOGUE_Y - (int)(25 * SCALE));
        }
        g.setColor(Color.WHITE);

        int x = DIALOGUE_X;
        int y = DIALOGUE_Y;
        int lineHeight = g.getFontMetrics().getHeight();

        for (int i = 0; i < currentLetterIndex; i++) {
            if (visibleText.charAt(i) == '~') continue;
            if (visibleText.charAt(i) == '\n') {
                y += lineHeight;
                x = DIALOGUE_X;
            }
            g.drawString(""+visibleText.charAt(i), x, y);
            x += g.getFontMetrics().charWidth(visibleText.charAt(i));
        }
        if (currentLetterIndex == dialogue.length()) {
            renderArrow(g);
            if (question != null && dialogueIndex == dialogues.size()-1) renderYesOrNoText(g);
            else renderContinueText(g);
        }
    }

    private void renderArrow(Graphics g) {
        int xPos = DIALOGUE_BOX_X + DIALOGUE_BOX_WID - (int)(20 * SCALE);
        int yPos = DIALOGUE_Y + DIALOGUE_BOX_HEI - (int)(30 * SCALE);
        int bounce = (int) (Math.sin(arrowAnimTick / (double) DIALOGUE_ARR_ANIM_SPEED) * 5);
        g.drawString("▼", xPos, yPos + bounce);
    }

    private void renderContinueText(Graphics g) {
        int xPos = DIALOGUE_BOX_X + (int)(20 * SCALE);
        int yPos = DIALOGUE_Y + DIALOGUE_BOX_HEI - (int)(30 * SCALE);
        g.drawString("[Press X to continue]", xPos, yPos);
    }

    private void renderYesOrNoText(Graphics g) {
        int xPos = DIALOGUE_BOX_X + (int)(20 * SCALE);
        int yPos = DIALOGUE_Y + DIALOGUE_BOX_HEI - (int)(30 * SCALE);
        g.drawString("[Press Y/N to answer]", xPos, yPos);
    }

    private void renderPortrait(Graphics2D g2d) {
        if (currPortrait == null || portraitBox == null) return;
        int padding = (int) (1 * SCALE);
        int pSize = (int) (portraitBox.getWidth() - (padding * 2));
        int imgX = (int) portraitBox.getX() + padding;
        int imgY = (int) portraitBox.getY() + padding;

        Shape oldClip = g2d.getClip();
        RoundRectangle2D imgClip = new RoundRectangle2D.Double(imgX, imgY, pSize, pSize, 10, 10);

        g2d.setClip(imgClip);
        g2d.drawImage(currPortrait, imgX, imgY, pSize, pSize, null);
        g2d.setClip(oldClip);
    }

    // Reset
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

    // Helper
    private String createCurrentText(Graphics g, String dialogue) {
        StringBuilder text = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        String[] words = dialogue.split(" ");

        for (String word : words) {
            String potentialLine = currentLine + (currentLine.length() > 0 ? " " : "") + word;
            if (g.getFontMetrics().stringWidth(potentialLine) <= DIALOGUE_LINE) {
                if (currentLine.length() > 0) {
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

    /**
     * Advances to the next dialogue.
     *
     * @return true if there is a next dialogue, false otherwise
     */
    public boolean next() {
        if (dialogues == null || dialogues.isEmpty()) return false;
        if (skipLetterAnim()) return true;
        if (question != null && dialogueIndex == dialogues.size()-2) {
            onLastQuestion = true;
        }
        // Dialogue finished
        if (dialogueIndex >= dialogues.size()-1) {
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
        combinedShape = new Area(dialogueBox);
        portraitBox = null;
        currPortrait = null;

        if (intersectionObject instanceof Npc) {
            Npc npc = (Npc) intersectionObject;
            currPortrait = SpriteManager.getInstance().getNpcPortrait(npc.getNpcType());
            int portraitSize = (int) (40 * SCALE);
            int padding = (int) (1 * SCALE);
            int frameSize = portraitSize + padding;
            int xPos = DIALOGUE_BOX_X + DIALOGUE_BOX_WID - frameSize;
            int yPos = (int) (DIALOGUE_BOX_Y - frameSize + (7 * SCALE));

            portraitBox = new RoundRectangle2D.Double(xPos, yPos, frameSize, frameSize, 10, 10);
            combinedShape.add(new Area(portraitBox));
        }
    }

    /**
     * Sets the dialogues and intersection object.
     *
     * @param dialogue the dialogue
     * @param intersectionObject the intersection object
     */
    public void setDialogues(Dialogue dialogue, GameObject intersectionObject) {
        this.dialogues = dialogue.getLines();
        this.question = dialogue.getQuestion();
        this.intersectionObject = intersectionObject;
        configurePortrait();
        reset();
    }

    // Getters
    public Question getQuestion() {
        return question;
    }

    public boolean isOnLastQuestion() {
        return onLastQuestion;
    }
}
