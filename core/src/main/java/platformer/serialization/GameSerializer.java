package platformer.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import platformer.core.Account;
import platformer.core.loading.PathManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and deserializes encrypted local save slots using atomic writes and automatic .bak recovery.
 */
public class GameSerializer implements Serializer<Account, List<Account>> {

    private static final int CURRENT_SAVE_VERSION = 1;
    private final Encryptor encryptor = new GameEncryptor();
    private final Gson gson = new GsonBuilder().create();
    private final List<String> saves = List.of("save1", "save2", "save3");

    /**
     * Serializes the provided account data and saves it to the specified index file.
     * <p>
     * @param account The account object to be serialized and saved.
     * @param index The index of the save file.
     */
    @Override
    public void serialize(Account account, int index) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        account.setLastTimeSaved(date + " " + time);
        account.setSaveVersion(CURRENT_SAVE_VERSION);

        try {
            String encryptedJson = encryptor.encrypt(gson.toJson(account));
            if (encryptedJson == null) return;

            Path savesDir = Paths.get(PathManager.getSavesPath());
            Path targetFile = savesDir.resolve("save" + index);
            Path tempFile = savesDir.resolve("save" + index + ".tmp");
            Path backupFile = savesDir.resolve("save" + index + ".bak");

            Files.writeString(tempFile, encryptedJson, StandardCharsets.UTF_8);
            if (Files.exists(targetFile)) {
                Files.copy(targetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Logger.getInstance().notify("Game saved to slot " + index + ".", Message.NOTIFICATION);
        } catch (IOException e) {
            Logger.getInstance().notify("Game saving to slot " + index + " failed: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Deserializes the game data from save files and returns a list of account objects.
     * <p>
     * @return A list of account objects deserialized from save files.
     */
    @Override
    public List<Account> deserialize() {
        List<Account> savedFilesData = new ArrayList<>();
        Path savesDir = Paths.get(PathManager.getSavesPath());
        for (String save : saves) {
            Path file = savesDir.resolve(save);
            Path backup = savesDir.resolve(save + ".bak");

            Account account = tryReadAccount(file);
            if (account == null && Files.exists(backup)) {
                Logger.getInstance().notify("Primary save " + save + " corrupted; recovering from backup.", Message.WARNING);
                account = tryReadAccount(backup);
            }
            savedFilesData.add(account);
        }
        return savedFilesData;
    }

    private Account tryReadAccount(Path file) {
        if (!Files.exists(file)) return null;
        try {
            String encryptedJson = Files.readString(file, StandardCharsets.UTF_8);
            String decryptedJson = encryptor.decrypt(encryptedJson);
            if (decryptedJson == null || decryptedJson.isBlank()) return null;
            return gson.fromJson(decryptedJson, Account.class);
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to read save file " + file.getFileName() + ": " + e.getMessage(), Message.ERROR);
            return null;
        }
    }

    @Override
    public void delete(int index) {
        Path savesDir = Paths.get(PathManager.getSavesPath());
        try {
            boolean deleted = Files.deleteIfExists(savesDir.resolve("save" + index));
            Files.deleteIfExists(savesDir.resolve("save" + index + ".bak"));
            Files.deleteIfExists(savesDir.resolve("save" + index + ".tmp"));
            if (deleted) Logger.getInstance().notify("Game at slot " + index + " deleted.", Message.NOTIFICATION);
            else Logger.getInstance().notify("Game at slot " + index + " deletion failed.", Message.ERROR);
        } catch (IOException e) {
            Logger.getInstance().notify("Game at slot " + index + " deletion failed: " + e.getMessage(), Message.ERROR);
        }
    }
}