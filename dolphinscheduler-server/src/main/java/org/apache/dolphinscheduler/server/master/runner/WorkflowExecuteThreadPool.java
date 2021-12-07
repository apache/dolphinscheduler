package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class WorkflowExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteThreadPool.class);

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateEventCallbackService stateEventCallbackService;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("Master-Exec-Thread");
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * submit state event
     */
    public void submitStateEvent(StateEvent stateEvent) {
        WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        if (workflowExecuteThread == null) {
            logger.error("workflowExecuteThread is null, processInstanceId:{}", stateEvent.getProcessInstanceId());
        }
        workflowExecuteThread.addStateEvent(stateEvent);
        this.execute(workflowExecuteThread);
    }

    /**
     * execute workflow
     */
    public void execute(WorkflowExecuteThread workflowExecuteThread) {
        int processInstanceId = workflowExecuteThread.getProcessInstance().getId();
        ListenableFuture future = this.submitListenable(workflowExecuteThread);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("handle events {} failed", processInstanceId, ex);
            }

            @Override
            public void onSuccess(Object result) {
                if (workflowExecuteThread.workFlowFinish()) {
                    processInstanceExecCacheManager.removeByProcessInstanceId(processInstanceId);
                    notifyProcessChanged(workflowExecuteThread.getProcessInstance());
                    logger.info("process instance {} finished.", processInstanceId);
                }
            }
        });
    }

    /**
     * notify process change
     */
    private void notifyProcessChanged(ProcessInstance finishProcessInstance) {
        if (Flag.NO == finishProcessInstance.getIsSubProcess()) {
            return;
        }
        Map<ProcessInstance, TaskInstance> fatherMaps = processService.notifyProcessList(finishProcessInstance.getId());
        for (ProcessInstance processInstance : fatherMaps.keySet()) {
            String address = NetUtils.getAddr(masterConfig.getListenPort());
            if (processInstance.getHost().equalsIgnoreCase(address)) {
                this.notifyMyself(processInstance, fatherMaps.get(processInstance));
            } else {
                this.notifyProcess(finishProcessInstance, processInstance, fatherMaps.get(processInstance));
            }
        }
    }

    /**
     * notify myself
     */
    private void notifyMyself(ProcessInstance processInstance, TaskInstance taskInstance) {
        logger.info("notify process {} task {} state change", processInstance.getId(), taskInstance.getId());
        if (!processInstanceExecCacheManager.contains(processInstance.getId())) {
            return;
        }
        StateEvent stateEvent = new StateEvent();
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        this.submitStateEvent(stateEvent);
    }

    /**
     * notify process's master
     */
    private void notifyProcess(ProcessInstance finishProcessInstance, ProcessInstance processInstance, TaskInstance taskInstance) {
        String host = processInstance.getHost();
        if (StringUtils.isEmpty(host)) {
            logger.error("process {} host is empty, cannot notify task {} now", processInstance.getId(), taskInstance.getId());
            return;
        }
        String address = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(
                finishProcessInstance.getId(), 0, finishProcessInstance.getState(), processInstance.getId(), taskInstance.getId()
        );
        stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command());
    }
}
