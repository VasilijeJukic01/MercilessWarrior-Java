package platformer.audio;

import platformer.audio.types.Ambience;
import platformer.audio.types.Song;
import platformer.audio.types.Sound;
import platformer.event.EventBus;
import platformer.event.events.ui.GamePausedEvent;
import platformer.event.events.ui.GameResumedEvent;

/**
 * The main public access point for the game's audio system.
 * This class follows the Singleton pattern to provide a single, globally accessible instance of the {@link AudioPlayer}.
 * Other parts of the game should interact with this class to play sounds, music, and ambient tracks.
 */
public class Audio {

    private static volatile Audio instance = null;

    private AudioPlayer<Song, Sound, Ambience> audioPlayer;

    private Audio() {
        init();
    }

    public static Audio getInstance() {
        if (instance == null) {
            synchronized (Audio.class) {
                if (instance == null) {
                    instance = new Audio();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the audio player implementation.
     */
    private void init() {
        this.audioPlayer = new OpenAL();
        EventBus.getInstance().register(GamePausedEvent.class, e -> onGamePaused());
        EventBus.getInstance().register(GameResumedEvent.class, e -> onGameResumed());
    }

    private void onGamePaused() {
        audioPlayer.pauseSong();
        audioPlayer.pauseSounds();
    }

    private void onGameResumed() {
        audioPlayer.unpauseSong();
        audioPlayer.unpauseSounds();
    }


    public AudioPlayer<Song, Sound, Ambience>  getAudioPlayer() {
        return audioPlayer;
    }
}
