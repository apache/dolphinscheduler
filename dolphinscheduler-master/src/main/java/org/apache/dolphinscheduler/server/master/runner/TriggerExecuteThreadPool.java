package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class TriggerExecuteThreadPool extends ThreadPoolTaskExecutor {

    @Autowired
    private MasterConfig masterConfig;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("StreamTaskExecuteThread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    public void executeEvent(final TriggerExecuteRunnable triggerExecuteRunnable) {
//        if (!streamTaskExecuteRunnable.isStart() || streamTaskExecuteRunnable.eventSize() == 0) {
//            return;
//        }
//        int taskInstanceId = streamTaskExecuteRunnable.getTaskInstance().getId();
//        ListenableFuture<?> future = this.submitListenable(streamTaskExecuteRunnable::handleEvents);
//        future.addCallback(new ListenableFutureCallback() {
//
//            @Override
//            public void onFailure(Throwable ex) {
//                LogUtils.setTaskInstanceIdMDC(taskInstanceId);
//                log.error("Stream task instance events handle failed", ex);
//                LogUtils.removeTaskInstanceIdMDC();
//            }
//
//            @Override
//            public void onSuccess(Object result) {
//                LogUtils.setTaskInstanceIdMDC(taskInstanceId);
//                log.info("Stream task instance is finished.");
//                LogUtils.removeTaskInstanceIdMDC();
//            }
//        });
    }
}
