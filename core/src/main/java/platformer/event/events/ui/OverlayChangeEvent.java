package platformer.event.events.ui;

import platformer.event.Event;
import platformer.state.types.PlayingState;

/**
 * An event that triggers when the game overlay changes, such as switching between different UI overlays.
 * This is used to update the current playing state of the game.
 *
 * @param newOverlay The new overlay state that is being set.
 */
public record OverlayChangeEvent(PlayingState newOverlay) implements Event {}