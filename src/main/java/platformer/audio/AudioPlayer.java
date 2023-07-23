package platformer.audio;

public interface AudioPlayer {

    void playSong(int song);

    void stopSong();

    void playSound(int sound);

    void stopSound(int sound);

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

    void setVolume(float volume);

    void destroy();
}
