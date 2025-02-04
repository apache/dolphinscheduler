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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public class ZookeeperTreeCacheListenerAdapter implements TreeCacheListener {

    private final String watchedPath;

    private final SubscribeListener listener;

    public ZookeeperTreeCacheListenerAdapter(final String watchedPath, final SubscribeListener listener) {
        this.listener = listener;
        this.watchedPath = watchedPath;
    }

    @Override
    public void childEvent(final CuratorFramework curatorFramework, final TreeCacheEvent event) {
        final String eventPath = event.getData().getPath();
        switch (listener.getSubscribeScope()) {
            case PATH_ONLY:
                if (eventPath.equals(watchedPath)) {
                    listener.notify(convertToEvent(event, watchedPath));
                }
                break;
            case CHILDREN_ONLY:
                if (!eventPath.equals(watchedPath)) {
                    listener.notify(convertToEvent(event, watchedPath));
                }
                break;
            case ALL:
                listener.notify(convertToEvent(event, watchedPath));
                break;
            default:
                throw new RegistryException("Unknown event scope: " + listener.getSubscribeScope());
        }
    }

    private Event convertToEvent(TreeCacheEvent event, String watchedPath) {

        Event.Type type;
        switch (event.getType()) {
            case NODE_ADDED:
                type = Event.Type.ADD;
                break;
            case NODE_UPDATED:
                type = Event.Type.UPDATE;
                break;
            case NODE_REMOVED:
                type = Event.Type.REMOVE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }

        final ChildData data = event.getData();
        return Event.builder()
                .type(type)
                .watchedPath(watchedPath)
                .eventPath(data.getPath())
                .eventData(new String(data.getData()))
                .build();
    }
}
