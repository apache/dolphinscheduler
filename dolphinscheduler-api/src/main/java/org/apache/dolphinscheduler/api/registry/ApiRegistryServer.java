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

package org.apache.dolphinscheduler.api.registry;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "register",name = "enabled",havingValue = "true")
public class ApiRegistryServer implements IStoppable {

    private final Logger logger = LoggerFactory.getLogger(ApiRegistryServer.class);

    @Resource
    private ApiRegistryClient apiRegistryClient;

    @PostConstruct
    public void start() {
        this.apiRegistryClient.init();
        this.apiRegistryClient.registry();
        this.apiRegistryClient.setRegistryStoppable(this);
    }

    @Override
    public void stop(String cause) {
        if (!Stopper.stop()) {
            logger.warn("WorkerServer is already stopped, current cause: {}", cause);
            return;
        }
        ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());

        logger.info("Api server stopped, current cause: {}", cause);
    }
}
