package platformer.audio;

/**
 * Defines the contract for an audio playback system in the game.
 * This interface provides a standardized way to control different types of audio, such as music, sound effects, and ambient sounds.
 *
 * @param <U> The type representing songs.
 * @param <V> The type representing sound effects.
 * @param <W> The type representing ambient sounds.
 */
public interface AudioPlayer<U, V, W> {

    /**
     * Plays the specified song. If another song is already playing, it should be stopped first.
     * Songs are typically looped.
     *
     * @param song The song to play.
     */
    void playSong(U song);

    /**
     * Stops the currently playing song.
     */
    void stopSong();

    /**
     * Plays a one-shot sound effect.
     *
     * @param sound The sound effect to play.
     */
    void playSound(V sound);

    /**
     * Stops a specific sound effect if it is currently playing.
     *
     * @param sound The sound effect to stop.
     */
    void stopSound(V sound);

    /**
     * Pauses all currently playing sound effects.
     */
    void pauseSounds();

    /**
     * Resumes all sound effects that were paused.
     */
    void unpauseSounds();

    /**
     * Plays the specified ambient sound. Ambient sounds are typically looped.
     *
     * @param ambience The ambient sound to play.
     */
    void playAmbience(W ambience);

    /**
     * Stops the currently playing ambient sound.
     */
    void stopAmbience();

    /**
     * Pauses the currently playing song.
     */
    void pauseSong();

    /**
     * Resumes the currently paused song.
     */
    void unpauseSong();

    /**
     * Sets and plays the appropriate song for the current level.
     */
    void setLevelSong();

    /**
     * Plays a random sword slash sound effect.
     */
    void playSlashSound();

    /**
     * Plays a random hit sound effect.
     */
    void playHitSound();

    /**
     * Plays a random block sound effect based on the blocker's type.
     *
     * @param type The type of entity blocking (e.g., "Player", "Enemy").
     */
    void playBlockSound(String type);

    /**
     * Plays a random crate breaking sound effect.
     */
    void playCrateSound();

    /**
     * Toggles the mute state for songs.
     */
    void songMute();

    /**
     * Toggles the mute state for sound effects and ambient sounds.
     */
    void soundMute();

    /**
     * Sets the volume for all music.
     *
     * @param musicVolume The volume level, typically from 0.0f to 1.0f.
     */
    void setMusicVolume(float musicVolume);

    /**
     * Sets the volume for all sound effects and ambient sounds.
     *
     * @param sfxVolume The volume level, typically from 0.0f to 1.0f.
     */
    void setSfxVolume(float sfxVolume);

    /**
     * Cleans up and releases all audio resources used by the player.
     * This should be called when the game is shutting down.
     */
    void destroy();
}
