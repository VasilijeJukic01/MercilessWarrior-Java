package platformer.service.multiplayer.requests;

public class PongDTO extends MultiplayerMessage {

    public long clientTime;

    public PongDTO(long clientTime) {
        this.type = "PONG";
        this.clientTime = clientTime;
    }

}