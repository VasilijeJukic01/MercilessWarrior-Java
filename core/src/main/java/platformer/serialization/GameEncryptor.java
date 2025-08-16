package platformer.serialization;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * This class implements the Encryptor interface for encrypting and decrypting game data.
 * <p>
 * It uses AES encryption with PBKDF2 key derivation algorithm.
 */
public class GameEncryptor implements Encryptor {

    private static final String SECRET_KEY = "HPLXBALYYYZWNX";

    /**
     * Encrypts the provided data using AES encryption.
     * <p>
     * @param data The data to be encrypted.
     * @return The encrypted data as a Base64-encoded string.
     */
    @Override
    public String encrypt(String data) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            byte[] iv = new byte[16];
            random.nextBytes(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256);
            SecretKey temp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(temp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] encryptedText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] output = new byte[salt.length + iv.length + encryptedText.length];
            System.arraycopy(salt, 0, output, 0, salt.length);
            System.arraycopy(iv, 0, output, salt.length, iv.length);
            System.arraycopy(encryptedText, 0, output, salt.length + iv.length, encryptedText.length);

            return Base64.getEncoder().encodeToString(output);
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
            byte[] decodedData = Base64.getDecoder().decode(data);

            byte[] salt = new byte[16];
            System.arraycopy(decodedData, 0, salt, 0, salt.length);
            byte[] iv = new byte[16];
            System.arraycopy(decodedData, salt.length, iv, 0, iv.length);
            byte[] encryptedText = new byte[decodedData.length - salt.length - iv.length];
            System.arraycopy(decodedData, salt.length + iv.length, encryptedText, 0, encryptedText.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] decryptedText = cipher.doFinal(encryptedText);

            return new String(decryptedText, StandardCharsets.UTF_8);
        }
        catch (Exception ignored) {}

        return null;
    }

    /**
     * Encrypts the provided byte array using AES encryption.
     * <p>
     * @param data The raw byte array to be encrypted.
     * @return The encrypted byte array.
     */
    public byte[] encrypt(byte[] data) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            byte[] iv = new byte[16];
            random.nextBytes(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256);
            SecretKey temp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(temp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] encryptedText = cipher.doFinal(data);

            byte[] output = new byte[salt.length + iv.length + encryptedText.length];
            System.arraycopy(salt, 0, output, 0, salt.length);
            System.arraycopy(iv, 0, output, salt.length, iv.length);
            System.arraycopy(encryptedText, 0, output, salt.length + iv.length, encryptedText.length);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the provided byte array using AES decryption.
     * <p>
     * @param data The encrypted byte array.
     * @return The decrypted raw byte array.
     */
    public byte[] decrypt(byte[] data) {
        try {
            byte[] salt = new byte[16];
            System.arraycopy(data, 0, salt, 0, salt.length);
            byte[] iv = new byte[16];
            System.arraycopy(data, salt.length, iv, 0, iv.length);
            byte[] encryptedText = new byte[data.length - salt.length - iv.length];
            System.arraycopy(data, salt.length + iv.length, encryptedText, 0, encryptedText.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return cipher.doFinal(encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
