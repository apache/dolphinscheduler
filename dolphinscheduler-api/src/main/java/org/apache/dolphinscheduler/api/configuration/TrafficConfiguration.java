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

package org.apache.dolphinscheduler.api.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrafficConfiguration {

    @Value("${traffic.control.global.switch:false}")
    private boolean trafficGlobalControlSwitch;
    @Value("${traffic.control.max.global.qps.rate:300}")
    private Integer maxGlobalQpsRate;
    @Value("${traffic.control.tenant.switch:false}")
    private boolean trafficTenantControlSwitch;
    @Value("${traffic.control.default.tenant.qps.rate:10}")
    private Integer defaultTenantQpsRate;
    @Value("#{'${traffic.control.customize.tenant.qps.rate:}'.empty?null:'${traffic.control.customize.tenant.qps.rate:}'}")
    private Map<String, Integer> customizeTenantQpsRate;

    public boolean isTrafficGlobalControlSwitch() {
        return trafficGlobalControlSwitch;
    }

    public void setTrafficGlobalControlSwitch(boolean trafficGlobalControlSwitch) {
        this.trafficGlobalControlSwitch = trafficGlobalControlSwitch;
    }

    public Integer getMaxGlobalQpsRate() {
        return maxGlobalQpsRate;
    }

    public void setMaxGlobalQpsRate(Integer maxGlobalQpsRate) {
        this.maxGlobalQpsRate = maxGlobalQpsRate;
    }

    public boolean isTrafficTenantControlSwitch() {
        return trafficTenantControlSwitch;
    }

    public void setTrafficTenantControlSwitch(boolean trafficTenantControlSwitch) {
        this.trafficTenantControlSwitch = trafficTenantControlSwitch;
    }

    public Integer getDefaultTenantQpsRate() {
        return defaultTenantQpsRate;
    }

    public void setDefaultTenantQpsRate(Integer defaultTenantQpsRate) {
        this.defaultTenantQpsRate = defaultTenantQpsRate;
    }

    public Map<String, Integer> getCustomizeTenantQpsRate() {
        return customizeTenantQpsRate;
    }

    public void setCustomizeTenantQpsRate(Map<String, Integer> customizeTenantQpsRate) {
        this.customizeTenantQpsRate = customizeTenantQpsRate;
    }
}
