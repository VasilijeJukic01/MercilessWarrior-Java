package platformer.audio;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.utils.ValueEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpenAL implements AudioPlayer {

    private final List<Integer> songs = new ArrayList<>();
    private final List<OpenALSource> songSources = new ArrayList<>();
    private final List<Integer> sounds = new ArrayList<>();
    private final List<OpenALSource> soundSources = new ArrayList<>();
    private final List<Integer> pausedSounds = new ArrayList<>();

    private int currentSong;
    private float volume = 0.5f;
    private boolean songMute, soundMute;
    private final Random rand = new Random();

    public OpenAL() {
        initAL();
        loadSongs();
        loadSounds();
        setListenerData();
        Logger.getInstance().notify("Audio loaded successfully!", Message.INFORMATION);
    }

    private void initAL() {
        try {
            AL.create();
        }
        catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    // Data
    private void loadAudio(ValueEnum[] audioArray, List<Integer> buffers, List<OpenALSource> sources) {
        for (ValueEnum audio : audioArray) {
            String id = audio.getValue();
            buffers.add(loadBuffers("audio/" + id + ".wav"));
            sources.add(new OpenALSource());
        }
    }

    private void loadSongs() {
        Songs[] songsArray = Songs.values();
        loadAudio(songsArray, songs, songSources);
        updateSongVolume();
    }

    private void loadSounds() {
        Sounds[] soundsArray = Sounds.values();
        loadAudio(soundsArray, sounds, soundSources);
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
    public void pauseSounds() {
        for (Integer sound : sounds) {
            if (soundSources.get(sounds.indexOf(sound)).isPlaying()) {
                pausedSounds.add(sound);
                soundSources.get(sounds.indexOf(sound)).pause();
            }
        }
    }

    @Override
    public void unpauseSounds() {
        for (Integer sound : sounds) {
            if (pausedSounds.contains(sound))
                soundSources.get(sounds.indexOf(sound)).unpause();
        }
        pausedSounds.clear();
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
