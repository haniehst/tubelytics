package utils;

import java.util.*;

public class Helper {

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