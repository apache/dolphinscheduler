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

package org.apache.dolphinscheduler.server.master.cluster;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractClusterSubscribeListener<T extends BaseServerMetadata> implements SubscribeListener {

    @Override
    public void notify(Event event) {
        try {
            // make sure the event is processed in order
            synchronized (this) {
                Event.Type type = event.type();
                T server = parseServerFromHeartbeat(event.data());
                if (server == null) {
                    log.error("Unknown cluster change event: {}", event);
                    return;
                }
                switch (type) {
                    case ADD:
                        log.info("Server {} added", server);
                        onServerAdded(server);
                        break;
                    case REMOVE:
                        log.warn("Server {} removed", server);
                        onServerRemove(server);
                        break;
                    case UPDATE:
                        log.debug("Server {} updated", server);
                        onServerUpdate(server);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            log.error("Notify cluster change event: {} failed", event, ex);
        }
    }

    abstract T parseServerFromHeartbeat(String serverHeartBeatJson);

    public abstract void onServerAdded(T serverHeartBeat);

    public abstract void onServerRemove(T serverHeartBeat);

    public abstract void onServerUpdate(T serverHeartBeat);

}
