package platformer.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.utils.ValueEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * OpenAL class that implements the AudioPlayer interface.
 * It is responsible for loading and playing audio files.
 * It uses the OpenAL library to play audio files.
 */
public class OpenAL implements AudioPlayer<Song, Sound, Ambience>  {

    private final List<Integer> songs = new ArrayList<>();
    private final List<OpenALSource> songSources = new ArrayList<>();
    private final List<Integer> sounds = new ArrayList<>();
    private final List<OpenALSource> soundSources = new ArrayList<>();
    private final List<Integer> pausedSounds = new ArrayList<>();
    private final List<OpenALSource> ambienceSources = new ArrayList<>();
    private final List<Integer> ambiences = new ArrayList<>();

    private int currentSong;
    private float musicVolume = 0.2f;
    private float sfxVolume = 0.2f;
    private boolean songMute, soundMute;
    private final Random rand = new Random();

    public OpenAL() {
        initAL();
        loadSongs();
        loadSounds();
        loadAmbiences();
        setListenerData();
        Logger.getInstance().notify("Audio loaded successfully!", Message.INFORMATION);
    }

    private void initAL() {
        try {
            AL.create();
        }
        catch (Exception ignored) {}
    }

    // Data
    private void loadAudio(ValueEnum<String>[] audioArray, List<Integer> buffers, List<OpenALSource> sources) {
        Arrays.stream(audioArray).forEach(audio -> {
            String id = audio.getValue();
            buffers.add(loadBuffers("audio/" + id + ".wav"));
            sources.add(new OpenALSource());
        });
    }

    private void loadSongs() {
        Song[] songArray = Song.values();
        loadAudio(songArray, songs, songSources);
        updateSongVolume();
    }

    private void loadSounds() {
        Sound[] soundArray = Sound.values();
        loadAudio(soundArray, sounds, soundSources);
        updateSoundVolume();
    }

    private void loadAmbiences() {
        Ambience[] ambienceArray = Ambience.values();
        loadAudio(ambienceArray, ambiences, ambienceSources);
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
    public void playSong(Song song) {
        stopSong();
        currentSong = song.ordinal();
        if (!songMute) setMusicVolume(musicVolume);
        songSources.get(currentSong).play(songs.get(currentSong));
        songSources.get(currentSong).loop(true);

        if (song == Song.FOREST_1) playAmbience(Ambience.FOREST);
        else stopAmbience();
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
    public void playSound(Sound sound) {
        soundSources.get(sound.ordinal()).play(sounds.get(sound.ordinal()));
    }

    @Override
    public void stopSound(Sound sound) {
        soundSources.get(sound.ordinal()).stop();
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
    public void playAmbience(Ambience ambience) {
        int index = ambience.ordinal();
        if (!soundMute) soundSources.get(index).changeVolume(sfxVolume);
        ambienceSources.get(index).play(ambiences.get(index));
        ambienceSources.get(index).loop(true);
    }

    @Override
    public void stopAmbience() {
        ambienceSources.forEach(OpenALSource::stop);
    }

    @Override
    public void setLevelSong() {
        playSong(Song.FOREST_1);
    }

    @Override
    public void playSlashSound() {
        int start = 0;
        start += rand.nextInt(3);
        playSound(Sound.values()[start]);
    }

    @Override
    public void playHitSound() {
        int start = 3;
        start += rand.nextInt(2);
        playSound(Sound.values()[start]);
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
        playSound(Sound.values()[start]);
    }

    @Override
    public void playCrateSound() {
        int start = 6;
        start += rand.nextInt(2);
        playSound(Sound.values()[start]);
    }

    // Mute
    private void muteSource(List<Integer> buffers, List<OpenALSource> sources, boolean isMute) {
        for (Integer b : buffers) {
            if (isMute) sources.get(buffers.indexOf(b)).changeVolume(0);
            else sources.get(buffers.indexOf(b)).changeVolume(musicVolume);
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
        muteSource(ambiences, ambienceSources, soundMute);
    }

    // Volume
    private void updateSongVolume() {
        if (songMute) return;
        songSources.get(currentSong).changeVolume(musicVolume);
    }

    private void updateSoundVolume() {
        if (soundMute) return;
        sounds.forEach(sound -> soundSources.get(sounds.indexOf(sound))
                .changeVolume(sfxVolume));
        ambiences.forEach(ambience -> ambienceSources.get(ambiences.indexOf(ambience))
                .changeVolume(sfxVolume));
    }

    @Override
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        updateSongVolume();
    }

    @Override
    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
        updateSoundVolume();
    }

    @Override
    public void destroy() {
        songs.forEach(AL10::alDeleteBuffers);
        sounds.forEach(AL10::alDeleteBuffers);
        ambiences.forEach(AL10::alDeleteBuffers);
        AL.destroy();
    }

}
