package platformer.audio;

/**
 * A simple data class to hold the properties of a loaded audio file, necessary for calculating playback offsets.
 */
public class AudioFileProperties {

    public final int sampleRate;
    public final int channels;
    public final int bitsPerSample;

    public AudioFileProperties(int sampleRate, int channels, int bitsPerSample) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
    }
}