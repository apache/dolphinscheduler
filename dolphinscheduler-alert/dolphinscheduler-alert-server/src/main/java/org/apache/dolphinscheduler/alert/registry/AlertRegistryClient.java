package org.apache.dolphinscheduler.alert.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.alert.AlertConfig;
import org.apache.dolphinscheduler.alert.task.AlertHeartbeatTask;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertRegistryClient implements AutoCloseable {

    @Autowired
    private AlertConfig alertConfig;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private AlertHeartbeatTask alertHeartbeatTask;

    public synchronized void start() {
        try {
            registry();
            // todo: we don't listen the connection state, since we will try to acquire the registry lock before loop
            // the alert task from db.
        } catch (Exception ex) {
            throw new RegistryException("AlertRegistryClient start up error", ex);
        }
    }

    public boolean getAlertLock() {
        try {
            return registryClient.getLock(NodeType.ALERT_LOCK.getRegistryPath());
        } catch (RegistryException ex) {
            log.error("Get alert lock from registry error", ex);
            return false;
        }
    }

    public boolean releaseAlertLock() {
        try {
            return registryClient.getLock(NodeType.ALERT_LOCK.getRegistryPath());
        } catch (RegistryException ex) {
            log.info("Release alert lock from registry error", ex);
            return false;
        }
    }

    private void registry() {
        String alertServerAddress = alertConfig.getAlertServerAddress();
        String alertServerRegistryPath = alertConfig.getAlertServerRegistryPath();
        log.info("Alert server: {} registering to registry: {}", alertServerAddress, alertServerRegistryPath);

        registryClient.remove(alertServerRegistryPath);
        registryClient.persistEphemeral(alertServerRegistryPath, alertHeartbeatTask.getHeartBeatJsonString());

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.ALERT_SERVER)) {
            ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
        }

        alertHeartbeatTask.start();
        log.info("Alert server: {} registered to registry: {}", alertServerAddress, alertServerRegistryPath);
    }

    @Override
    public void close() throws Exception {
        alertHeartbeatTask.shutdown();

        registryClient.close();
        log.info("Closed registry client");
    }
}
