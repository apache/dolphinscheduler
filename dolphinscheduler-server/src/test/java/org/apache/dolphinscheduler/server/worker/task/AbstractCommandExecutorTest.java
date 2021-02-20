package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class AbstractCommandExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCommandExecutorTest.class);

    private ShellCommandExecutor shellCommandExecutor;

    @Before
    public void before() throws Exception {
        System.setProperty("log4j2.disable.jmx", Boolean.TRUE.toString());
        shellCommandExecutor = new ShellCommandExecutor(null);
    }

    @Test
    public void testSetTaskResultString() {
        shellCommandExecutor.setTaskResultString("shellReturn");
    }

    @Test
    public void testGetTaskResultString() {
        logger.info(shellCommandExecutor.getTaskResultString());
    }
}