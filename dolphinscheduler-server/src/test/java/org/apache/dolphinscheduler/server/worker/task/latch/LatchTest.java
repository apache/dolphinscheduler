package org.apache.dolphinscheduler.server.worker.task.latch;


import java.io.IOException;

import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.connectors.ConnectorsTask;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatchTest {


    private static final Logger logger = LoggerFactory.getLogger(LatchTest.class);

    @Test
    public void testHandle(){

        long start = System.currentTimeMillis();

        String bodyParams = "{\"milliseconds\":10000}";

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(bodyParams);

        LatchTask latchTask = new LatchTask(taskExecutionContext,logger);

        latchTask.init();

        try {

            latchTask.handle();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("use time :  " + (System.currentTimeMillis() - start ));

    }



}
