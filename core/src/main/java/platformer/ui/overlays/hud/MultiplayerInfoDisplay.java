package platformer.ui.overlays.hud;

import java.awt.*;
import static platformer.constants.Constants.*;

public class MultiplayerInfoDisplay {

    private String sessionId = null;
    private String status = "Connecting...";
    private long ping = -1;
    
    public void render(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.setColor(new Color(255, 255, 255, 200));

        int x = (int)(200 * SCALE);
        int y = (int)(10 * SCALE);
        
        if (sessionId == null) g.drawString(status, x, y);
        else {
            g.drawString("Session ID: " + sessionId, x, y);
            y += (int)(10 * SCALE);
            g.drawString("Ping: " + (ping > 0 ? ping + "ms" : "N/A"), x, y);
        }
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        this.status = "Connected!";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }
}