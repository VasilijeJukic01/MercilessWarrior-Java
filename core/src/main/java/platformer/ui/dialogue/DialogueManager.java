package platformer.ui.dialogue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.Npc;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

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
                String key = dialogue.getObject();
                dialogues.putIfAbsent(key, new ArrayList<>());
                dialogues.get(key).add(dialogue);
            }
        } catch (Exception ignored) {
            System.out.println(ignored);
        }
    }

    public void setDialogueObject(List<String> dialogues, GameObject object) {
        overlay.setDialogues(dialogues, object);
    }

    public void updateDialogue() {
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

    public List<Dialogue> getDialogues(String id) {
        return dialogues.get(id).stream()
                .filter(d -> !d.isActivated())
                .collect(Collectors.toList());
    }

    public void setActivated(Dialogue dialogue) {
        dialogues.get(dialogue.getObject()).stream()
                .filter(d -> d.getId().equals(dialogue.getId()))
                .findFirst()
                .ifPresent(Dialogue::setActivated);
    }

}
