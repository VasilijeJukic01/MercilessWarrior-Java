package platformer.service.multiplayer.requests;

public class PingDTO extends MultiplayerMessage {

    public long clientTime;

    public PingDTO(long clientTime) {
        this.type = "PING";
        this.clientTime = clientTime;
    }
}