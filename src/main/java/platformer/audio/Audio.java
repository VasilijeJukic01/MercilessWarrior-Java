package platformer.audio;


public class Audio {

    public static Audio instance = null;

    private AudioPlayer audioPlayer;

    private Audio() {
        init();
    }

    private void init() {
        this.audioPlayer = new OpenAL();
    }

    public static Audio getInstance() {
        if (instance == null) {
            instance = new Audio();
        }
        return instance;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
}
