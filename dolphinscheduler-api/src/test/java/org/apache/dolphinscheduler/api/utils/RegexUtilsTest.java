package org.apache.dolphinscheduler.api.utils;


import org.junit.Assert;
import org.junit.Test;

/**
 * RegexUtils test case
 */
public class RegexUtilsTest {

    @Test
    public void testIsNumeric() {
        String num1 = "123467854678";
        boolean numeric = RegexUtils.isNumeric(num1);
        Assert.assertTrue(numeric);

        String num2 = "0.0.01";
        boolean numeric2 = RegexUtils.isNumeric(num2);
        Assert.assertFalse(numeric2);
    }

}