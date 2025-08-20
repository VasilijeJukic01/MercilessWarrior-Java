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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static platformer.constants.Constants.*;

public class ChatOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private record RenderLine(String timestampPart, String usernamePart, String contentPart, Color userColor) {}

    private static class FormattedChatMessage {
        private final String timestamp;
        private final String username;
        private final String content;
        private final Color userColor;
        List<String> wrappedContent;

        FormattedChatMessage(String username, String content, Color userColor) {
            this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            this.username = username;
            this.content = content;
            this.userColor = userColor;
        }
    }

    private final GameState gameState;

    private final List<FormattedChatMessage> history = new LinkedList<>();
    private int totalHistoryLines = 0;
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

    /**
     * Wraps a given string into multiple lines if it exceeds the max width.
     */
    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Case 1 - The word is longer than the line width
            if (fm.stringWidth(word) > maxWidth) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                StringBuilder longWordPart = new StringBuilder();
                for (char c : word.toCharArray()) {
                    if (fm.stringWidth(longWordPart.toString() + c) > maxWidth) {
                        lines.add(longWordPart.toString());
                        longWordPart = new StringBuilder();
                    }
                    longWordPart.append(c);
                }
                currentLine.append(longWordPart);
            }
            // Case 2 - Normal word wrapping
            else {
                if (!currentLine.isEmpty() && fm.stringWidth(currentLine + " " + word) > maxWidth) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    private void onChatMessageReceived(ChatMessageReceivedEvent event) {
        history.add(new FormattedChatMessage(event.username(), event.message(), event.userColor()));
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
        g2d.setColor(Color.WHITE);
        g2d.draw(inputBox);

        renderChatHistory(g2d);
        renderScrollBar(g2d);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        FontMetrics fm = g.getFontMetrics();
        String textToRender;
        if (active) {
            String fullInput = currentInput + "_";
            int inputWidth = fm.stringWidth(fullInput);
            int boxWidth = (int) inputBox.width - 10;
            if (inputWidth > boxWidth) {
                int startIndex = 0;
                while (fm.stringWidth(fullInput.substring(startIndex)) > boxWidth) {
                    startIndex++;
                }
                textToRender = fullInput.substring(startIndex);
            }
            else textToRender = fullInput;
        }
        else textToRender = "> Press ` to chat";
        g.drawString(textToRender, (int)inputBox.x + 5, (int)(inputBox.y + inputBox.height - 4*SCALE));

        g2d.dispose();
    }

    private void renderChatHistory(Graphics2D g2d) {
        g2d.setClip(chatBox);
        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int chatBoxInnerWidth = (int) chatBox.width - 15;

        List<RenderLine> allLines = new ArrayList<>();
        for (FormattedChatMessage message : history) {
            String prefix = String.format("[%s] %s: ", message.timestamp, message.username);
            int prefixWidth = fm.stringWidth(prefix);
            int contentMaxWidth = chatBoxInnerWidth - prefixWidth;

            if (message.wrappedContent == null) {
                message.wrappedContent = wrapText(message.content, contentMaxWidth, fm);
            }

            allLines.add(new RenderLine(
                    String.format("[%s] ", message.timestamp),
                    message.username,
                    ": " + (message.wrappedContent.isEmpty() ? "" : message.wrappedContent.get(0)),
                    message.userColor
            ));

            for (int i = 1; i < message.wrappedContent.size(); i++) {
                allLines.add(new RenderLine("", "", message.wrappedContent.get(i), message.userColor));
            }
        }

        this.totalHistoryLines = allLines.size();
        int intScrollOffset = (int) Math.round(this.scrollOffset);
        int startIdx = Math.max(0, totalHistoryLines - VISIBLE_HISTORY_LINES - intScrollOffset);
        int endIdx = totalHistoryLines - intScrollOffset;
        int y = (int) (chatBox.y + chatBox.height - fm.getDescent());

        for (int i = endIdx - 1; i >= startIdx; i--) {
            if (i < allLines.size()) {
                RenderLine line = allLines.get(i);
                int x = (int) chatBox.x + 5;

                // Timestamp
                g2d.setColor(Color.WHITE);
                g2d.drawString(line.timestampPart, x, y);
                x += fm.stringWidth(line.timestampPart);
                // Username
                g2d.setColor(line.userColor);
                g2d.drawString(line.usernamePart, x, y);
                x += fm.stringWidth(line.usernamePart);
                // Content
                g2d.setColor(Color.WHITE);
                g2d.drawString(line.contentPart, x, y);

                y -= lineHeight;
            }
        }
        g2d.setClip(null);
    }

    private void renderScrollBar(Graphics2D g2d) {
        if (totalHistoryLines <= VISIBLE_HISTORY_LINES) return;
        g2d.setColor(new Color(50, 50, 50, 180));
        g2d.fill(scrollBarTrack);

        double scrollableRange = totalHistoryLines - VISIBLE_HISTORY_LINES;
        double thumbHeightRatio = (double) VISIBLE_HISTORY_LINES / totalHistoryLines;
        double thumbHeight = Math.max(10 * SCALE, scrollBarTrack.height * thumbHeightRatio);
        double scrollRatio = (scrollableRange > 0) ? (scrollOffset / scrollableRange) : 0;
        double thumbY = scrollBarTrack.y + (scrollBarTrack.height - thumbHeight) * (1 - scrollRatio);

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
        if (totalHistoryLines > VISIBLE_HISTORY_LINES) {
            scrollOffset = Math.min(totalHistoryLines - VISIBLE_HISTORY_LINES, scrollOffset + 1);
        }
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
            if (totalHistoryLines <= VISIBLE_HISTORY_LINES) return;
            double scrollableHeight = scrollBarTrack.height - scrollBarThumb.height;
            if (scrollableHeight <= 0) return;
            double scrollDelta = (double)dy / scrollableHeight * (double)(totalHistoryLines - VISIBLE_HISTORY_LINES);
            scrollOffset -= scrollDelta;
            scrollOffset = Math.max(0, Math.min(totalHistoryLines - VISIBLE_HISTORY_LINES, scrollOffset));
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