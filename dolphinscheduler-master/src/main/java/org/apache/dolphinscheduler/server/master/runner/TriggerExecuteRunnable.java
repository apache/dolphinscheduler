package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.plugin.task.api.*;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerCallBack;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
public abstract class TriggerExecuteRunnable implements Runnable {


    protected @Nullable AbstractTask task;

    protected TriggerExecuteRunnable() {
    }

    protected void executeTrigger(TriggerCallBack triggerCallBack) {
        if (task == null) {
            throw new IllegalArgumentException("The task plugin instance is not initialized");
        }
//        task.handle(taskCallBack);
    }

    protected void afterExecute() throws TaskException {
//        if (task == null) {
//            throw new TaskException("The current task instance is null");
//        }
//        sendAlertIfNeeded();
//
//        sendTaskResult();
//
//        WorkerTaskExecutorHolder.remove(taskExecutionContext.getTaskInstanceId());
//        log.info("Remove the current task execute context from worker cache");
//        clearTaskExecPathIfNeeded();
    }

    protected void afterThrowing(Throwable throwable) throws TaskException {
//        if (cancelTask()) {
//            log.info("Cancel the task successfully");
//        }
//        WorkerTaskExecutorHolder.remove(taskExecutionContext.getTaskInstanceId());
//        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
//        taskExecutionContext.setEndTime(System.currentTimeMillis());
//        workerMessageSender.sendMessageWithRetry(taskExecutionContext,
//                ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.FINISH);
//        log.info("Get a exception when execute the task, will send the task status: {} to master: {}",
//                TaskExecutionStatus.FAILURE.name(), taskExecutionContext.getHost());

    }

    protected boolean cancelTask() {
        // cancel the task
        if (task == null) {
            return true;
        }
        try {
            task.cancel();
//            ProcessUtils.cancelApplication(taskExecutionContext);
            return true;
        } catch (Exception e) {
            log.error("Cancel task failed, this will not affect the taskInstance status, but you need to check manual",
                    e);
            return false;
        }
    }

    @Override
    public void run() {
        try {
            TaskInstanceLogHeader.printInitializeTaskContextHeader();
            initializeTask();

            TaskInstanceLogHeader.printLoadTaskInstancePluginHeader();
            beforeExecute();

//            TaskCallBack taskCallBack = TaskCallbackImpl.builder()
//                    .workerMessageSender(workerMessageSender)
//                    .taskExecutionContext(taskExecutionContext)
//                    .build();

            TaskInstanceLogHeader.printExecuteTaskHeader();
//            executeTrigger(taskCallBack);

            TaskInstanceLogHeader.printFinalizeTaskHeader();
            afterExecute();
            closeLogAppender();
        } catch (Throwable ex) {
            log.error("Task execute failed, due to meet an exception", ex);
            afterThrowing(ex);
            closeLogAppender();
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }

    protected void initializeTask() {
        log.info("Begin to initialize task");

        long taskStartTime = System.currentTimeMillis();
//        taskExecutionContext.setStartTime(taskStartTime);
        log.info("Set task startTime: {}", taskStartTime);

//        String taskAppId = String.format("%s_%s", taskExecutionContext.getProcessInstanceId(),
//                taskExecutionContext.getTaskInstanceId());
//        taskExecutionContext.setTaskAppId(taskAppId);
//        log.info("Set task appId: {}", taskAppId);

//        log.info("End initialize task {}", JSONUtils.toPrettyJsonString(taskExecutionContext));
    }

    protected void beforeExecute() {
    }

    protected void sendAlertIfNeeded() {

    }

    protected void sendTaskResult() {

    }

    protected void clearTaskExecPathIfNeeded() {

    }

    protected void closeLogAppender() {

    }

    public @Nullable AbstractTask getTask() {
        return task;
    }

}
