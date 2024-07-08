package platformer.serialization;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * This class implements the Encryptor interface for encrypting and decrypting game data.
 * <p>
 * It uses AES encryption with PBKDF2 key derivation algorithm.
 */
public class GameEncryptor implements Encryptor {

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

    /**
     * Encrypts the provided data using AES encryption.
     * <p>
     * @param data The data to be encrypted.
     * @return The encrypted data as a Base64-encoded string.
     */
    @Override
    public String encrypt(String data) {
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

    /**
     * Decrypts the provided data using AES decryption.
     * <p>
     * @param data The data to be decrypted (Base64-encoded string).
     * @return The decrypted data as a string.
     */
    @Override
    public String decrypt(String data) {
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
