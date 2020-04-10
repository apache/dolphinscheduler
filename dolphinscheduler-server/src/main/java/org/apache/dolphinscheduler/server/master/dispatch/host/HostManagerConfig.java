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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * host manager config
 */
@Configuration
public class HostManagerConfig {

    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    public HostManagerConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public HostManager hostManager() {
        String hostSelector = masterConfig.getHostSelector();
        HostSelector selector = HostSelector.of(hostSelector);
        HostManager hostManager;
        switch (selector){
            case RANDOM:
                hostManager = new RandomHostManager();
                break;
            case ROUNDROBIN:
                hostManager = new RoundRobinHostManager();
                break;
            case LOWERWEIGHT:
                hostManager = new LowerWeightHostManager();
                break;
            default:
                throw new IllegalArgumentException("unSupport selector " + hostSelector);
        }
        beanFactory.autowireBean(hostManager);
        return hostManager;
    }
}
