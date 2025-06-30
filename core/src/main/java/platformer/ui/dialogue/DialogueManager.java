package platformer.ui.dialogue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.DialogueBehavior;
import platformer.model.gameObjects.npc.Npc;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

/**
 * This class is responsible for managing dialogues in the game.
 * It reads dialogue data, activates dialogues for game objects, and updates the dialogue state.
 */
public class DialogueManager {

    private final GameState gameState;
    private final DialogueOverlay overlay;

    private final Map<String, List<Dialogue>> dialogues;

    public DialogueManager(GameState gameState) {
        this.gameState = gameState;
        this.overlay = (DialogueOverlay) gameState.getOverlayManager().getOverlays().get(PlayingState.DIALOGUE);
        this.dialogues = new HashMap<>();
        readDialogueFile();
    }

    private void readDialogueFile() {
        try {
            String file = Objects.requireNonNull(getClass().getResource(OBJECT_DIALOGUES)).getFile();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Gson gson = new Gson();
            Type dialogueFileType = new TypeToken<DialogueFile>() {}.getType();
            DialogueFile dialogueFile = gson.fromJson(reader, dialogueFileType);
            reader.close();

            for (Dialogue dialogue : dialogueFile.getDialogues()) {
                replacePlaceholders(dialogue);
                String key = dialogue.getObject();
                dialogues.putIfAbsent(key, new ArrayList<>());
                dialogues.get(key).add(dialogue);
            }
        } catch (Exception ignored) {
            Logger.getInstance().notify("Reading dialogue file failed!", Message.ERROR);
        }
    }

    private void replacePlaceholders(Dialogue dialogue) {
        if ("Board".equals(dialogue.getObject())) {
            KeyboardController kbc = Framework.getInstance().getKeyboardController();
            for (int i = 0; i < dialogue.getLines().size(); i++) {
                String line = dialogue.getLines().get(i);
                line = line.replace("%%JUMP%%", kbc.getKeyName("Jump").toUpperCase());
                line = line.replace("%%ATTACK%%", kbc.getKeyName("Attack").toUpperCase());
                dialogue.getLines().set(i, line);
            }
        }
    }

    /**
     * Activates a dialogue for the specified game object.
     *
     * @param id the dialogue ID
     * @param object the game object
     */
    public void activateDialogue(String id, GameObject object) {
        overlay.reset();
        gameState.setOverlay(PlayingState.DIALOGUE);
        Random random = new Random();
        int index = random.nextInt(getDialogues(id).size());
        if (object instanceof Npc) {
            Npc npc = (Npc) object;
            if (npc.getDialogueBehavior() == DialogueBehavior.RANDOM) {
                index = random.nextInt(getDialogues(id).size());
            }
            else index = npc.getDialogueIndicator();
        }
        Dialogue dialogue = getDialogues(id).get(index);
        dialogue.setActivated();
        setDialogueObject(dialogue, object);
    }

    /**
     * Sets the dialogue object in the overlay.
     *
     * @param dialogue the dialogue
     * @param object the game object
     */
    private void setDialogueObject(Dialogue dialogue, GameObject object) {
        overlay.setDialogues(dialogue, object);
    }

    /**
     * Accepts the current question in the dialogue.
     */
    public void acceptQuestion() {
        if (overlay.getQuestion() == null || !overlay.isOnLastQuestion()) return;
        String questName = overlay.getQuestion().getAnswers().get(0).getNext().replace("Quest: ", "");
        gameState.getQuestManager().startNPCQuest(questName);
        updateDialogue("QUESTION");
    }

    /**
     * Declines the current question in the dialogue.
     */
    public void declineQuestion() {
        if (overlay.getQuestion() == null || !overlay.isOnLastQuestion()) return;
        overlay.next();
        gameState.setOverlay(null);
        overlay.reset();
    }

    /**
     * Updates the dialogue state.
     * If the dialogue is finished, it sets the appropriate overlay based on the intersecting object.
     * <p>
     * @param type the dialogue type
     */
    public void updateDialogue(String type) {
        if (overlay.isOnLastQuestion() && type.equals("STANDARD")) {
            overlay.skipLetterAnim();
            return;
        }
        if(!overlay.next()) {
            if (Objects.equals(gameState.getObjectManager().getIntersectingObject(), "Blacksmith"))
                gameState.setOverlay(PlayingState.BLACKSMITH);
            else if (Objects.equals(gameState.getObjectManager().getIntersectingObject(), "Shop"))
                gameState.setOverlay(PlayingState.SHOP);
            else if (Objects.equals(gameState.getObjectManager().getIntersectingObject(), "SaveTotem"))
                gameState.setOverlay(PlayingState.SAVE);
            else if (gameState.getObjectManager().getIntersectingObject().contains("Npc")) {
                Npc npc = (Npc) gameState.getObjectManager().getIntersection();
                npc.increaseDialogueIndicator();
                gameState.setOverlay(null);
            }
            else gameState.setOverlay(null);

            overlay.reset();
        }
    }

    private List<Dialogue> getDialogues(String id) {
        return dialogues.get(id).stream()
                .filter(d -> !d.isActivated())
                .collect(Collectors.toList());
    }

}
