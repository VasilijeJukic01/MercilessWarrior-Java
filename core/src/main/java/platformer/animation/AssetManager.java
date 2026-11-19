package platformer.animation;

import platformer.serialization.GameEncryptor;
import java.io.*;
import java.util.List;
import java.util.Map;

public class AssetManager {

    private static volatile AssetManager instance;
    private Map<String, byte[]> assetData;

    private AssetManager() {
        loadAndDecryptAssets();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            synchronized (AssetManager.class) {
                if (instance == null) {
                    instance = new AssetManager();
                }
            }
        }
        return instance;
    }

    private void loadAndDecryptAssets() {
        try (InputStream is = AssetManager.class.getResourceAsStream("/assets.gpak")) {
            if (is == null) throw new RuntimeException("Encrypted asset file not found!");
            byte[] encryptedData = is.readAllBytes();
            GameEncryptor encryptor = new GameEncryptor();
            byte[] decryptedData = encryptor.decrypt(encryptedData);
            if (decryptedData == null) throw new RuntimeException("Failed to decrypt assets!");

            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                assetData = (Map<String, byte[]>) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or decrypt assets", e);
        }
    }

    /**
     * Returns all packed asset paths matching the given directory prefix and file extension.
     */
    public List<String> getAssetPaths(String prefix, String suffix) {
        return assetData.keySet().stream()
                .filter(path -> path.startsWith(prefix) && path.endsWith(suffix))
                .sorted()
                .toList();
    }

    /**
     * Reads files (like JSON or Configs) directly from the encrypted memory bundle, falling back to the classpath resource stream if not found in the bundle.
     *
     * @param path The resource path (e.g., "/meta/begin.json").
     * @return An InputStream for the resource, or null if not found.
     */
    public InputStream getResourceAsStream(String path) {
        byte[] data = getAsset(path);
        if (data != null) return new ByteArrayInputStream(data);
        return AssetManager.class.getResourceAsStream(path);
    }

    public byte[] getAsset(String path) {
        return assetData.get(path);
    }
}
