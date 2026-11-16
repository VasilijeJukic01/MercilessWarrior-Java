package platformer.service.multiplayer.requests;

public class PlayerStateDTO extends MultiplayerMessage {
    public String clientId;
    public String username;
    public double x, y;
    public String levelId;
    public int animState;
    public int animIndex;
    public int flipSign;
    public int flipCoefficient;
    public boolean isTransformed;

    public PlayerStateDTO() {
        this.type = "PLAYER_STATE";
    }

    public PlayerStateDTO(String clientId, String username, double x, double y, String levelId, int animState, int animIndex, int flipSign, int flipCoefficient, boolean isTransformed) {
        this.type = "PLAYER_STATE";
        this.clientId = clientId;
        this.username = username;
        this.x = x;
        this.y = y;
        this.levelId = levelId;
        this.animState = animState;
        this.animIndex = animIndex;
        this.flipSign = flipSign;
        this.flipCoefficient = flipCoefficient;
        this.isTransformed = isTransformed;
    }
}