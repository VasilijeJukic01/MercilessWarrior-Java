package platformer.controller;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.FilePaths.KEYBOARD_CONFIG_PATH;

/**
 * KeyboardController class.
 * <p>
 * This class loads the key configuration from the file and provides the key code for a given command.
 */
public class KeyboardController {

    private final Map<String, Integer> keyConfig;

    public KeyboardController() {
        this.keyConfig = new HashMap<>();
        loadKeyConfig();
    }

    private void loadKeyConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader(KEYBOARD_CONFIG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    keyConfig.put(parts[0], getKeyCode(parts[1]));
                }
            }
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to load key config file: " + KEYBOARD_CONFIG_PATH, Message.ERROR);
        }
    }

    private int getKeyCode(String keyName) {
        try {
            return KeyEvent.class.getField("VK_" + keyName.toUpperCase()).getInt(null);
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to get key code for: " + keyName, Message.ERROR);
            return -1;
        }
    }

    public int getKeyForCommand(String command) {
        return keyConfig.get(command);
    }

    public String getKeyName(String keycode) {
        return KeyEvent.getKeyText(keyConfig.get(keycode));
    }

}
