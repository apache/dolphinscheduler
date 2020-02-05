package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggerUtilsTest {
    private Logger logger = LoggerFactory.getLogger(LoggerUtilsTest.class);

    @Test
    public void buildTaskId() {

      String taskId = LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,79,4084,15210);

      Assert.assertEquals(" - [taskAppId=TASK-79-4084-15210]", taskId);
    }

    @Test
    public void getAppIds() {
       List<String> appIdList =  LoggerUtils.getAppIds("Running job: application_1_1",logger);
       Assert.assertEquals("application_1_1", appIdList.get(0));

    }
}