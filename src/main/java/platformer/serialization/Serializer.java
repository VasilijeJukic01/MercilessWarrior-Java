package platformer.serialization;

public interface Serializer<U, V> {

    void serialize(U u);

    V deserialize();

}
