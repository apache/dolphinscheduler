package org.apache.dolphinscheduler.spi.task;

public interface TaskExecutor {

     Task createTask(TaskRequest request);

     void init();
}
