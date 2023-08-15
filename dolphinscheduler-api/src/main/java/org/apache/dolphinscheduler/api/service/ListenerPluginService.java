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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ListenerPluginService {

    Result registerListenerPlugin(MultipartFile file, String classPath);

    Result updateListenerPlugin(int id, MultipartFile file, String classPath);

    Result removeListenerPlugin(int id);

    Result listPluginPaging(String searchVal, Integer pageNo, Integer pageSize);

    Result listPluginList();

    Result createListenerInstance(int pluginDefineId, String instanceName, String pluginInstanceParams,
                                  List<ListenerEventType> listenerEventTypes);

    Result updateListenerInstance(int instanceId, String instanceName, String pluginInstanceParams,
                                  List<ListenerEventType> listenerEventType);

    Result removeListenerInstance(int id);

    Result listInstancePaging(String searchVal, Integer pageNo, Integer pageSize);

    boolean checkExistPluginInstanceName(String instanceName);
}
