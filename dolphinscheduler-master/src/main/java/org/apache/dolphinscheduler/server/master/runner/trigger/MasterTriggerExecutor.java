package org.apache.dolphinscheduler.server.master.runner.trigger;

import org.apache.dolphinscheduler.server.master.exception.LogicTaskFactoryNotFoundException;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.task.ILogicTask;

public abstract class MasterTriggerExecutor implements Runnable {
    protected ILogicTask logicTask;

    public MasterTriggerExecutor() {
    }

    protected abstract void executeTrigger() throws MasterTaskExecuteException;

    protected abstract void afterExecute() throws MasterTaskExecuteException;

    protected void afterThrowing(Throwable throwable) {

    }

    public void cancelTask() throws MasterTaskExecuteException {
        if (logicTask != null) {
            logicTask.kill();
        }
    }

    public void pauseTr() throws MasterTaskExecuteException {
        if (logicTask != null) {
            logicTask.pause();
        }
    }

    public ILogicTask getILogicTask() {
        return logicTask;
    }

    @Override
    public void run() {
        try {
            initializeTrigger();

            beforeExecute();

            executeTrigger();

            afterExecute();
        } catch (Throwable ex) {
            //log.error("Task execute failed, due to meet an exception", ex);
            afterThrowing(ex);
        } finally {
        }
    }

    protected void initializeTrigger() {

    }

    protected void beforeExecute() throws LogicTaskFactoryNotFoundException, LogicTaskInitializeException {
    }

}