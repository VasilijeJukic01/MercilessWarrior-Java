package platformer.audio;

import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.AL10.*;

/**
 * Represents an OpenAL source.
 * <p>
 * A source is an object that plays a sound. It can be paused, stopped, and have its volume changed.
 */
public class OpenALSource {

    private final int sourceID;

    public OpenALSource() {
        this.sourceID = AL10.alGenSources();
        AL10.alSourcef(sourceID, AL10.AL_GAIN, 1);
        AL10.alSourcef(sourceID, AL10.AL_PITCH, 1);
        AL10.alSource3f(sourceID, AL10.AL_POSITION, 0, 0, 0);
    }

    // Operations
    public void play(int buffer) {
        AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
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
