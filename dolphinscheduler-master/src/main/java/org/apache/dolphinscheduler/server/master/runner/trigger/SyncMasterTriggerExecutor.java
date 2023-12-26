package org.apache.dolphinscheduler.server.master.runner.trigger;

import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.task.ISyncLogicTask;

public class SyncMasterTriggerExecutor extends MasterTriggerExecutor {

    public SyncMasterTriggerExecutor() {
        super();
    }

    @Override
    protected void executeTrigger() throws MasterTaskExecuteException {
        if (logicTask == null) {
            throw new MasterTaskExecuteException("The task plugin instance is null");
        }
        ISyncLogicTask iSyncLogicTask = (ISyncLogicTask) logicTask;
        iSyncLogicTask.handle();
    }

    protected void afterExecute() throws MasterTaskExecuteException {

    }
}