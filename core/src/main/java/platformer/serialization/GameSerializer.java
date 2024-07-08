package platformer.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import platformer.core.Account;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.FilePaths.SAVE_PATH;

/**
 * The GameSerializer class implements the Serializer interface for serializing and deserializing game data.
 * <p>
 * It provides methods to save game data to files and load game data from files.
 */
public class GameSerializer implements Serializer<Account, List<Account>> {

    private final Encryptor encryptor;

    public GameSerializer() {
        this.encryptor = new GameEncryptor();
    }

    private final List<String> saves = List.of("save1", "save2", "save3");

    /**
     * Serializes the provided account data and saves it to the specified index file.
     * <p>
     * @param account The account object to be serialized and saved.
     * @param index The index of the save file.
     */
    @Override
    public void serialize(Account account, int index) {
        String day = LocalDate.now().getDayOfWeek().toString().substring(0, 3);
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        account.setLastTimeSaved(day+" "+time);
        try {
            Gson gson = new Gson();
            String jsonCache = gson.toJson(account);

            String encryptedJson = encryptor.encrypt(jsonCache);
            if (encryptedJson == null) return;

            try (FileWriter writer = new FileWriter(SAVE_PATH + "save" + index)) {
                writer.write(encryptedJson);
                Logger.getInstance().notify("Game saved to slot " + index + ".", Message.NOTIFICATION);
            }
        }
        catch (IOException e) {
            Logger.getInstance().notify("Game saving to slot " + index + "failed.", Message.ERROR);
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
        for (String save : saves) {
            String filePath = SAVE_PATH + save;
            File file = new File(filePath);

            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(filePath);
                    StringBuilder sb = new StringBuilder();
                    int ch;

                    while ((ch = fileReader.read()) != -1) {
                        sb.append((char) ch);
                    }
                    fileReader.close();

                    String encryptedJson = sb.toString();
                    String decryptedJson = encryptor.decrypt(encryptedJson);

                    Gson gson = new GsonBuilder().create();
                    Account account = gson.fromJson(decryptedJson, Account.class);
                    savedFilesData.add(account);

                }
                catch (IOException e) {
                    Logger.getInstance().notify("Game at "+ save +" corrupted.", Message.ERROR);
                }
            }
        }
        return savedFilesData;
    }

}
