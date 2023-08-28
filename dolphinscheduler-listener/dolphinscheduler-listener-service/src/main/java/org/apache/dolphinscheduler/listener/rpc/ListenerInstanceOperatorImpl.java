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

package org.apache.dolphinscheduler.listener.rpc;

import org.apache.dolphinscheduler.extract.listener.IListenerInstanceOperator;
import org.apache.dolphinscheduler.extract.listener.request.CreateListenerPluginInstanceRequest;
import org.apache.dolphinscheduler.extract.listener.request.ListenerResponse;
import org.apache.dolphinscheduler.extract.listener.request.RemoveListenerPluginInstanceRequest;
import org.apache.dolphinscheduler.extract.listener.request.UpdateListenerPluginInstanceRequest;
import org.apache.dolphinscheduler.listener.service.ListenerPluginService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListenerInstanceOperatorImpl implements IListenerInstanceOperator {

    @Autowired
    private ListenerPluginService listenerPluginService;

    @Override
    public ListenerResponse createListenerInstance(CreateListenerPluginInstanceRequest createListenerPluginInstanceRequest) {
        return listenerPluginService.createListenerInstance(createListenerPluginInstanceRequest.getPluginDefineId(),
                createListenerPluginInstanceRequest.getInstanceName(),
                createListenerPluginInstanceRequest.getPluginInstanceParams(),
                createListenerPluginInstanceRequest.getEventTypes());
    }

    @Override
    public ListenerResponse updateListenerInstance(UpdateListenerPluginInstanceRequest updateListenerPluginInstanceRequest) {
        return listenerPluginService.updateListenerInstance(updateListenerPluginInstanceRequest.getInstanceId(),
                updateListenerPluginInstanceRequest.getInstanceName(),
                updateListenerPluginInstanceRequest.getPluginInstanceParams(),
                updateListenerPluginInstanceRequest.getEventTypes());
    }

    @Override
    public ListenerResponse removeListenerInstance(RemoveListenerPluginInstanceRequest removeListenerPluginInstanceRequest) {
        return listenerPluginService.removeListenerInstance(removeListenerPluginInstanceRequest.getInstanceId());
    }
}
