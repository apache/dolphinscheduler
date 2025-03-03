package org.apache.dolphinscheduler.server.master.runner.queue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PriorityDelayEntryTest {

    private final long baseDelayTimeMills = 1000L;

    @Test
    public void testCompareToDataNullEqual() {
        assertThrows(NullPointerException.class, () -> new PriorityDelayEntry<>(baseDelayTimeMills, null));
    }

    @Test
    public void testCompareToPriorityDifferent() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills + 100, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills, "B");
        assertEquals(-1, entry1.compareTo(entry2)); // "A" has higher priority than "B"
        assertEquals(1, entry2.compareTo(entry1));  // "B" has lower priority than "A"
    }

    @Test
    public void testCompareToPriorityEqualDifferentDelay() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills + 100, "A");
        assertEquals(-1, entry1.compareTo(entry2)); // entry1 has earlier trigger time
        assertEquals(1, entry2.compareTo(entry1));  // entry2 has later trigger time
    }

    @Test
    public void testCompareToEqualPriorityAndDelay() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        assertEquals(0, entry1.compareTo(entry2));
    }
}
