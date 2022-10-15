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

package org.apache.dolphinscheduler.server.master.registry;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class MasterRegistryDataListener implements SubscribeListener {

    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryDataListener.class);

    private final MasterRegistryClient masterRegistryClient;

    public MasterRegistryDataListener() {
        masterRegistryClient = SpringApplicationContext.getBean(MasterRegistryClient.class);
    }

    @Override
    public void notify(Event event) {
        final String path = event.path();
        if (Strings.isNullOrEmpty(path)) {
            return;
        }
        //monitor master
        if (path.startsWith(REGISTRY_DOLPHINSCHEDULER_MASTERS + Constants.SINGLE_SLASH)) {
            handleMasterEvent(event);
        } else if (path.startsWith(REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH)) {
            //monitor worker
            handleWorkerEvent(event);
        }
    }

    private void handleMasterEvent(Event event) {
        final String path = event.path();
        switch (event.type()) {
            case ADD:
                logger.info("master node added : {}", path);
                break;
            case REMOVE:
                masterRegistryClient.removeMasterNodePath(path, NodeType.MASTER, true);

                break;
            default:
                break;
        }
    }

    private void handleWorkerEvent(Event event) {
        final String path = event.path();
        switch (event.type()) {
            case ADD:
                logger.info("worker node added : {}", path);
                break;
            case REMOVE:
                logger.info("worker node deleted : {}", path);
                masterRegistryClient.removeWorkerNodePath(path, NodeType.WORKER, true);
                break;
            default:
                break;
        }
    }

}
