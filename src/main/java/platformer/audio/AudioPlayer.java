package platformer.audio;

public interface AudioPlayer {

    void playSong(int song);

    void stopSong();

    void playSound(int sound);

    void pauseSong();

    void unpauseSong();

    void setLevelSong();

    void playSlashSound();

    void playHitSound();

    void playCrateSound();

    void songMute();

    void soundMute();

    void setVolume(float volume);

    void destroy();
}
