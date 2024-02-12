package platformer.ui.dialogue;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.*;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

@SuppressWarnings("unchecked")
public class DialogueManager {

    private final GameState gameState;
    private final DialogueOverlay overlay;

    private final Map<Class<? extends GameObject>, List<List<String>>> dialogues;

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
                addDialogue(dialogues, getObjectByName(object));
            }

        }
    }

    private <T extends GameObject> void addDialogue(List<String> dialogue, Class<T> objectClass) {
        if (objectClass == null) return;
        dialogue.remove(0);
        if (dialogues.containsKey(objectClass)) {
            dialogues.get(objectClass).add(dialogue);
        }
        else {
            List<List<String>> dialogues = new ArrayList<>();
            dialogues.add(dialogue);
            this.dialogues.put(objectClass, dialogues);
        }
    }

    private <T extends GameObject> Class<T> getObjectByName(String name) {
        switch (name) {
            case "Blacksmith": return (Class<T>) Blacksmith.class;
            case "Shop": return (Class<T>) Shop.class;
            case "SaveTotem": return (Class<T>) SaveTotem.class;
            case "Dog": return (Class<T>) Dog.class;
            case "Board": return (Class<T>) Board.class;
            default: return null;
        }
    }

    public <T extends GameObject> void setDialogueObject(List<String> dialogues, Class<T> dialogueClass) {
        overlay.setDialogues(dialogues, dialogueClass);
    }

    public void updateDialogue() {
        if(!overlay.next()) {
            if (gameState.getObjectManager().getIntersectingObject() == Blacksmith.class)
                gameState.setOverlay(PlayingState.BLACKSMITH);
            else if (gameState.getObjectManager().getIntersectingObject() == Shop.class)
                gameState.setOverlay(PlayingState.SHOP);
            else if (gameState.getObjectManager().getIntersectingObject() == SaveTotem.class)
                gameState.setOverlay(PlayingState.SAVE);
            else gameState.setOverlay(null);

            overlay.reset();
        }
    }

    public List<List<String>> getDialogues(Class<? extends GameObject> objectClass) {
        return dialogues.get(objectClass);
    }

}
