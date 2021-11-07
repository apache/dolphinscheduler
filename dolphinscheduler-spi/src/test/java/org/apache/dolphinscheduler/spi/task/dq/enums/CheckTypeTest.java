package org.apache.dolphinscheduler.spi.task.dq.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CheckTypeTest {

    @Test
    public void testGetCode() {
        assertEquals(0, CheckType.COMPARISON_MINUS_STATISTICS.getCode());
        assertEquals(1, CheckType.STATISTICS_MINUS_COMPARISON.getCode());
        assertEquals(2, CheckType.STATISTICS_COMPARISON_PERCENTAGE.getCode());
        assertEquals(3, CheckType.STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE.getCode());
    }

    @Test
    public void testGetDescription() {
        assertEquals("comparison_minus_statistics", CheckType.COMPARISON_MINUS_STATISTICS.getDescription());
        assertEquals("statistics_minus_comparison", CheckType.STATISTICS_MINUS_COMPARISON.getDescription());
        assertEquals("statistics_comparison_percentage", CheckType.STATISTICS_COMPARISON_PERCENTAGE.getDescription());
        assertEquals("statistics_comparison_difference_comparison_percentage", CheckType.STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(CheckType.COMPARISON_MINUS_STATISTICS, CheckType.of(0));
    }
}
