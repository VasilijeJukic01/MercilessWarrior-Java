package platformer.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryStack;
import platformer.audio.types.Ambience;
import platformer.audio.types.Song;
import platformer.audio.types.Sound;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.core.loading.LoadingProgressTracker;
import platformer.utils.ValueEnum;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * An implementation of the {@link AudioPlayer} interface using the OpenAL library (via LWJGL).
 * This class is responsible for initializing the audio device, loading all sound files into buffers,
 * managing sound sources, and handling playback logic.
 */
public class OpenAL implements AudioPlayer<Song, Sound, Ambience>  {

    private final Map<Integer, AudioFileProperties> songProperties = new HashMap<>();

    private final List<Integer> songs = new ArrayList<>();
    private final List<OpenALSource> songSources = new ArrayList<>();
    private final List<Integer> sounds = new ArrayList<>();
    private final List<OpenALSource> soundSources = new ArrayList<>();
    private final List<Integer> pausedSounds = new ArrayList<>();
    private final List<OpenALSource> ambienceSources = new ArrayList<>();
    private final List<Integer> ambiences = new ArrayList<>();

    private int currentSong = -1;
    private float musicVolume = 0.2f;
    private float sfxVolume = 0.2f;
    private boolean songMute, soundMute;
    private final Random rand = new Random();
    private final LoadingProgressTracker progressTracker = LoadingProgressTracker.getInstance();

    // Loading
    private static final double INIT_PROGRESS = 0.1;
    private static final double SONGS_PROGRESS = 0.4;
    private static final double SOUNDS_PROGRESS = 0.3;
    private static final double AMBIENCE_PROGRESS = 0.2;

    /**
     * Initializes the OpenAL audio system. This involves setting up the audio device and context,
     * and loading all songs, sound effects, and ambient sounds from files into OpenAL buffers.
     */
    public OpenAL() {
        progressTracker.updateStatus("Initializing audio system");
        progressTracker.updateProgress(0.01);

        initAL();
        progressTracker.updateProgress(INIT_PROGRESS);
        progressTracker.updateStatus("Loading music");

        loadSongs();
        progressTracker.updateProgress(INIT_PROGRESS + SONGS_PROGRESS);
        progressTracker.updateStatus("Loading sound effects");

        loadSounds();
        progressTracker.updateProgress(INIT_PROGRESS + SONGS_PROGRESS + SOUNDS_PROGRESS);
        progressTracker.updateStatus("Loading ambient sounds");

        loadAmbiences();
        setListenerData();

        progressTracker.updateProgress(INIT_PROGRESS + SONGS_PROGRESS + SOUNDS_PROGRESS + AMBIENCE_PROGRESS);
        progressTracker.updateStatus("Audio loaded successfully!");
        Logger.getInstance().notify("Audio loaded successfully!", Message.INFORMATION);
    }

    /**
     * Initializes the OpenAL device and creates a context.
     * @throws IllegalStateException if the audio device or context cannot be created.
     */
    private void initAL() {
        long device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer contextAttribList = stack.mallocInt(1).put(0).flip();
            long context = ALC10.alcCreateContext(device, contextAttribList);
            if (context == NULL) throw new IllegalStateException("Failed to create OpenAL context.");

            ALC10.alcMakeContextCurrent(context);
            AL.createCapabilities(ALC.createCapabilities(device));
        }
    }

    // Data
    private void loadAudio(ValueEnum<String>[] audioArray, List<Integer> buffers, List<OpenALSource> sources, Map<Integer, AudioFileProperties> propertiesMap, double progressStart, double progressTotal) {
        int totalFiles = audioArray.length;
        double progressPerFile = progressTotal / totalFiles;
        double currentProgress = progressStart;

        for (ValueEnum<String> audio : audioArray) {
            String id = audio.getValue();

            int bufferId = loadBuffer("audio/" + id + ".wav", propertiesMap);
            if (bufferId != -1) {
                buffers.add(bufferId);
                sources.add(new OpenALSource());
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            currentProgress += progressPerFile;
            progressTracker.updateProgress(currentProgress);

            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void loadSongs() {
        Song[] songArray = Song.values();
        loadAudio(songArray, songs, songSources, songProperties, INIT_PROGRESS, SONGS_PROGRESS);
        updateSongVolume();
    }

    private void loadSounds() {
        Sound[] soundArray = Sound.values();
        loadAudio(soundArray, sounds, soundSources,  null, INIT_PROGRESS + SONGS_PROGRESS, SOUNDS_PROGRESS);
        updateSoundVolume();
    }

    private void loadAmbiences() {
        Ambience[] ambienceArray = Ambience.values();
        loadAudio(ambienceArray, ambiences, ambienceSources, null, INIT_PROGRESS + SONGS_PROGRESS + SOUNDS_PROGRESS, AMBIENCE_PROGRESS);
    }

    // Core
    /**
     * Loads a single audio file (must be .wav) into an OpenAL buffer.
     *
     * @param file The path to the audio file within the resources.
     * @param propertiesMap A map to store the audio properties for this buffer, or null if properties are not needed.
     * @return The integer ID of the generated OpenAL buffer, or -1 on failure.
     */
    private int loadBuffer(String file, Map<Integer, AudioFileProperties> propertiesMap) {
        int buffer = AL10.alGenBuffers();
        if (file.endsWith(".wav")) {
            try (WaveData waveFile = WaveData.create(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).getFile())) {
                AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
                if (propertiesMap != null) {
                    AudioFormat format = waveFile.getAudioFormat();
                    propertiesMap.put(buffer, new AudioFileProperties(waveFile.samplerate, format.getChannels(), 16));
                }
            }
            catch (Exception e) {
                Logger.getInstance().notify("Could not load audio file: " + file, Message.ERROR);
                return -1;
            }
        }
        return buffer;
    }

    /**
     * Sets the listener's position and velocity to default values at the center of the world.
     */
    public void setListenerData() {
        AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
        AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
    }

    // Mediator
    @Override
    public void playSong(Song song) {
        playSong(song, 0);
    }

    @Override
    public void playSong(Song song, int offsetMs) {
        stopSong();
        currentSong = song.ordinal();
        if (!songMute) setMusicVolume(musicVolume);

        int bufferId = songs.get(currentSong);
        AudioFileProperties props = songProperties.get(bufferId);
        int byteOffset = 0;
        if (props != null && offsetMs > 0) {
            double bytesPerSecond = props.sampleRate * props.channels * (props.bitsPerSample / 8.0);
            byteOffset = (int) (bytesPerSecond * (offsetMs / 1000.0));
        }

        songSources.get(currentSong).play(bufferId, false, byteOffset);
        songSources.get(currentSong).loop(true);

        if (song == Song.FOREST_1) playAmbience(Ambience.FOREST);
        else stopAmbience();
    }

    @Override
    public Song getCurrentSong() {
        if (currentSong != -1 && songSources.get(currentSong).isPlaying()) {
            return Song.values()[currentSong];
        }
        return null;
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
        if (currentSong != -1 && currentSong < songSources.size()) {
            songSources.get(currentSong).stop();
        }
        currentSong = -1;
    }

    @Override
    public void playSound(Sound sound) {
        soundSources.get(sound.ordinal()).play(sounds.get(sound.ordinal()), true);
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
        ambienceSources.get(index).play(ambiences.get(index), false);
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
        if (songMute || currentSong == -1) return;
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation deletes all OpenAL buffers and sources, then properly closes the
     * OpenAL device and context to prevent resource leaks.
     */
    @Override
    public void destroy() {
        songs.forEach(AL10::alDeleteBuffers);
        sounds.forEach(AL10::alDeleteBuffers);
        ambiences.forEach(AL10::alDeleteBuffers);

        long context = ALC10.alcGetCurrentContext();
        long device = ALC10.alcGetContextsDevice(context);

        ALC10.alcMakeContextCurrent(NULL);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }

}
