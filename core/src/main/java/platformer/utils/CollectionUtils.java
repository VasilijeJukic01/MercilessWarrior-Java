package platformer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class providing static, generic methods for common operations on collections and arrays.
 */
public final class CollectionUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CollectionUtils() {}

    /**
     * Reverses the elements of an array in-place.
     *
     * @param <T> The generic type of the array elements.
     * @param arr The array to be reversed. This array is modified directly.
     * @return The same array instance that was passed in, now reversed.
     */
    public static <T> T[] reverseArray(T[] arr) {
        int start = 0;
        int end = arr.length - 1;
        while (start < end) {
            T temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        return arr;
    }

    /**
     * Flattens a map of lists into a single list containing all elements from all value lists.
     *
     * @param <T>     The generic type of the elements within the lists.
     * @param itemMap A map where each value is a {@code List} of items of type {@code T}. The keys are ignored.
     * @return A new {@code List<T>} containing all elements from the map's value lists.
     */
    public static <T> List<T> getAllItems(Map<?, List<T>> itemMap) {
        List<T> allItems = new ArrayList<>();
        itemMap.values().forEach(allItems::addAll);
        return allItems;
    }
}