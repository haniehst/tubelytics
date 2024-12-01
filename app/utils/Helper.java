package utils;

import java.util.*;

/**
 * A utility class providing helper methods for common operations on data structures.
 * <p>
 * The {@code Helper} class includes methods for manipulating and transforming collections.
 * </p>
 *
 * @author Hanieh
 */
public class Helper {

    /**
     * Reverses the order of entries in the provided {@link LinkedHashMap} while maintaining
     * the key-value mappings.
     *
     * @param map the {@code LinkedHashMap} to be reversed
     * @return a new {@code LinkedHashMap} with entries in reversed order
     *
     * @throws NullPointerException if the provided map is {@code null}
     */
    public static <K, V> LinkedHashMap<K, V> reverseMap(LinkedHashMap<K, V> map) {
        LinkedHashMap<K, V> reversedMap = new LinkedHashMap<>();
        List<K> keys = new ArrayList<>(map.keySet());
        Collections.reverse(keys);
        for (K key : keys) {
            reversedMap.put(key, map.get(key));
        }
        return reversedMap;
    }
}