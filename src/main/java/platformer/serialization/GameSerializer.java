package platformer.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import platformer.core.Account;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static platformer.constants.FilePaths.SAVE_PATH;

public class GameSerializer implements Serializer<Account, List<Account>> {

    private final List<String> saves = List.of("save1", "save2", "save3");
    private static final String SECRET_KEY = "HPLXBALYYYZWNX";

    private static final byte[] SALT = new byte[] {
            (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
            (byte) 0x90, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
    };
    private static final byte[] IV = new byte[] {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
            (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
            (byte) 0x76, (byte) 0x54, (byte) 0x32, (byte) 0x10
    };

    @Override
    public void serialize(Account account, int index) {
        String day = LocalDate.now().getDayOfWeek().toString().substring(0, 3);
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        account.setLastTimeSaved(day+" "+time);
        try {
            Gson gson = new Gson();
            String jsonCache = gson.toJson(account);

            String encryptedJson = encrypt(jsonCache);
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
                    String decryptedJson = decrypt(encryptedJson);

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

    private String encrypt(String data) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT, 65536, 256);
            SecretKey temp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(temp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));

            byte[] encryptedText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] saltAndIv = new byte[SALT.length + IV.length];
            System.arraycopy(SALT, 0, saltAndIv, 0, SALT.length);
            System.arraycopy(IV, 0, saltAndIv, SALT.length, IV.length);

            byte[] encryptedData = new byte[saltAndIv.length + encryptedText.length];
            System.arraycopy(saltAndIv, 0, encryptedData, 0, saltAndIv.length);
            System.arraycopy(encryptedText, 0, encryptedData, saltAndIv.length, encryptedText.length);

            return Base64.getEncoder().encodeToString(encryptedData);
        }
        catch (Exception ignored) {}

        return null;
    }

    private String decrypt(String data) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(data);

            byte[] saltAndIv = new byte[SALT.length + IV.length];
            System.arraycopy(encryptedData, 0, saltAndIv, 0, saltAndIv.length);

            byte[] encryptedText = new byte[encryptedData.length - saltAndIv.length];
            System.arraycopy(encryptedData, saltAndIv.length, encryptedText, 0, encryptedText.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));

            byte[] decryptedText = cipher.doFinal(encryptedText);

            return new String(decryptedText, StandardCharsets.UTF_8);
        }
        catch (Exception ignored) {}

        return null;
    }

}
