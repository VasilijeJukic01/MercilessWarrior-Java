package platformer.audio;

public interface AudioPlayer<U, V> {

    void playSong(U song);

    void stopSong();

    void playSound(V sound);

    void stopSound(V sound);

    void pauseSounds();

    void unpauseSounds();

    void pauseSong();

    void unpauseSong();

    void setLevelSong();

    void playSlashSound();

    void playHitSound();

    void playBlockSound(String type);

    void playCrateSound();

    void songMute();

    void soundMute();

    void setMusicVolume(float musicVolume);

    void setSfxVolume(float sfxVolume);

    void destroy();
}
