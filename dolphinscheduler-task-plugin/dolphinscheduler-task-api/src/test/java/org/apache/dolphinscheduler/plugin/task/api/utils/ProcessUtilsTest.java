package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtilsTest {
    public static final Logger logger = LoggerFactory.getLogger(ProcessUtilsTest.class);

    @Test
    public void kill() throws IOException {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        Assertions.assertFalse(ProcessUtils.kill(taskExecutionContext));
    }
}
