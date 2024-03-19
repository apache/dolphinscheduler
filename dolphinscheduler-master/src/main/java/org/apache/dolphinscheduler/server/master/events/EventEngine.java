package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventEngine extends BaseDaemonThread {

    @Autowired
    private IWorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    @Autowired
    private EventFirer eventFirer;

    private final Set<Integer> firingWorkflowInstanceIds = ConcurrentHashMap.newKeySet();

    public EventEngine() {
        super("EventEngine");
    }

    @Override
    public synchronized void start() {
        super.start();
        log.info(getClass().getName() + " started");
    }

    @Override
    public void run() {
        for (;;) {
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                fireAllActiveEvents();
                stopWatch.stop();
                log.info("Fire all active events cost: {} ms", stopWatch.getTime());
                this.wait(5_000);
            } catch (Throwable throwable) {
                log.error("Fire active event error", throwable);
                ThreadUtils.sleep(3_000);
            }
        }
    }

    public void fireAllActiveEvents() {
        Collection<IWorkflowExecutionRunnable> workflowExecutionRunnableCollection =
                workflowExecuteRunnableRepository.getAll();
        for (IWorkflowExecutionRunnable workflowExecutionRunnable : workflowExecutionRunnableCollection) {
            ProcessInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
            final Integer workflowInstanceId = workflowInstance.getId();
            final String workflowInstanceName = workflowInstance.getName();
            try {
                LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
                if (firingWorkflowInstanceIds.contains(workflowInstanceId)) {
                    log.debug("WorkflowExecutionRunnable: {} is already in firing", workflowInstanceName);
                    return;
                }
                IEventRepository<IEvent> workflowEventRepository = workflowExecutionRunnable.getEventRepository();
                firingWorkflowInstanceIds.add(workflowInstanceId);
                eventFirer.fireActiveEvents(workflowEventRepository)
                        .whenComplete((fireCount, ex) -> {
                            firingWorkflowInstanceIds.remove(workflowInstanceId);
                            if (ex != null) {
                                log.error("Fire event for WorkflowExecutionRunnable: {} error", workflowInstanceName,
                                        ex);
                            } else {
                                if (fireCount > 0) {
                                    log.info("Fire {} events for WorkflowExecutionRunnable: {} success", fireCount,
                                            workflowInstanceName);
                                }
                            }
                        });
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

}
