package org.apache.dolphinscheduler.plugin.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class PropertyUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtilsTest.class);

    /**
     * Test getString
     */
    @Test
    public void testGetString() {

        String result = PropertyUtils.getString("test.string");
        logger.info(result);
        assertEquals("teststring", result);

        //If key is null, then return null
        result = PropertyUtils.getString(null);
        assertNull(result);
    }


    /**
     * Test getBoolean
     */
    @Test
    public void testGetBoolean() {

        //Expected true
        Boolean result = PropertyUtils.getBoolean("test.true");
        assertTrue(result);

        //Expected false
        result = PropertyUtils.getBoolean("test.false");
        assertFalse(result);
    }

    /**
     * Test getLong
     */
    @Test
    public void testGetLong() {
        long result = PropertyUtils.getLong("test.long");
        assertSame(result, 100L);
    }

    /**
     * Test getDouble
     */
    @Test
    public void testGetDouble() {

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        double result = PropertyUtils.getDouble("abc", 5.0);
        assertEquals(result, 5.0, 0);

        result = PropertyUtils.getDouble("cba", 5.0);
        assertEquals(3.1, result, 0.01);
    }

}