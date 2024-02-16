package platformer.ui.dialogue;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.npc.Npc;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

public class DialogueManager {

    private final GameState gameState;
    private final DialogueOverlay overlay;

    private final Map<String, List<List<String>>> dialogues;

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
            String line;
            StringBuilder content = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            extractContent(content);

        }
        catch (Exception ignored) {}
    }

    private void extractContent(StringBuilder content) {
        String[] objects = content.toString().split(";");
        for (String segment : objects) {
            String[] parts = segment.split(":");
            String object = "";
            for (String s : parts) {
                if (s.contains("Object")) {
                    object = s.substring(7).trim().replaceAll("[0-9]", "");
                    continue;
                }
                String[] sentences = s.split("\n");
                List<String> dialogues = new ArrayList<>(Arrays.asList(sentences));
                addDialogue(dialogues, object);
            }

        }
    }

    private void addDialogue(List<String> dialogue, String id) {
        if (id == null) return;
        dialogue.remove(0);
        if (dialogues.containsKey(id)) {
            dialogues.get(id).add(dialogue);
        }
        else {
            List<List<String>> dialogues = new ArrayList<>();
            dialogues.add(dialogue);
            this.dialogues.put(id, dialogues);
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

    public List<List<String>> getDialogues(String id) {
        return dialogues.get(id);
    }

}
