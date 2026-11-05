package platformer.model.cutscene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.player.Player;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.Trigger;
import platformer.state.types.PlayingState;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static platformer.constants.FilePaths.CUTSCENES_PATH;

/**
 * Data-driven manager that loads cutscenes from cutscenes.json, evaluates spatial or room-clear triggers, and executes a queue of {@link CutsceneStep} commands.
 */
public class CutsceneManager {

    private GameContext context;
    private List<CutsceneDefinition> definitions = new ArrayList<>();
    private final Queue<CutsceneStep> activeSteps = new ArrayDeque<>();
    private CutsceneStep currentStep = null;
    private CutsceneDefinition activeDefinition = null;

    public CutsceneManager() {
        loadCutscenes();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void loadCutscenes() {
        try (InputStream is = getClass().getResourceAsStream(CUTSCENES_PATH)) {
            if (is == null) return;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<CutsceneDefinition>>() {}.getType();
                List<CutsceneDefinition> loaded = new Gson().fromJson(reader, listType);
                if (loaded != null) {
                    this.definitions = loaded;
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load cutscenes.json: " + e.getMessage(), Message.ERROR);
        }
    }

    public void update() {
        if (isActive()) {
            if (context.getGameState().getActiveState() != PlayingState.DIALOGUE) {
                context.getPlayer().resetDirections();
                context.getGameState().getStateController().resetKeys();
            }

            if (currentStep == null) {
                currentStep = activeSteps.poll();
                if (currentStep != null) {
                    currentStep.start(context);
                }
                else {
                    finishCutscene();
                    return;
                }
            }

            if (currentStep.update(context)) {
                currentStep = null;
            }
            return;
        }

        evaluateTriggers();
    }

    private void evaluateTriggers() {
        if (context.getInteriorManager().isInterior() || context.getLevelManager().isInArena()) return;
        String currentLevelId = context.getLevelManager().getCurrentLevelId();

        for (CutsceneDefinition def : definitions) {
            if (def.getLevelId() != null && !def.getLevelId().equalsIgnoreCase(currentLevelId)) continue;
            if (def.getOnceFlag() != null && context.getWorldMemoryManager().hasFlag(def.getOnceFlag())) continue;
            if (def.getRequiredActiveQuest() != null && !context.getQuestManager().isQuestActive(def.getRequiredActiveQuest())) {
                continue;
            }

            if ("CUTSCENE_TILE".equalsIgnoreCase(def.getTriggerType()) && isPlayerTouchingCutsceneTrigger()) {
                startCutscene(def);
                return;
            }
            else if ("ROOM_ENEMIES_CLEARED".equalsIgnoreCase(def.getTriggerType()) && areAllRoomEnemiesDefeated()) {
                startCutscene(def);
                return;
            }
        }
    }

    private boolean isPlayerTouchingCutsceneTrigger() {
        Player player = context.getPlayer();
        for (Trigger trigger : context.getLevelManager().getCurrentLevel().getTriggers()) {
            if (trigger.type() == LvlTriggerType.CUTSCENE && player.getHitBox().intersects(trigger.bounds())) {
                return true;
            }
        }
        return false;
    }

    private boolean areAllRoomEnemiesDefeated() {
        List<Enemy> enemies = context.getEnemyManager().getAllEnemies();
        if (enemies.isEmpty()) return false;
        for (Enemy e : enemies) {
            if (e.isAlive()) return false;
        }
        return true;
    }

    public void startCutscene(CutsceneDefinition def) {
        this.activeDefinition = def;
        this.activeSteps.clear();
        this.currentStep = null;

        if (def.getOnceFlag() != null) {
            context.getWorldMemoryManager().addFlag(def.getOnceFlag());
        }

        for (CutsceneStepData stepData : def.getSteps()) {
            activeSteps.add(CutsceneStepFactory.createStep(stepData));
        }

        context.getPlayer().resetDirections();
        context.getGameState().getStateController().resetKeys();
        Logger.getInstance().notify("Started cutscene: " + def.getId(), Message.INFORMATION);
    }

    private void finishCutscene() {
        this.activeDefinition = null;
        this.currentStep = null;
        this.activeSteps.clear();
        context.getCamera().clearCutsceneTarget();
        context.getScreenEffectsManager().setLetterboxActive(false);
        context.getScreenEffectsManager().setCutsceneFadeAlpha(0.0f);
    }

    public boolean isActive() {
        return activeDefinition != null || currentStep != null || !activeSteps.isEmpty();
    }

    public void reset() {
        finishCutscene();
    }
}