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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryDataChangeListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

public class JdbcRegistryDataChangeListenerAdapter implements JdbcRegistryDataChangeListener {

    private final String watchedPath;
    private final SubscribeListener listener;

    public JdbcRegistryDataChangeListenerAdapter(final String watchedPath, final SubscribeListener listener) {
        this.watchedPath = watchedPath;
        this.listener = listener;
    }

    @Override
    public void onJdbcRegistryDataChanged(String eventPath, String value) {
        if (!isPathMatch(watchedPath, eventPath, listener.getSubscribeScope())) {
            return;
        }
        final Event event = Event.builder()
                .watchedPath(watchedPath)
                .eventPath(eventPath)
                .eventData(value)
                .type(Event.Type.UPDATE)
                .build();
        listener.notify(event);
    }

    @Override
    public void onJdbcRegistryDataDeleted(String eventPath) {
        if (!isPathMatch(watchedPath, eventPath, listener.getSubscribeScope())) {
            return;
        }
        final Event event = Event.builder()
                .watchedPath(watchedPath)
                .eventPath(eventPath)
                .type(Event.Type.REMOVE)
                .build();
        listener.notify(event);
    }

    @Override
    public void onJdbcRegistryDataAdded(String eventPath, String value) {
        if (!isPathMatch(watchedPath, eventPath, listener.getSubscribeScope())) {
            return;
        }
        final Event event = Event.builder()
                .watchedPath(watchedPath)
                .eventPath(eventPath)
                .eventData(value)
                .type(Event.Type.ADD)
                .build();
        listener.notify(event);
    }

    private boolean isPathMatch(final String subscribePath,
                                final String eventPath,
                                final SubscribeListener.SubscribeScope subscribeScope) {
        switch (subscribeScope) {
            case PATH_ONLY:
                return KeyUtils.isSamePath(subscribePath, eventPath);
            case CHILDREN_ONLY:
                return KeyUtils.isParent(subscribePath, eventPath);
            case ALL:
                return KeyUtils.isParent(subscribePath, eventPath)
                        || KeyUtils.isSamePath(subscribePath, eventPath);
            default:
                throw new IllegalArgumentException("Invalid subscribe scope " + subscribeScope);
        }
    }

}
