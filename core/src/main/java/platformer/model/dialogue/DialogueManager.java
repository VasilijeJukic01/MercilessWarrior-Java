package platformer.model.dialogue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.FightInitiatedEvent;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.dialogue.question.Answer;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.npc.DialogueBehavior;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.state.types.PlayingState;
import platformer.ui.overlays.DialogueOverlay;
import platformer.ui.overlays.OverlayManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.OBJECT_DIALOGUES;

/**
 * This class is responsible for managing dialogues in the game.
 * It reads dialogue data, activates dialogues for game objects, and updates the dialogue state.
 */
public class DialogueManager {

    private GameContext context;
    private DialogueOverlay overlay;
    private OverlayManager overlayManager;

    private final Map<String, List<Dialogue>> dialogues;

    public DialogueManager() {
        this.dialogues = new HashMap<>();
        readDialogueFile();
    }

    public void wire(GameContext context) {
        this.overlayManager = context.getOverlayManager();
        this.context = context;
    }

    private DialogueOverlay getOverlay() {
        if (this.overlay == null && this.overlayManager != null) {
            this.overlay = (DialogueOverlay) this.overlayManager.getOverlays().get(PlayingState.DIALOGUE);
        }
        return this.overlay;
    }

    private void readDialogueFile() {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(OBJECT_DIALOGUES));
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            Type dialogueFileType = new TypeToken<DialogueFile>() {}.getType();
            DialogueFile dialogueFile = gson.fromJson(reader, dialogueFileType);

            for (Dialogue dialogue : dialogueFile.getDialogues()) {
                replacePlaceholders(dialogue);
                String key = dialogue.getObject();
                dialogues.putIfAbsent(key, new ArrayList<>());
                dialogues.get(key).add(dialogue);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Reading dialogue file failed: " + e.getMessage(), Message.ERROR);
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
        List<Dialogue> availableDialogues = getDialogues(id);
        if (availableDialogues.isEmpty()) return;

        if (getOverlay() != null) getOverlay().reset();
        EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.DIALOGUE));
        Random random = new Random();
        int index = random.nextInt(availableDialogues.size());
        if (object instanceof Npc npc) {
            if (npc.getDialogueBehavior() == DialogueBehavior.RANDOM) {
                index = random.nextInt(availableDialogues.size());
            }
            else index = Math.min(npc.getDialogueIndicator(), availableDialogues.size() - 1);
        }
        Dialogue dialogue = availableDialogues.get(index);
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
        getOverlay().setDialogues(dialogue, object);
    }

    /**
     * Accepts the current question in the dialogue.
     */
    public void acceptQuestion() {
        if (getOverlay().getQuestion() == null || !getOverlay().isOnLastQuestion()) return;
        Answer acceptAnswer = getOverlay().getQuestion().getAnswers().get(0);
        executeAnswer(acceptAnswer, true);
    }

    /**
     * Declines the current question in the dialogue.
     */
    public void declineQuestion() {
        if (getOverlay().getQuestion() == null || !getOverlay().isOnLastQuestion()) return;
        List<Answer> answers = getOverlay().getQuestion().getAnswers();
        if (answers.size() > 1) executeAnswer(answers.get(1), false);
        else {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            getOverlay().reset();
        }
    }

    private void executeAnswer(Answer answer, boolean isAccept) {
        if (answer.getQuest() != null) startQuestIfNeeded(answer.getQuest());
        else if (answer.getNext() != null && answer.getNext().startsWith("Quest: ")) {
            startQuestIfNeeded(answer.getNext().replace("Quest: ", ""));
        }

        if (answer.getRequiredItems() != null || answer.getRequiredCoins() > 0) {
            Map<String, Integer> items = answer.getRequiredItems() != null ? answer.getRequiredItems() : Collections.emptyMap();
            Player player = context.getPlayer();
            if (!player.getInventory().hasAndConsumeResources(items, answer.getRequiredCoins(), player)) {
                NpcType speaker = (context.getObjectManager().getIntersection() instanceof Npc npc) ? npc.getNpcType() : null;
                getOverlay().reset();
                activateDialogueById(answer.getFailureDialogue(), speaker);
                return;
            }
        }

        if (answer.getCutscene() != null) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            getOverlay().reset();
            context.getCutsceneManager().startCutsceneById(answer.getCutscene());
            return;
        }

        // TODO: Temp
        if ("FIGHT_RORIC".equals(answer.getNext())) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            getOverlay().reset();
            EventBus.getInstance().publish(new FightInitiatedEvent("RORIC"));
            return;
        }

        if (isAccept) updateDialogue("QUESTION");
        else {
            if (context.getObjectManager().getIntersection() instanceof Npc npc) {
                npc.increaseDialogueIndicator();
            }
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            getOverlay().reset();
        }
    }

    private void startQuestIfNeeded(String questName) {
        if (!context.getQuestManager().isQuestActive(questName) && !context.getQuestManager().isQuestCompleted(questName)) {
            context.getQuestManager().startNPCQuest(questName);
        }
    }

    /**
     * Updates the dialogue state.
     * If the dialogue is finished, it sets the appropriate overlay based on the intersecting object.
     * <p>
     * @param type the dialogue type
     */
    public void updateDialogue(String type) {
        if (getOverlay().isOnLastQuestion() && type.equals("STANDARD")) {
            getOverlay().skipLetterAnim();
            return;
        }
        if(!getOverlay().next()) {
            ObjectManager objectManager = context.getGameState().getObjectManager();
            String intersect = objectManager.getIntersectingObject();

            if ("Blacksmith".equals(intersect))
                EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.BLACKSMITH));
            else if ("Shop".equals(intersect))
                EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.SHOP));
            else if ("SaveTotem".equals(intersect))
                EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.SAVE));
            else if (intersect != null && intersect.contains("Npc")) {
                Npc npc = (Npc) objectManager.getIntersection();
                npc.increaseDialogueIndicator();
                EventBus.getInstance().publish(new OverlayChangeEvent(null));
            }
            else EventBus.getInstance().publish(new OverlayChangeEvent(null));

            getOverlay().reset();
        }
    }

    /**
     * Activates a specific dialogue entry by its unique ID in dialogues.json.
     *
     * @param dialogueId  The unique ID of the dialogue entry.
     * @param speakerType Optional NPC type used to display the speaker's portrait and name.
     */
    public void activateDialogueById(String dialogueId, NpcType speakerType) {
        if (dialogueId == null) return;
        for (List<Dialogue> list : dialogues.values()) {
            for (Dialogue d : list) {
                if (dialogueId.equals(d.getId())) {
                    if (getOverlay() != null) getOverlay().reset();
                    EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.DIALOGUE));
                    d.setActivated();

                    GameObject speakerObj = null;
                    if (speakerType != null) {
                        speakerObj = context.getObjectManager().getNpcByType(speakerType);
                        if (speakerObj == null) {
                            speakerObj = new Npc(ObjType.NPC, 0, 0, speakerType);
                        }
                    }
                    setDialogueObject(d, speakerObj);
                    return;
                }
            }
        }
    }

    private List<Dialogue> getDialogues(String id) {
        if (!dialogues.containsKey(id)) return Collections.emptyList();
        String currentLocation = context.getInteriorManager().isInterior() ? context.getInteriorManager().getCurrentInteriorId() : "main_map";

        return dialogues.get(id).stream()
                .filter(d -> !d.isActivated())
                .filter(d -> !d.isCutsceneOnly())
                .filter(d -> d.getLocation() == null || d.getLocation().equals(currentLocation))
                .filter(d -> d.getRequiredFlag() == null || context.getWorldMemoryManager().hasFlag(d.getRequiredFlag()))
                .filter(d -> d.getExcludeFlag() == null || !context.getWorldMemoryManager().hasFlag(d.getExcludeFlag()))
                .collect(Collectors.toList());
    }

}
