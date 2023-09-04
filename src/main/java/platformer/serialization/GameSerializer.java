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
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.FilePaths.SAVE_PATH;

public class GameSerializer implements Serializer<Account, List<Account>> {

    private Gson gson = new Gson();
    private final List<String> saves = List.of("save1", "save2", "save3");

    @Override
    public void serialize(Account account, int index) {
        try (FileWriter writer = new FileWriter(SAVE_PATH + "save"+index)) {
            gson.toJson(account, writer);
            Logger.getInstance().notify("Game saved to slot "+index+".", Message.NOTIFICATION);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Account> deserialize() {
        List<Account> savedFilesData = new ArrayList<>();
        for (String save : saves) {
            String filePath = SAVE_PATH + save;
            File file = new File(filePath);

            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(filePath);
                    gson = new GsonBuilder().create();

                    Account account = gson.fromJson(fileReader, Account.class);
                    savedFilesData.add(account);

                    fileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return savedFilesData;
    }

}
