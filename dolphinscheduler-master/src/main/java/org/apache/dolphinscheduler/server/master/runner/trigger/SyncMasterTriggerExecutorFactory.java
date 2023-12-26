package org.apache.dolphinscheduler.server.master.runner.trigger;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.LogicTaskPluginFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncMasterTriggerExecutorFactory implements MasterTriggerExecutorFactory<SyncMasterTriggerExecutor> {
    @Autowired
    private LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder;
    @Autowired
    private LogicTaskInstanceExecutionEventSenderManager logicTaskInstanceExecutionEventSenderManager;
    @Override
    public SyncMasterTriggerExecutor createMasterTriggerExecutor() {
        return new SyncMasterTriggerExecutor();
    }
}
