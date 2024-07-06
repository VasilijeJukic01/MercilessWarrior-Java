package platformer.serialization;

/**
 * Interface for encrypting and decrypting data.
 */
public interface Encryptor {

    /**
     * Encrypts the given data.
     *
     * @param data The data to encrypt.
     * @return The encrypted data.
     */
    String encrypt(String data);

    /**
     * Decrypts the given data.
     *
     * @param data The data to decrypt.
     * @return The decrypted data.
     */
    String decrypt(String data);

}
