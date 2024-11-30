package utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;


/**
 * Unit tests for the {@link Helper} utility class.
 * <p>
 * This test class validates the functionality of the {@link Helper#reverseMap(LinkedHashMap)}
 * method to ensure it correctly reverses the order of entries in a LinkedHashMap.
 * </p>
 * @author Hanieh
 */
public class HelperTest {

    @Test
    public void testReverseMap() {
        // Arrange: Create a LinkedHashMap with some entries
        LinkedHashMap<Integer, String> originalMap = new LinkedHashMap<>();
        originalMap.put(1, "One");
        originalMap.put(2, "Two");
        originalMap.put(3, "Three");

        // Act: Call the reverseMap method
        LinkedHashMap<Integer, String> reversedMap = Helper.reverseMap(originalMap);

        // Assert: Verify the reversed order
        LinkedHashMap<Integer, String> expectedReversedMap = new LinkedHashMap<>();
        expectedReversedMap.put(3, "Three");
        expectedReversedMap.put(2, "Two");
        expectedReversedMap.put(1, "One");

        assertEquals("The reversed map does not match the expected output.", expectedReversedMap, reversedMap);

        // Additional assertions
        assertNotNull("The reversed map should not be null.", reversedMap);
        assertEquals("The size of the reversed map should match the original map.", originalMap.size(), reversedMap.size());
    }

    @Test
    public void testReverseMapEmpty() {
        // Arrange: Create an empty LinkedHashMap
        LinkedHashMap<Integer, String> emptyMap = new LinkedHashMap<>();

        // Act: Call the reverseMap method
        LinkedHashMap<Integer, String> reversedMap = Helper.reverseMap(emptyMap);

        // Assert: Verify the reversed map is also empty
        assertTrue("The reversed map should be empty for an empty input map.", reversedMap.isEmpty());
    }

    @Test
    public void testReverseMapSingleEntry() {
        // Arrange: Create a LinkedHashMap with a single entry
        LinkedHashMap<Integer, String> singleEntryMap = new LinkedHashMap<>();
        singleEntryMap.put(42, "Answer");

        // Act: Call the reverseMap method
        LinkedHashMap<Integer, String> reversedMap = Helper.reverseMap(singleEntryMap);

        // Assert: Verify the reversed map is the same as the original
        assertEquals("The reversed map should be identical to the original for a single entry.", singleEntryMap, reversedMap);
    }
}
