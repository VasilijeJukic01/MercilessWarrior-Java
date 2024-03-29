package platformer.audio;

public class Audio {

    private static volatile Audio instance = null;

    private AudioPlayer<Song, Sound> audioPlayer;

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


    public AudioPlayer<Song, Sound>  getAudioPlayer() {
        return audioPlayer;
    }
}
