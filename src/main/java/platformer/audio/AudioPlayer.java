package platformer.audio;

/**
 * Interface for the AudioPlayer class.
 * @param <U> The type of the song.
 * @param <V> The type of the sound.
 */
public interface AudioPlayer<U, V> {

    /**
     * Plays the song.
     * @param song The song to play.
     */
    void playSong(U song);

    /**
     * Stops the song.
     */
    void stopSong();

    /**
     * Plays the sound.
     * @param sound The sound to play.
     */
    void playSound(V sound);

    /**
     * Stops the sound.
     * @param sound The sound to stop.
     */
    void stopSound(V sound);

    /**
     * Pauses the sounds.
     */
    void pauseSounds();

    /**
     * Unpauses the sounds.
     */
    void unpauseSounds();

    /**
     * Pauses the song.
     */
    void pauseSong();

    /**
     * Unpauses the song.
     */
    void unpauseSong();

    /**
     * Sets the level song.
     */
    void setLevelSong();

    /**
     * Plays slash sound.
     */
    void playSlashSound();

    /**
     * Plays hit sound.
     */
    void playHitSound();

    /**
     * Plays block sound.
     *
     * @param type The type of the block.
     */
    void playBlockSound(String type);

    /**
     * Plays crate sound.
     */
    void playCrateSound();

    /**
     * Mutes the song.
     */
    void songMute();

    /**
     * Mutes the sound.
     */
    void soundMute();

    /**
     * Sets the music volume.
     *
     * @param musicVolume The volume of the music.
     */
    void setMusicVolume(float musicVolume);


    /**
     * Sets the sfx volume.
     *
     * @param sfxVolume The volume of the sfx.
     */
    void setSfxVolume(float sfxVolume);

    /**
     * Destroys the audio player.
     */
    void destroy();
}
