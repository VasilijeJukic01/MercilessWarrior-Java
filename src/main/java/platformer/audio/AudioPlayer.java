package platformer.audio;

public interface AudioPlayer {

    void playSong(Song song);

    void stopSong();

    void playSound(Sound sound);

    void stopSound(Sound sound);

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
