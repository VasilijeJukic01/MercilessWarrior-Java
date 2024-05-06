package platformer.audio;

/**
 * Singleton class that is responsible for creating an instance of the AudioPlayer.
 * The AudioPlayer is responsible for playing the audio in the game.
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

    private void init() {
        this.audioPlayer = new OpenAL();
    }


    public AudioPlayer<Song, Sound, Ambience>  getAudioPlayer() {
        return audioPlayer;
    }
}
