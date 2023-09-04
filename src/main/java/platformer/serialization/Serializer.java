package platformer.serialization;

public interface Serializer<U, V> {

    void serialize(U u, int index);

    V deserialize();

}
