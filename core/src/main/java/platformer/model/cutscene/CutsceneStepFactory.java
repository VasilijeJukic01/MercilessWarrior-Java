package platformer.model.cutscene;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestObjectiveType;
import platformer.physics.CollisionDetector;
import platformer.state.types.PlayingState;

import java.awt.*;

import static platformer.constants.Constants.TILES_SIZE;

/**
 * Factory that builds executable {@link CutsceneStep} commands from declarative {@link CutsceneStepData}.
 */
public final class CutsceneStepFactory {

    private CutsceneStepFactory() {}

    public static CutsceneStep createStep(CutsceneStepData data) {
        if (data == null || data.getType() == null) {
            return new WaitStep(1);
        }
        return switch (data.getType().toUpperCase()) {
            case "LETTERBOX" -> new LetterboxStep(Boolean.TRUE.equals(data.getActive()));
            case "CAMERA_PAN" -> new CameraPanStep(
                    data.getTileX() != null ? data.getTileX() : 0,
                    data.getTileY() != null ? data.getTileY() : 0,
                    data.getDuration() != null ? data.getDuration() : 120
            );
            case "CAMERA_RESET" -> new CameraResetStep(data.getDuration() != null ? data.getDuration() : 100);
            case "WAIT" -> new WaitStep(data.getDuration() != null ? data.getDuration() : 60);
            case "FADE" -> new FadeStep(
                    data.getTargetAlpha() != null ? data.getTargetAlpha() : 1.0f,
                    data.getDuration() != null ? data.getDuration() : 60
            );
            case "DESPAWN_FOLLOWER" -> new DespawnFollowerStep();
            case "SPAWN_NPC" -> new SpawnNpcStep(
                    NpcType.valueOf(data.getNpcType()),
                    data.getTileX() != null ? data.getTileX() : 10,
                    data.getTileY() != null ? data.getTileY() : 10,
                    data.getDirection() != null ? Direction.valueOf(data.getDirection()) : Direction.LEFT
            );
            case "TELEPORT_PLAYER" -> new TeleportPlayerStep(
                    data.getTileX() != null ? data.getTileX() : 10,
                    data.getTileY() != null ? data.getTileY() : 10
            );
            case "DIALOGUE" -> new DialogueStep(
                    data.getDialogueId(),
                    data.getSpeaker() != null ? NpcType.valueOf(data.getSpeaker()) : null
            );
            case "COMPLETE_OBJECTIVE" -> new CompleteObjectiveStep(
                    data.getQuestName(),
                    data.getStepIndex(),
                    data.getObjectiveType() != null ? QuestObjectiveType.valueOf(data.getObjectiveType()) : null,
                    data.getTarget() != null ? ObjectiveTarget.valueOf(data.getTarget()) : null
            );
            case "RELOAD_LEVEL_VARIANT" -> new ReloadLevelVariantStep();
            case "START_BOSS_FIGHT" -> new StartBossFightStep();
            default -> new WaitStep(1);
        };
    }

    private static class LetterboxStep implements CutsceneStep {
        private final boolean active;

        LetterboxStep(boolean active) {
            this.active = active;
        }

        @Override
        public void start(GameContext context) {
            context.getScreenEffectsManager().setLetterboxActive(active);
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class CameraPanStep implements CutsceneStep {
        private final int tileX, tileY, duration;
        private double startX, startY;
        private int ticks = 0;

        CameraPanStep(int tileX, int tileY, int duration) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.duration = Math.max(1, duration);
        }

        @Override
        public void start(GameContext context) {
            ticks = 0;
            startX = context.getCamera().getFocusX();
            startY = context.getCamera().getFocusY();
        }

        @Override
        public boolean update(GameContext context) {
            ticks++;
            double t = Math.min(1.0, (double) ticks / duration);
            double eased = 0.5 - 0.5 * Math.cos(Math.PI * t);

            double endX = tileX * TILES_SIZE;
            double endY = tileY * TILES_SIZE;
            int currentX = (int) Math.round(startX + eased * (endX - startX));
            int currentY = (int) Math.round(startY + eased * (endY - startY));

            context.getCamera().setCutsceneTarget(new Point(currentX, currentY));
            return ticks >= duration;
        }
    }

    private static class CameraResetStep implements CutsceneStep {
        private final int duration;
        private double startX, startY;
        private int ticks = 0;

        CameraResetStep(int duration) {
            this.duration = Math.max(1, duration);
        }

        @Override
        public void start(GameContext context) {
            ticks = 0;
            startX = context.getCamera().getFocusX();
            startY = context.getCamera().getFocusY();
        }

        @Override
        public boolean update(GameContext context) {
            ticks++;
            double t = Math.min(1.0, (double) ticks / duration);
            double eased = 0.5 - 0.5 * Math.cos(Math.PI * t);

            Player player = context.getPlayer();
            double endX = player.getHitBox().x;
            double endY = player.getHitBox().y;
            int currentX = (int) Math.round(startX + eased * (endX - startX));
            int currentY = (int) Math.round(startY + eased * (endY - startY));

            context.getCamera().setCutsceneTarget(new Point(currentX, currentY));
            if (ticks >= duration) {
                context.getCamera().clearCutsceneTarget();
                return true;
            }
            return false;
        }
    }

    private static class WaitStep implements CutsceneStep {
        private final int duration;
        private int ticks = 0;

        WaitStep(int duration) {
            this.duration = duration;
        }

        @Override
        public void start(GameContext context) {
            ticks = 0;
        }

        @Override
        public boolean update(GameContext context) {
            ticks++;
            return ticks >= duration;
        }
    }

    private static class FadeStep implements CutsceneStep {
        private final float targetAlpha;
        private final int duration;
        private float startAlpha;
        private int ticks = 0;

        FadeStep(float targetAlpha, int duration) {
            this.targetAlpha = targetAlpha;
            this.duration = Math.max(1, duration);
        }

        @Override
        public void start(GameContext context) {
            ticks = 0;
            startAlpha = context.getScreenEffectsManager().getCutsceneFadeAlpha();
        }

        @Override
        public boolean update(GameContext context) {
            ticks++;
            float t = Math.min(1.0f, (float) ticks / duration);
            float current = startAlpha + t * (targetAlpha - startAlpha);
            context.getScreenEffectsManager().setCutsceneFadeAlpha(current);
            return ticks >= duration;
        }
    }

    private static class DespawnFollowerStep implements CutsceneStep {
        @Override
        public void start(GameContext context) {
            context.getObjectManager().despawnFollower();
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class SpawnNpcStep implements CutsceneStep {
        private final NpcType npcType;
        private final int tileX, tileY;
        private final Direction direction;

        SpawnNpcStep(NpcType npcType, int tileX, int tileY, Direction direction) {
            this.npcType = npcType;
            this.tileX = tileX;
            this.tileY = tileY;
            this.direction = direction;
        }

        @Override
        public void start(GameContext context) {
            int[][] lvlData = context.getLevelManager().getCurrentLevel().getLvlData();
            Npc npc = context.getObjectManager().getNpcByType(npcType);
            if (npc == null) {
                npc = new Npc(ObjType.NPC, tileX * TILES_SIZE, tileY * TILES_SIZE, npcType);
                context.getObjectManager().addGameObject(npc);
            }
            double groundY = CollisionDetector.getGroundY(tileX * TILES_SIZE, tileY * TILES_SIZE, lvlData);
            npc.getHitBox().x = tileX * TILES_SIZE;
            npc.getHitBox().y = groundY - npc.getHitBox().height;
            npc.setDirection(direction);
            npc.setAlive(true);

            context.getObjectManager().applyObjectMetadata();
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class TeleportPlayerStep implements CutsceneStep {
        private final int tileX, tileY;

        TeleportPlayerStep(int tileX, int tileY) {
            this.tileX = tileX;
            this.tileY = tileY;
        }

        @Override
        public void start(GameContext context) {
            int[][] lvlData = context.getLevelManager().getCurrentLevel().getLvlData();
            Player player = context.getPlayer();
            int px = tileX * TILES_SIZE;
            double groundY = CollisionDetector.getGroundY(px, tileY * TILES_SIZE, lvlData);
            player.setSpawn(new Point(px, (int) (groundY - player.getHitBox().height - 1)));
            player.resetDirections();
            context.getCamera().snapToPlayer(player);
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class DialogueStep implements CutsceneStep {
        private final String dialogueId;
        private final NpcType speaker;
        private boolean opened = false;

        DialogueStep(String dialogueId, NpcType speaker) {
            this.dialogueId = dialogueId;
            this.speaker = speaker;
        }

        @Override
        public void start(GameContext context) {
            opened = false;
            context.getDialogueManager().activateDialogueById(dialogueId, speaker);
        }

        @Override
        public boolean update(GameContext context) {
            PlayingState state = context.getGameState().getActiveState();
            if (state == PlayingState.DIALOGUE) {
                opened = true;
                return false;
            }
            return opened;
        }
    }

    private static class CompleteObjectiveStep implements CutsceneStep {
        private final String questName;
        private final Integer stepIndex;
        private final QuestObjectiveType objectiveType;
        private final ObjectiveTarget target;

        CompleteObjectiveStep(String questName, Integer stepIndex, QuestObjectiveType objectiveType, ObjectiveTarget target) {
            this.questName = questName;
            this.stepIndex = stepIndex;
            this.objectiveType = objectiveType;
            this.target = target;
        }

        @Override
        public void start(GameContext context) {
            if (questName != null && stepIndex != null) {
                context.getQuestManager().completeObjectiveStep(questName, stepIndex);
            }
            if (objectiveType != null && target != null) {
                context.getQuestManager().updateQuestProgress(objectiveType, target);
            }
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class ReloadLevelVariantStep implements CutsceneStep {
        @Override
        public void start(GameContext context) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.CRAFTING);
            context.getLevelManager().reloadCurrentLevelVariant();
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }

    private static class StartBossFightStep implements CutsceneStep {
        @Override
        public void start(GameContext context) {
            context.getEnemyManager().getAllEnemies().stream()
                    .filter(e -> e instanceof Lancer)
                    .map(e -> (Lancer) e)
                    .findFirst()
                    .ifPresent(lancer -> lancer.engageFight(context.getObjectManager()));
        }

        @Override
        public boolean update(GameContext context) {
            return true;
        }
    }
}