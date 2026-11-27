package platformer.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryStack;
import platformer.audio.types.Ambience;
import platformer.audio.types.Song;
import platformer.audio.types.Sound;
import platformer.core.Settings;
import platformer.core.loading.LoadingProgressTracker;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.utils.ValueEnum;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * OpenAL implementation of {@link AudioPlayer} backed by EnumMap buffer tables and a fixed-size SFX source pool.
 */
public class OpenAL implements AudioPlayer<Song, Sound, Ambience> {

    private static final int SFX_POOL_SIZE = 28;

    private final Map<Integer, AudioFileProperties> songProperties = new HashMap<>();
    private final Map<Song, Integer> songBuffers = new EnumMap<>(Song.class);
    private final Map<Sound, Integer> soundBuffers = new EnumMap<>(Sound.class);
    private final Map<Ambience, Integer> ambienceBuffers = new EnumMap<>(Ambience.class);

    private OpenALSource songSource;
    private OpenALSource ambienceSource;
    private final OpenALSource[] sfxPool = new OpenALSource[SFX_POOL_SIZE];
    private final Sound[] sfxPoolTag = new Sound[SFX_POOL_SIZE];
    private final boolean[] pausedPoolSlots = new boolean[SFX_POOL_SIZE];
    private int nextPoolIndex = 0;

    private Song currentSong = null;
    private float musicVolume = Settings.getInstance().getMusicVolume();
    private float sfxVolume = Settings.getInstance().getSfxVolume();
    private boolean songMute = Settings.getInstance().isMusicMute();
    private boolean soundMute = Settings.getInstance().isSfxMute();
    private final Random rand = new Random();
    private final LoadingProgressTracker progressTracker = LoadingProgressTracker.getInstance();

    // Loading progress splits
    private static final double INIT_PROGRESS = 0.1;
    private static final double SONGS_PROGRESS = 0.4;
    private static final double SOUNDS_PROGRESS = 0.3;
    private static final double AMBIENCE_PROGRESS = 0.2;

    private static final Sound[] SLASH_SOUNDS = { Sound.SLASH_1, Sound.SLASH_2, Sound.SLASH_3 };
    private static final Sound[] HIT_SOUNDS = { Sound.ENEMY_HIT_1, Sound.ENEMY_HIT_2 };
    private static final Sound[] ENEMY_BLOCK_SOUNDS = { Sound.ENEMY_BLOCK_1, Sound.ENEMY_BLOCK_2 };
    private static final Sound[] PLAYER_BLOCK_SOUNDS = { Sound.SWORD_BLOCK_1, Sound.SWORD_BLOCK_2, Sound.SWORD_BLOCK_3 };
    private static final Sound[] CRATE_BREAK_SOUNDS = { Sound.CRATE_BREAK_1, Sound.CRATE_BREAK_2 };

    /**
     * Initializes the OpenAL audio system. This involves setting up the audio device and context,
     * and loading all songs, sound effects, and ambient sounds from files into OpenAL buffers.
     */
    public OpenAL() {
        progressTracker.updateStatus("Initializing audio system");
        progressTracker.updateProgress(0.01);

        initAL();
        initSources();
        progressTracker.updateProgress(INIT_PROGRESS);

        progressTracker.updateStatus("Loading music");
        loadAudio(Song.values(), songBuffers, songProperties, INIT_PROGRESS, SONGS_PROGRESS);
        updateSongVolume();
        progressTracker.updateProgress(INIT_PROGRESS + SONGS_PROGRESS);

        progressTracker.updateStatus("Loading sound effects");
        loadAudio(Sound.values(), soundBuffers, null, INIT_PROGRESS + SONGS_PROGRESS, SOUNDS_PROGRESS);
        updateSoundVolume();
        progressTracker.updateProgress(INIT_PROGRESS + SONGS_PROGRESS + SOUNDS_PROGRESS);

        progressTracker.updateStatus("Loading ambient sounds");
        loadAudio(Ambience.values(), ambienceBuffers, null, INIT_PROGRESS + SONGS_PROGRESS + SOUNDS_PROGRESS, AMBIENCE_PROGRESS);
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

    private void initSources() {
        this.songSource = new OpenALSource();
        this.ambienceSource = new OpenALSource();
        for (int i = 0; i < SFX_POOL_SIZE; i++) {
            sfxPool[i] = new OpenALSource();
        }
    }

    private <E extends Enum<E> & ValueEnum<String>> void loadAudio(E[] audioArray, Map<E, Integer> targetMap, Map<Integer, AudioFileProperties> propertiesMap, double progressStart, double progressTotal) {
        if (audioArray.length == 0) return;
        double progressPerFile = progressTotal / audioArray.length;
        double currentProgress = progressStart;

        for (E audio : audioArray) {
            int bufferId = loadBuffer("audio/" + audio.getValue() + ".wav", propertiesMap);
            if (bufferId != -1) {
                targetMap.put(audio, bufferId);
            }
            currentProgress += progressPerFile;
            progressTracker.updateProgress(currentProgress);
        }
    }

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
            try (WaveData waveFile = WaveData.create("/" + file)) {
                AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
                if (propertiesMap != null) {
                    AudioFormat format = waveFile.getAudioFormat();
                    propertiesMap.put(buffer, new AudioFileProperties(waveFile.samplerate, format.getChannels(), 16));
                }
            }
            catch (Exception e) {
                AL10.alDeleteBuffers(buffer);
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
    public synchronized void playSong(Song song) {
        playSong(song, 0);
    }

    @Override
    public synchronized void playSong(Song song, int offsetMs) {
        stopSong();
        if (song == null) return;
        Integer bufferId = songBuffers.get(song);
        if (bufferId == null) return;

        currentSong = song;
        updateSongVolume();

        AudioFileProperties props = songProperties.get(bufferId);
        int byteOffset = 0;
        if (props != null && offsetMs > 0) {
            double bytesPerSecond = props.sampleRate * props.channels * (props.bitsPerSample / 8.0);
            byteOffset = (int) (bytesPerSecond * (offsetMs / 1000.0));
        }

        songSource.play(bufferId, false, byteOffset);
        songSource.loop(true);

        if (song == Song.FOREST_1) playAmbience(Ambience.FOREST);
        else stopAmbience();
    }

    @Override
    public synchronized Song getCurrentSong() {
        if (currentSong != null && songSource.isPlaying()) {
            return currentSong;
        }
        return null;
    }

    @Override
    public synchronized void pauseSong() {
        if (currentSong != null) songSource.pause();
    }

    @Override
    public synchronized void unpauseSong() {
        if (currentSong != null) songSource.unpause();
    }

    @Override
    public synchronized void stopSong() {
        songSource.stop();
        currentSong = null;
    }

    @Override
    public synchronized void playSound(Sound sound) {
        if (sound == null) return;
        Integer bufferId = soundBuffers.get(sound);
        if (bufferId == null) return;

        int slot = acquireSfxSlot();
        float vol = soundMute ? 0f : sfxVolume;
        sfxPoolTag[slot] = sound;
        pausedPoolSlots[slot] = false;
        sfxPool[slot].changeVolume(vol);
        sfxPool[slot].play(bufferId, true);
    }

    private int acquireSfxSlot() {
        for (int i = 0; i < SFX_POOL_SIZE; i++) {
            int idx = (nextPoolIndex + i) % SFX_POOL_SIZE;
            if (!sfxPool[idx].isPlaying() && !pausedPoolSlots[idx]) {
                nextPoolIndex = (idx + 1) % SFX_POOL_SIZE;
                return idx;
            }
        }
        // All voices busy: steal oldest slot in round-robin order
        int stolen = nextPoolIndex;
        nextPoolIndex = (nextPoolIndex + 1) % SFX_POOL_SIZE;
        return stolen;
    }

    @Override
    public synchronized void stopSound(Sound sound) {
        if (sound == null) return;
        for (int i = 0; i < SFX_POOL_SIZE; i++) {
            if (sfxPoolTag[i] == sound) {
                sfxPool[i].stop();
                sfxPoolTag[i] = null;
                pausedPoolSlots[i] = false;
            }
        }
    }

    @Override
    public synchronized void pauseSounds() {
        for (int i = 0; i < SFX_POOL_SIZE; i++) {
            if (sfxPool[i].isPlaying()) {
                sfxPool[i].pause();
                pausedPoolSlots[i] = true;
            }
        }
    }

    @Override
    public synchronized void unpauseSounds() {
        for (int i = 0; i < SFX_POOL_SIZE; i++) {
            if (pausedPoolSlots[i]) {
                sfxPool[i].unpause();
                pausedPoolSlots[i] = false;
            }
        }
    }

    @Override
    public synchronized void playAmbience(Ambience ambience) {
        if (ambience == null) {
            stopAmbience();
            return;
        }
        Integer bufferId = ambienceBuffers.get(ambience);
        if (bufferId == null) return;

        float vol = soundMute ? 0f : sfxVolume;
        ambienceSource.changeVolume(vol);
        ambienceSource.play(bufferId, false);
        ambienceSource.loop(true);
    }

    @Override
    public synchronized void stopAmbience() {
        ambienceSource.stop();
    }

    @Override
    public void setLevelSong() {
        playSong(Song.FOREST_1);
    }

    @Override
    public void playSlashSound() {
        playSound(SLASH_SOUNDS[rand.nextInt(SLASH_SOUNDS.length)]);
    }

    @Override
    public void playHitSound() {
        playSound(HIT_SOUNDS[rand.nextInt(HIT_SOUNDS.length)]);
    }

    @Override
    public void playBlockSound(String type) {
        if ("Enemy".equals(type)) {
            playSound(ENEMY_BLOCK_SOUNDS[rand.nextInt(ENEMY_BLOCK_SOUNDS.length)]);
        }
        else if ("Player".equals(type)) {
            playSound(PLAYER_BLOCK_SOUNDS[rand.nextInt(PLAYER_BLOCK_SOUNDS.length)]);
        }
    }

    @Override
    public void playCrateSound() {
        playSound(CRATE_BREAK_SOUNDS[rand.nextInt(CRATE_BREAK_SOUNDS.length)]);
    }

    // Volume
    private void updateSongVolume() {
        float actualVolume = songMute ? 0f : musicVolume;
        songSource.changeVolume(actualVolume);
    }

    private void updateSoundVolume() {
        float actualVolume = soundMute ? 0f : sfxVolume;
        for (OpenALSource source : sfxPool) source.changeVolume(actualVolume);
        ambienceSource.changeVolume(actualVolume);
    }

    @Override
    public synchronized void songMute() {
        songMute = !songMute;
        updateSongVolume();
        Settings.getInstance().setMusicMute(songMute);
        Settings.getInstance().save();
    }

    @Override
    public synchronized void soundMute() {
        soundMute = !soundMute;
        updateSoundVolume();
        Settings.getInstance().setSfxMute(soundMute);
        Settings.getInstance().save();
    }

    @Override
    public synchronized void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        updateSongVolume();
        Settings.getInstance().setMusicVolume(musicVolume);
    }

    @Override
    public synchronized void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
        updateSoundVolume();
        Settings.getInstance().setSfxVolume(sfxVolume);
    }

    @Override
    public synchronized float getMusicVolume() {
        return musicVolume;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation deletes all OpenAL buffers and sources, then properly closes the OpenAL device and context to prevent resource leaks.
     */
    @Override
    public synchronized void destroy() {
        songSource.delete();
        ambienceSource.delete();
        for (OpenALSource source : sfxPool) source.delete();

        songBuffers.values().forEach(AL10::alDeleteBuffers);
        soundBuffers.values().forEach(AL10::alDeleteBuffers);
        ambienceBuffers.values().forEach(AL10::alDeleteBuffers);

        long context = ALC10.alcGetCurrentContext();
        long device = ALC10.alcGetContextsDevice(context);

        ALC10.alcMakeContextCurrent(NULL);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}