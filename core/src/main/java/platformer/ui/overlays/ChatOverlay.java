package platformer.ui.overlays;

import platformer.event.EventBus;
import platformer.event.events.multiplayer.ChatMessageReceivedEvent;
import platformer.state.types.GameState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import static platformer.constants.Constants.*;

public class ChatOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    private final List<String> history = new LinkedList<>();
    private String currentInput = "";
    private boolean active = false;

    private static final int MAX_HISTORY_LINES = 50;
    private static final int VISIBLE_HISTORY_LINES = 6;
    private double scrollOffset = 0.0;

    // Components
    private final Rectangle2D.Double chatBox, inputBox;
    private final Rectangle2D.Double scrollBarTrack, scrollBarThumb;
    private boolean isDraggingScrollBar = false;
    private int scrollDragStartY;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ChatOverlay(GameState gameState) {
        this.gameState = gameState;
        int chatWidth = (int)(200 * SCALE);
        int chatHeight = (int)(70 * SCALE);
        int chatX = (int)(625 * SCALE);
        int chatY = GAME_HEIGHT - chatHeight - (int)(25 * SCALE);
        int inputHeight = (int)(15 * SCALE);
        this.chatBox = new Rectangle2D.Double(chatX, chatY, chatWidth, chatHeight);
        this.inputBox = new Rectangle2D.Double(chatX, chatY + chatHeight + (int)(3 * SCALE), chatWidth, inputHeight);

        int scrollBarWidth = (int)(10 * SCALE);
        this.scrollBarTrack = new Rectangle2D.Double(chatBox.x + chatBox.width - scrollBarWidth, chatBox.y, scrollBarWidth, chatBox.height);
        this.scrollBarThumb = new Rectangle2D.Double();

        EventBus.getInstance().register(ChatMessageReceivedEvent.class, this::onChatMessageReceived);
    }

    private void onChatMessageReceived(ChatMessageReceivedEvent event) {
        String timestamp = LocalTime.now().format(timeFormatter);
        String formattedMessage = String.format("[%s] %s: %s", timestamp, event.username(), event.message());
        history.add(formattedMessage);
        if (history.size() > MAX_HISTORY_LINES) history.remove(0);
        // Scroll to the bottom when a new message arrives
        if (scrollOffset == 0) scrollToBottom();
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // History Box
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fill(chatBox);

        // Input Box
        g2d.setColor(new Color(0, 0, 0, active ? 180 : 100));
        g2d.fill(inputBox);
        g2d.setColor(new Color(255, 255, 255, active ? 150 : 100));
        g2d.draw(inputBox);

        renderChatHistory(g2d);
        renderScrollBar(g2d);
        g.setColor(new Color(255, 255, 255, 100));
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        String textToRender = active ? currentInput + "_" : "> Press 'T' to chat";
        g.drawString(textToRender, (int)inputBox.x + 5, (int)(inputBox.y + inputBox.height - 4*SCALE));

        g2d.dispose();
    }

    private void renderChatHistory(Graphics2D g2d) {
        g2d.setClip(chatBox);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int intScrollOffset = (int)Math.round(this.scrollOffset);
        int startIdx = Math.max(0, history.size() - VISIBLE_HISTORY_LINES - intScrollOffset);
        int endIdx = history.size() - intScrollOffset;
        int y = (int) (chatBox.y + chatBox.height - fm.getDescent());
        for (int i = endIdx - 1; i >= startIdx; i--) {
            g2d.drawString(history.get(i), (int) chatBox.x + 5, y);
            y -= lineHeight;
        }
        g2d.setClip(null);
    }
    
    private void renderScrollBar(Graphics2D g2d) {
        if (history.size() <= VISIBLE_HISTORY_LINES) return;
        g2d.setColor(new Color(50, 50, 50, 180));
        g2d.fill(scrollBarTrack);

        int totalLines = history.size();
        double thumbHeightRatio = (double) VISIBLE_HISTORY_LINES / totalLines;
        double thumbHeight = Math.max(10 * SCALE, scrollBarTrack.height * thumbHeightRatio);
        double scrollRatio = scrollOffset / (totalLines - VISIBLE_HISTORY_LINES);
        double thumbY = scrollBarTrack.y + (scrollBarTrack.height - thumbHeight) * scrollRatio;
        scrollBarThumb.setRect(scrollBarTrack.x, thumbY, scrollBarTrack.width, thumbHeight);

        g2d.setColor(new Color(120, 120, 120, 220));
        g2d.fill(scrollBarThumb);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!active) return;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            }
        }
        else {
            char c = e.getKeyChar();
            if (Character.isDefined(c) && c >= ' ' && c != '`') currentInput += c;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!active) return;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMessage();
        }
    }

    private void sendMessage() {
        if (!currentInput.trim().isEmpty()) {
            gameState.getMultiplayerHandler().sendChatMessage(currentInput);
        }
        currentInput = "";
        toggle();
    }

    public void toggle() {
        this.active = !this.active;
        if (!active) currentInput = "";
    }
    
    public boolean isActive() {
        return this.active;
    }

    private void scrollToBottom() {
        scrollOffset = 0;
    }

    private void scrollUp() {
        scrollOffset = Math.min(history.size() - VISIBLE_HISTORY_LINES, scrollOffset + 1);
    }

    private void scrollDown() {
        scrollOffset = Math.max(0, scrollOffset - 1);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (scrollBarThumb.contains(e.getPoint())) {
            isDraggingScrollBar = true;
            scrollDragStartY = e.getY();
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        isDraggingScrollBar = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDraggingScrollBar) {
            int dy = e.getY() - scrollDragStartY;
            scrollDragStartY = e.getY();
            int totalLines = history.size();
            if (totalLines <= VISIBLE_HISTORY_LINES) return;

            double scrollableHeight = scrollBarTrack.height - scrollBarThumb.height;
            if (scrollableHeight <= 0) return;

            double scrollDelta = (double)dy / scrollableHeight * (double)(totalLines - VISIBLE_HISTORY_LINES);

            scrollOffset += scrollDelta;
            scrollOffset = Math.max(0, Math.min(totalLines - VISIBLE_HISTORY_LINES, scrollOffset));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void reset() {
        history.clear();
        currentInput = "";
        active = false;
        scrollOffset = 0;
    }
}