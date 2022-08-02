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

package org.apache.dolphinscheduler.plugin.task.java;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(TaskChannelFactory.class)
public class JavaTaskChannelFactory implements TaskChannelFactory {
    /**
     * @description: Construct a channel for a Java task
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: org.apache.dolphinscheduler.plugin.task.api.TaskChannel
     **/
    @Override
    public TaskChannel create() {
        return new JavaTaskChannel();
    }

    /**
     * @description: Get a unique identifier of the Java task
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: java.lang.String
     **/
    @Override
    public String getName() {
        return "JAVA";
    }

    /**
     * @description: Gets the plug-in parameters for the Java task
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: java.util.List<org.apache.dolphinscheduler.spi.params.base.PluginParams>
     **/
    @Override
    public List<PluginParams> getParams() {
        return null;
    }
}
