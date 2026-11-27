package platformer.animation;

import platformer.serialization.GameEncryptor;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Inflater;

public class AssetManager {

    private static final int MAGIC_GPAK = ('G' << 24) | ('P' << 16) | ('A' << 8) | 'K';
    private static final int SUPPORTED_VERSION = 2;

    private record AssetEntry(long offset, int length, boolean compressed) { }

    private static volatile AssetManager instance;

    private final GameEncryptor encryptor = new GameEncryptor();
    private FileChannel archiveChannel;
    private SecretKey archiveKey;
    private Map<String, AssetEntry> index;

    private AssetManager() {
        loadArchive();
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

    private void loadArchive() {
        try {
            Path gpakFile = resolveArchivePath();
            if (gpakFile == null || !Files.exists(gpakFile)) {
                throw new RuntimeException("Encrypted asset file (assets.gpak) not found!");
            }

            FileChannel channel = FileChannel.open(gpakFile, StandardOpenOption.READ);
            ByteBuffer headerBuf = ByteBuffer.allocate(36);
            readFully(channel, headerBuf, 0);
            headerBuf.flip();

            int magic = headerBuf.getInt();
            int version = headerBuf.getInt();
            if (magic != MAGIC_GPAK || version != SUPPORTED_VERSION) {
                channel.close();
                throw new RuntimeException("Outdated or invalid assets.gpak format! Run EncryptionTool to build GPAK.");
            }

            byte[] salt = new byte[16];
            headerBuf.get(salt);
            long indexOffset = headerBuf.getLong();
            int indexLength = headerBuf.getInt();

            this.archiveChannel = channel;
            this.archiveKey = encryptor.deriveKey(salt);
            loadIndex(indexOffset, indexLength);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or decrypt assets", e);
        }
    }

    /**
     * Returns all packed asset paths matching the given directory prefix and file extension.
     */
    private Path resolveArchivePath() throws Exception {
        URL url = AssetManager.class.getResource("/assets.gpak");
        if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
            return Paths.get(url.toURI());
        }
        Path external = Paths.get("assets.gpak");
        if (Files.exists(external)) {
            return external;
        }
        // Packaged inside JAR: stream to temp file once (never loads full archive into heap)
        try (InputStream is = AssetManager.class.getResourceAsStream("/assets.gpak")) {
            if (is == null) return null;
            Path temp = Files.createTempFile("mw_assets_", ".gpak");
            temp.toFile().deleteOnExit();
            try (OutputStream os = Files.newOutputStream(temp)) {
                is.transferTo(os);
            }
            return temp;
        }
    }

    private void loadIndex(long indexOffset, int indexLength) throws IOException {
        ByteBuffer idxBuf = ByteBuffer.allocate(indexLength);
        readFully(archiveChannel, idxBuf, indexOffset);
        byte[] decryptedIndex = encryptor.decryptWithKey(idxBuf.array(), archiveKey);

        Map<String, AssetEntry> loadedIndex = new ConcurrentHashMap<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(decryptedIndex))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String path = dis.readUTF();
                long offset = dis.readLong();
                int length = dis.readInt();
                boolean compressed = dis.readBoolean();
                loadedIndex.put(path, new AssetEntry(offset, length, compressed));
            }
        }
        this.index = loadedIndex;
    }

    private static void readFully(FileChannel channel, ByteBuffer dst, long position) throws IOException {
        long pos = position;
        while (dst.hasRemaining()) {
            int read = channel.read(dst, pos);
            if (read < 0) throw new EOFException("Unexpected EOF reading assets.gpak at offset " + pos);
            pos += read;
        }
    }

    private static byte[] inflate(byte[] compressed) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(compressed.length * 2);
        byte[] buf = new byte[8192];
        while (!inflater.finished()) {
            int count = inflater.inflate(buf);
            if (count == 0 && inflater.needsInput()) break;
            baos.write(buf, 0, count);
        }
        inflater.end();
        return baos.toByteArray();
    }

    public boolean hasAsset(String path) {
        return index.containsKey(path);
    }

    public List<String> getAssetPaths(String prefix, String suffix) {
        return index.keySet().stream()
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
        AssetEntry entry = index.get(path);
        if (entry == null) return null;
        try {
            ByteBuffer buf = ByteBuffer.allocate(entry.length());
            readFully(archiveChannel, buf, entry.offset());
            byte[] decrypted = encryptor.decryptWithKey(buf.array(), archiveKey);
            return entry.compressed() ? inflate(decrypted) : decrypted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read asset from GPAK: " + path, e);
        }
    }
}