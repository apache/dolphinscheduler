package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgsUtilsTest {

    public static final Logger logger = LoggerFactory.getLogger(ArgsUtilsTest.class);

    @Test
    public void escape() {
        Assertions.assertEquals(ArgsUtils.escape("23\42"), "23\\42");
    }

    @Test
    public void wrapperSingleQuotes() {
        Assertions.assertEquals(ArgsUtils.wrapperSingleQuotes("23"), "'23'");
    }
}
