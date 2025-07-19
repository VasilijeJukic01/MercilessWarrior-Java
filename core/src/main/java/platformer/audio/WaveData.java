package platformer.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A utility class to load and hold audio data from a .wav file.
 * The data is stored in a format suitable for use with OpenAL.
 * This class implements {@link AutoCloseable} to allow for easy resource management using try-with-resources statements.
 */
public class WaveData implements AutoCloseable {

    public final int format;
    public final int samplerate;
    public final ByteBuffer data;

    private WaveData(int format, int samplerate, ByteBuffer data) {
        this.format = format;
        this.samplerate = samplerate;
        this.data = data;
    }

    /**
     * Creates a WaveData object by loading and decoding a .wav file.
     *
     * @param file The path to the .wav file.
     * @return A new WaveData object containing the audio information.
     * @throws RuntimeException if the WAV file fails to load or is in an unsupported format.
     */
    public static WaveData create(String file) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(file));
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodeFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(), false);
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);

            ByteBuffer buffer = BufferUtils.createByteBuffer(dais.available());
            byte[] bytes = new byte[dais.available()];
            dais.read(bytes);
            buffer.put(bytes);
            buffer.flip();

            int format = baseFormat.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            return new WaveData(format, (int) baseFormat.getSampleRate(), buffer);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException("Failed to load WAV file: " + file, e);
        }
    }

    /**
     * Clears the ByteBuffer to help release its memory.
     */
    @Override
    public void close() {
        data.clear();
    }
}
