package platformer.ui.dialogue;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.Npc;
import platformer.ui.overlays.Overlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.FONT_DIALOGUE;
import static platformer.constants.Constants.SCALE;
import static platformer.constants.UI.*;

public class DialogueOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private RoundRectangle2D dialogueBox;
    private List<String> dialogues;
    private int dialogueIndex = 0;
    private int currentLetterIndex = 0;
    private final int animSpeed = 8;
    private int animTick;

    private String visibleText = "";
    private boolean changeText = true;

    private GameObject intersectionObject;
    private Map<Class<? extends GameObject>, Boolean> firstTime;

    public DialogueOverlay() {
        init();
    }

    private void init() {
        this.dialogueBox = new RoundRectangle2D.Double(DIALOGUE_BOX_X, DIALOGUE_BOX_Y, DIALOGUE_BOX_WID, DIALOGUE_BOX_HEI, 25, 25);
        this.dialogues = new ArrayList<>();
        this.firstTime = new HashMap<>();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

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

    // Core
    @Override
    public void update() {
        if (dialogues == null || dialogues.isEmpty()) return;
        animTick++;
        if (animTick >= animSpeed) {
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
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fill(dialogueBox);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(dialogueBox);
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
            renderContinueText(g);
        }
    }

    private void renderArrow(Graphics g) {
        int xPos = DIALOGUE_BOX_X + DIALOGUE_BOX_WID - (int)(20 * SCALE);
        int yPos = DIALOGUE_Y + DIALOGUE_BOX_HEI - (int)(30 * SCALE);
        g.drawString("▼", xPos, yPos);
    }

    private void renderContinueText(Graphics g) {
        int xPos = DIALOGUE_BOX_X + (int)(20 * SCALE);
        int yPos = DIALOGUE_Y + DIALOGUE_BOX_HEI - (int)(30 * SCALE);
        g.drawString("[Press X to continue]", xPos, yPos);
    }

    // Reset
    @Override
    public void reset() {
        dialogueIndex = findRepetitionIndex();
        resetAnimation();
    }

    private void resetAnimation() {
        currentLetterIndex = animTick = 0;
        changeText = true;
    }

    // Helper
    private int findRepetitionIndex() {
        if (dialogues == null || dialogues.isEmpty()) return 0;
        if (firstTime.get(intersectionObject.getClass()) != null && !firstTime.get(intersectionObject.getClass())) return 0;
        if (firstTime.get(intersectionObject.getClass()) == null) return 0;
        for (int i = 0; i < dialogues.size(); i++) {
            if (dialogues.get(i).startsWith("~")) return i;
        }
        return 0;
    }

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

    public boolean next() {
        if (dialogues == null || dialogues.isEmpty()) return false;
        if (currentLetterIndex < dialogues.get(dialogueIndex).length()) {
            currentLetterIndex = dialogues.get(dialogueIndex).length();
            return true;
        }
        if (dialogueIndex >= dialogues.size()-1) return false;
        dialogueIndex++;
        resetAnimation();
        return true;
    }

    public void setDialogues(List<String> dialogues, GameObject intersectionObject) {
        this.dialogues = dialogues;
        this.intersectionObject = intersectionObject;
        reset();
        this.firstTime.put(intersectionObject.getClass(), true);
    }
}
