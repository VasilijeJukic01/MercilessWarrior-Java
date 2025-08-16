package platformer.service.multiplayer.requests;

public class PlayerStateDTO extends MultiplayerMessage {
    public String clientId;
    public String username;
    public double x, y;
    public int levelI, levelJ;
    public int animState;
    public int animIndex;
    public int flipSign;
    public int flipCoefficient;
    public boolean isTransformed;

    public PlayerStateDTO() {
        this.type = "PLAYER_STATE";
    }

    public PlayerStateDTO(String clientId, String username, double x, double y, int levelI, int levelJ, int animState, int animIndex, int flipSign, int flipCoefficient, boolean isTransformed) {
        this.type = "PLAYER_STATE";
        this.clientId = clientId;
        this.username = username;
        this.x = x;
        this.y = y;
        this.levelI = levelI;
        this.levelJ = levelJ;
        this.animState = animState;
        this.animIndex = animIndex;
        this.flipSign = flipSign;
        this.flipCoefficient = flipCoefficient;
        this.isTransformed = isTransformed;
    }


}