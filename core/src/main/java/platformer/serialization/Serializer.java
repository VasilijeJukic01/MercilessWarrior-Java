package platformer.serialization;

/**
 * A serializer that can serialize and deserialize objects.
 *
 * @param <U> the type of the object to serialize
 * @param <V> the type of the object to deserialize
 */
public interface Serializer<U, V> {

    /**
     * Serializes the object at the given index.
     *
     * @param u the object to serialize
     * @param index the index to serialize the object at
     */
    void serialize(U u, int index);

    /**
     * Deserializes the object at the given index.
     *
     * @return the deserialized object
     */
    V deserialize();

}
