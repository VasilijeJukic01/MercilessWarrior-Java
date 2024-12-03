package platformer.audio;

import org.lwjgl.openal.AL10;

import java.util.Random;

import static org.lwjgl.openal.AL10.*;

/**
 * Represents an OpenAL source.
 * <p>
 * A source is an object that plays a sound. It can be paused, stopped, and have its volume changed.
 */
public class OpenALSource {

    private final int sourceID;
    private final Random random;

    public OpenALSource() {
        this.sourceID = AL10.alGenSources();
        this.random = new Random();
        AL10.alSourcef(sourceID, AL10.AL_GAIN, 1);
        AL10.alSourcef(sourceID, AL10.AL_PITCH, 1);
        AL10.alSource3f(sourceID, AL10.AL_POSITION, 0, 0, 0);
    }

    // Operations
    public void play(int buffer, boolean varyPitch) {
        AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
        // Audio Fatigue
        if (varyPitch) {
            float pitch = 0.95f + (random.nextFloat() * 0.1f);
            AL10.alSourcef(sourceID, AL10.AL_PITCH, pitch);
        }
        else AL10.alSourcef(sourceID, AL10.AL_PITCH, 1.0f);
        AL10.alSourcePlay(sourceID);
    }

    public void pause() {
        AL10.alSourcePause(sourceID);
    }

    public void unpause() {
        AL10.alSourcePlay(sourceID);
    }

    public void stop() {
        if (isPlaying()) AL10.alSourceStop(sourceID);
    }

    public void loop(boolean loop) {
        AL10.alSourcei(sourceID, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    public void changeVolume(float volume) {
        AL10.alSourcef(sourceID, AL10.AL_GAIN, volume);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(sourceID, AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void delete() {
        AL10.alDeleteSources(sourceID);
    }

}
