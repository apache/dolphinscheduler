package org.apache.dolphinscheduler.common.utils;

import org.apache.parquet.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class StringUtilsTest {
    @Test
    public void testIsNotEmpty() {
        //null string
        boolean b = StringUtils.isNotEmpty(null);
        Assert.assertFalse(b);

        //"" string
        b = StringUtils.isNotEmpty("");
        Assert.assertFalse(b);

        //" " string
        b = StringUtils.isNotEmpty(" ");
        Assert.assertTrue(b);

        //"test" string
        b = StringUtils.isNotEmpty("test");
        Assert.assertTrue(b);
    }

    @Test
    public void testIsNotBlank() {
        //null string
        boolean b = StringUtils.isNotBlank(null);
        Assert.assertFalse(b);

        //"" string
        b = StringUtils.isNotBlank("");
        Assert.assertFalse(b);

        //" " string
        b = StringUtils.isNotBlank(" ");
        Assert.assertFalse(b);

        //" test " string
        b = StringUtils.isNotBlank(" test ");
        Assert.assertTrue(b);

        //"test" string
        b = StringUtils.isNotBlank("test");
        Assert.assertTrue(b);
    }
}
