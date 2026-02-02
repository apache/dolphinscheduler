/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.worker.alert;

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.extract.alert.IAlertOperator;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendRequest;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private RegistryClient registryClient;

    @Override
    public void sentAlert(int groupId, String title, String content, AlertType alertType) {
        log.debug("Attempting to send alert - groupId: {}, title: {}", groupId, title);

        Optional<Host> alertServerAddressOptional = getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Failed to send alert: alert server not exist.");
            throw new TaskException("Failed to send alert: alert server not exist");
        }

        Host alertServerAddress = alertServerAddressOptional.get();

        AlertSendRequest alertSendRequest = new AlertSendRequest(groupId, title, content, alertType);
        AlertSendResponse alertSendResponse;

        try {
            alertSendResponse = Clients
                    .withService(IAlertOperator.class)
                    .withHost(alertServerAddress.getAddress())
                    .sendAlert(alertSendRequest);
            log.info("Alert sent successfully to {} - groupId: {}, title: '{}', response: {}",
                    alertServerAddress.getAddress(), groupId, title, alertSendResponse);

            if (!alertSendResponse.isSuccess()) {
                log.error("Failed to send alert: alertSendResponse is fail");
                throw new TaskException("Failed to send alert: alertSendResponse is fail");
            } else {
                log.info("Success to send alert");
            }
        } catch (Exception e) {
            String errorMsg = String.format("Exception occurred while sending alert to %s - groupId: %d, title: '%s'",
                    alertServerAddress.getAddress(), groupId, title);
            log.error(errorMsg, e);
            throw new TaskException("Failed to send alert due to exception: " + e.getMessage(), e);
        }
    }

    public Optional<Host> getAlertServerAddress() {
        List<Server> serverList = registryClient.getServerList(RegistryNodeType.ALERT_SERVER);
        if (CollectionUtils.isEmpty(serverList)) {
            return Optional.empty();
        }
        Server server = serverList.get(0);
        return Optional.of(new Host(server.getHost(), server.getPort()));
    }
}
