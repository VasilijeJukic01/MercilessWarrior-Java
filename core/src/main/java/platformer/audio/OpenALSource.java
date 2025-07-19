package platformer.audio;

import org.lwjgl.openal.AL10;

import java.util.Random;

import static org.lwjgl.openal.AL10.*;

/**
 * Represents a wrapper around a single OpenAL sound source.
 * This class simplifies common operations on a source, such as playing, pausing, stopping, and adjusting volume and pitch.
 */
public class OpenALSource {

    private final int sourceID;
    private final Random random;

    /**
     * Generates a new OpenAL source and sets default properties like gain and pitch.
     */
    public OpenALSource() {
        this.sourceID = AL10.alGenSources();
        this.random = new Random();
        AL10.alSourcef(sourceID, AL10.AL_GAIN, 1);
        AL10.alSourcef(sourceID, AL10.AL_PITCH, 1);
        AL10.alSource3f(sourceID, AL10.AL_POSITION, 0, 0, 0);
    }

    // Operations
    /**
     * Plays an audio buffer on this source.
     *
     * @param buffer The ID of the OpenAL buffer to play.
     * @param varyPitch If true, a slight random pitch variation is applied to the sound to prevent audio fatigue from repetitive sound effects.
     */
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

    /**
     * Pauses playback on this source.
     */
    public void pause() {
        AL10.alSourcePause(sourceID);
    }

    /**
     * Resumes playback on this source if it was paused.
     */
    public void unpause() {
        AL10.alSourcePlay(sourceID);
    }

    /**
     * Stops playback on this source if it is currently playing.
     */
    public void stop() {
        if (isPlaying()) AL10.alSourceStop(sourceID);
    }

    /**
     * Sets whether the audio played on this source should loop.
     *
     * @param loop true to loop playback, false otherwise.
     */
    public void loop(boolean loop) {
        AL10.alSourcei(sourceID, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * Changes the volume (gain) of this source.
     *
     * @param volume The volume level, from 0.0f (silent) to 1.0f (full volume).
     */
    public void changeVolume(float volume) {
        AL10.alSourcef(sourceID, AL10.AL_GAIN, volume);
    }

    /**
     * Checks if this source is currently playing audio.
     *
     * @return true if the source state is AL_PLAYING, false otherwise.
     */
    public boolean isPlaying() {
        return AL10.alGetSourcei(sourceID, AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    /**
     * Deletes the OpenAL source to free up resources.
     */
    public void delete() {
        AL10.alDeleteSources(sourceID);
    }

}
