package platformer.ui.dialogue;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.Blacksmith;
import platformer.model.gameObjects.objects.Shop;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

@SuppressWarnings("unchecked")
public class DialogueManager {

    private final GameState gameState;
    private final DialogueOverlay overlay;

    private final Map<Class<? extends GameObject>, List<String>> dialogues;

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
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractContent(StringBuilder content) {
        String[] objects = content.toString().split(";");
        for (String segment : objects) {
            String[] parts = segment.split(":");
            String object = "";
            for (String s : parts) {
                if (s.contains("Object")) {
                    object = s.substring(7).trim();
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
        dialogues.put(objectClass, dialogue);
    }

    private <T extends GameObject> Class<T> getObjectByName(String name) {
        switch (name) {
            case "Blacksmith": return (Class<T>) Blacksmith.class;
            case "Shop": return (Class<T>) Shop.class;
            default: return null;
        }
    }

    public <T extends GameObject> void setDialogueObject(List<String> dialogues, Class<T> dialogueClass) {
        overlay.setDialogues(dialogues, dialogueClass);
    }

    public void updateDialogue() {
        if(!overlay.next()) {

            if (gameState.getObjectManager().isBlacksmithVisible())
                gameState.setOverlay(PlayingState.BLACKSMITH);
            else if (gameState.getObjectManager().isShopVisible())
                gameState.setOverlay(PlayingState.SHOP);

            overlay.reset();

        }
    }

    public List<String> getDialogues(Class<? extends GameObject> objectClass) {
        return dialogues.get(objectClass);
    }

}
