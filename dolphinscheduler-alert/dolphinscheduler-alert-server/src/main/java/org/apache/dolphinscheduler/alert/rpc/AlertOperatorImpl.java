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
package org.apache.dolphinscheduler.alert.rpc;

import org.apache.dolphinscheduler.alert.service.AlertSender;
import org.apache.dolphinscheduler.extract.alert.IAlertOperator;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendRequest;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.extract.alert.request.AlertTestSendRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertOperatorImpl implements IAlertOperator {

    @Autowired
    private AlertSender alertSender;

    @Override
    public AlertSendResponse sendAlert(AlertSendRequest alertSendRequest) {
        log.info("Received AlertSendRequest : {}", alertSendRequest);
        AlertSendResponse alertSendResponse = alertSender.syncHandler(
                alertSendRequest.getGroupId(),
                alertSendRequest.getTitle(),
                alertSendRequest.getContent());
        log.info("Handle AlertSendRequest finish: {}", alertSendResponse);
        return alertSendResponse;
    }

    @Override
    public AlertSendResponse sendTestAlert(AlertTestSendRequest alertSendRequest) {
        log.info("Received AlertTestSendRequest : {}", alertSendRequest);
        AlertSendResponse alertSendResponse = alertSender.syncTestSend(
                alertSendRequest.getPluginDefineId(),
                alertSendRequest.getPluginInstanceParams());
        log.info("Handle AlertTestSendRequest finish: {}", alertSendResponse);
        return alertSendResponse;
    }
}
