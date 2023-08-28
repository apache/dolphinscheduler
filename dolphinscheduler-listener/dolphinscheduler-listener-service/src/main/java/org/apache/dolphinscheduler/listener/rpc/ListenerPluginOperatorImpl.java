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

import org.apache.dolphinscheduler.extract.listener.IListenerPluginOperator;
import org.apache.dolphinscheduler.extract.listener.request.ListenerResponse;
import org.apache.dolphinscheduler.extract.listener.request.RegisterListenerPluginRequest;
import org.apache.dolphinscheduler.extract.listener.request.RemoveListenerPluginRequest;
import org.apache.dolphinscheduler.extract.listener.request.UpdateListenerPluginRequest;
import org.apache.dolphinscheduler.listener.service.ListenerPluginService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListenerPluginOperatorImpl implements IListenerPluginOperator {

    @Autowired
    private ListenerPluginService listenerPluginService;

    @Override
    public ListenerResponse registerListenerPlugin(RegisterListenerPluginRequest registerListenerPluginRequest) {
        return listenerPluginService.registerListenerPlugin(registerListenerPluginRequest.getFileName(),
                registerListenerPluginRequest.getClassPath(),
                registerListenerPluginRequest.getPluginJar());
    }

    @Override
    public ListenerResponse updateListenerPlugin(UpdateListenerPluginRequest updateListenerPluginRequest) {
        return listenerPluginService.updateListenerPlugin(updateListenerPluginRequest.getPluginId(),
                updateListenerPluginRequest.getFileName(),
                updateListenerPluginRequest.getClassPath(),
                updateListenerPluginRequest.getPluginJar());
    }

    @Override
    public ListenerResponse removeListenerPlugin(RemoveListenerPluginRequest removeListenerPluginRequest) {
        return listenerPluginService.removeListenerPlugin(removeListenerPluginRequest.getPluginId());
    }
}
