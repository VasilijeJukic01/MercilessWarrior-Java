package platformer.controller;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.core.loading.PathManager;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
        File userConfigFile = new File(PathManager.getConfigPath());
        if (userConfigFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userConfigFile))) {
                loadConfigFromReader(reader);
                return;
            } catch (IOException e) {
                Logger.getInstance().notify("Failed to load user keyboard config.", Message.WARNING);
            }
        }

        try (InputStream is = getClass().getResourceAsStream("/keyboard.config");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            loadConfigFromReader(reader);
        } catch (IOException | NullPointerException e) {
            Logger.getInstance().notify("Failed to load default keyboard config.", Message.ERROR);
        }
    }

    private void loadConfigFromReader(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=");
            if (parts.length == 2) keyConfig.put(parts[0].trim(), getKeyCode(parts[1].trim()));
        }
    }

    private int getKeyCode(String keyName) {
        String targetKeyName = keyName;
        if (keyName.startsWith("DIGIT")) targetKeyName = keyName.substring(5);
        try {
            return KeyEvent.class.getField("VK_" + targetKeyName.toUpperCase()).getInt(null);
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
