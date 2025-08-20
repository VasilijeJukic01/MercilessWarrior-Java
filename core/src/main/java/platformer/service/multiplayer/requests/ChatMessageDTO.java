package platformer.service.multiplayer.requests;

public class ChatMessageDTO extends MultiplayerMessage {

    public String username;
    public String content;
    public long timestamp;

    public ChatMessageDTO(String username, String content, long timestamp) {
        this.type = "CHAT_MESSAGE";
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
    }
}