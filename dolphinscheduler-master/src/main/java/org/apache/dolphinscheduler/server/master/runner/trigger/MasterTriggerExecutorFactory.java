package org.apache.dolphinscheduler.server.master.runner.trigger;


public interface MasterTriggerExecutorFactory<T extends MasterTriggerExecutor> {

    T createMasterTriggerExecutor();

}
