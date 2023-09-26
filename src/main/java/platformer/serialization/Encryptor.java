package platformer.serialization;

public interface Encryptor {

    String encrypt(String data);

    String decrypt(String data);

}
