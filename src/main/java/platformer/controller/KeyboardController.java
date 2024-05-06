package platformer.controller;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.FilePaths.KEYBOARD_CONFIG_PATH;

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
            e.printStackTrace();
        }
    }

    private int getKeyCode(String keyName) {
        try {
            return KeyEvent.class.getField("VK_" + keyName.toUpperCase()).getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getKeyForCommand(String command) {
        return keyConfig.get(command);
    }

}
