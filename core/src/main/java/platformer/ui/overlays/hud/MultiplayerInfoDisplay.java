package platformer.ui.overlays.hud;

import java.awt.*;
import static platformer.constants.Constants.*;

public class MultiplayerInfoDisplay {

    private String sessionId = null;
    private String status = "Connecting...";
    
    public void render(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.setColor(new Color(255, 255, 255, 200));

        int x = (int)(200 * SCALE);
        int y = (int)(20 * SCALE);
        
        if (sessionId == null) g.drawString(status, x, y);
        else {
            g.drawString("Session ID: " + sessionId, x, y);
        }
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        this.status = "Connected!";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}