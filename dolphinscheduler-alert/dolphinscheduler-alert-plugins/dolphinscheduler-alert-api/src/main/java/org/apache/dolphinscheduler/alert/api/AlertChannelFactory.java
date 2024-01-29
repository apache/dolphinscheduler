/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.alert.api;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPI;
import org.apache.dolphinscheduler.spi.plugin.SPIIdentify;

import java.util.List;

/**
 * alert channel factory
 */
public interface AlertChannelFactory extends PrioritySPI {

    /**
     * Returns the name of the alert channel
     *
     * @return the name of the alert channel
     */
    String name();

    /**
     * Create an alert channel
     *
     * @return alert channel
     */
    AlertChannel create();

    /**
     * Returns the configurable parameters that this plugin needs to display on the web ui
     */
    List<PluginParams> params();

    default SPIIdentify getIdentify() {
        return SPIIdentify.builder().name(name()).build();
    }
}
