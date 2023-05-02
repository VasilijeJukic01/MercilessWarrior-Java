package platformer.audio;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpenAL implements AudioPlayer {

    private final List<Integer> songs = new ArrayList<>();
    private final List<OpenALSource> songSources = new ArrayList<>();
    private final List<Integer> sounds = new ArrayList<>();
    private final List<OpenALSource> soundSources = new ArrayList<>();

    private int currentSong;
    private float volume = 0.5f;
    private boolean songMute, soundMute;
    private final Random rand = new Random();

    public OpenAL() {
        init();
        loadSongs();
        loadSounds();
        setListenerData();
    }

    private void init() {
        try {
            AL.create();
        }
        catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    // Data
    private void loadSongs() {
        String[] ids = {"menuTheme", "forestTheme"};
        for (String id : ids) {
            songs.add(loadBuffers("audio/" + id + ".wav"));
            songSources.add(new OpenALSource());
        }
        updateSongVolume();
    }

    private void loadSounds() {
        String[] ids = {"airSlash1", "airSlash2", "airSlash3", "attackSlash1", "attackSlash2", "gameOver", "crateBreak1", "crateBreak2", "skeletonD1", "playerDash", "arrowSound",
                        "block1", "block2", "swordBlock1", "swordBlock2", "swordBlock3", "fireSound1", "ghoulHide", "ghoulReveal", "ghoulDeath"};
        for (String id : ids) {
            sounds.add(loadBuffers("audio/" + id + ".wav"));
            soundSources.add(new OpenALSource());
        }
        updateSoundVolume();
    }

    // Core
    private int loadBuffers(String file) {
        int buffer = AL10.alGenBuffers();
        WaveData waveData = WaveData.create(file);
        AL10.alBufferData(buffer, waveData.format, waveData.data, waveData.samplerate);
        waveData.dispose();
        return buffer;
    }

    public void setListenerData() {
        AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
        AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
    }

    // Mediator
    @Override
    public void playSong(int song) {
        stopSong();
        currentSong = song;
        if (!songMute) setVolume(volume);
        songSources.get(currentSong).play(songs.get(currentSong));
        songSources.get(currentSong).loop(true);
    }

    @Override
    public void pauseSong() {
        songSources.get(currentSong).pause();
    }

    @Override
    public void unpauseSong() {
        songSources.get(currentSong).unpause();
    }

    @Override
    public void stopSong() {
        songSources.get(currentSong).stop();
    }

    @Override
    public void playSound(int sound) {
        soundSources.get(sound).play(sounds.get(sound));
    }

    @Override
    public void stopSound(int sound) {
        soundSources.get(sound).stop();
    }

    @Override
    public void setLevelSong() {
        playSong(Songs.FOREST_1.ordinal());
    }

    @Override
    public void playSlashSound() {
        int start = 0;
        start += rand.nextInt(3);
        playSound(start);
    }

    @Override
    public void playHitSound() {
        int start = 3;
        start += rand.nextInt(2);
        playSound(start);
    }

    @Override
    public void playBlockSound(String type) {
        int start = 0;
        if (type.equals("Enemy")) {
            start = 11;
            start += rand.nextInt(2);
        }
        else if (type.equals("Player")) {
            start = 13;
            start += rand.nextInt(3);
        }
        playSound(start);
    }

    @Override
    public void playCrateSound() {
        int start = 6;
        start += rand.nextInt(2);
        playSound(start);
    }

    // Mute
    private void muteSource(List<Integer> buffers, List<OpenALSource> sources, boolean isMute) {
        for (Integer b : buffers) {
            if (isMute) sources.get(buffers.indexOf(b)).changeVolume(0);
            else sources.get(buffers.indexOf(b)).changeVolume(volume);
        }
    }

    @Override
    public void songMute() {
        songMute = !songMute;
        muteSource(songs, songSources, songMute);
    }

    @Override
    public void soundMute() {
        soundMute = !soundMute;
        muteSource(sounds, soundSources, soundMute);
    }

    // Volume
    private void updateSongVolume() {
        if (songMute) return;
        songSources.get(currentSong).changeVolume(volume);
    }

    private void updateSoundVolume() {
        for (Integer sound : sounds) {
            soundSources.get(sounds.indexOf(sound)).changeVolume(volume);
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        updateSongVolume();
        updateSoundVolume();
    }

    @Override
    public void destroy() {
        for (Integer buffer : songs) {
            AL10.alDeleteBuffers(buffer);
        }
        for (Integer buffer : sounds) {
            AL10.alDeleteBuffers(buffer);
        }
        AL.destroy();
    }

}
