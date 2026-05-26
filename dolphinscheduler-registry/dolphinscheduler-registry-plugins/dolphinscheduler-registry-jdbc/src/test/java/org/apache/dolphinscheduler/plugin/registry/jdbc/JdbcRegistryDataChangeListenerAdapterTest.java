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

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class JdbcRegistryDataChangeListenerAdapterTest {

    private static final String WATCHED_PATH = "/nodes/master";

    /**
     * The REMOVE event must carry the deleted node value as eventData, otherwise downstream listeners
     * (e.g. AbstractClusterSubscribeListener / AbstractHAServer) cannot parse the removed server and will
     * silently drop the event, leaving stale registry data in memory.
     */
    @Test
    void onJdbcRegistryDataDeleted_shouldInjectDeletedValueAsEventData() {
        final String eventPath = "/nodes/master/127.0.0.1:5678";
        final String deletedValue = "{\"address\":\"127.0.0.1:5678\"}";
        final AtomicReference<Event> notified = new AtomicReference<>();

        final JdbcRegistryDataChangeListenerAdapter adapter = new JdbcRegistryDataChangeListenerAdapter(
                WATCHED_PATH, capturingListener(notified, SubscribeListener.SubscribeScope.CHILDREN_ONLY));

        adapter.onJdbcRegistryDataDeleted(eventPath, deletedValue);

        final Event event = notified.get();
        Truth.assertThat(event).isNotNull();
        Truth.assertThat(event.getType()).isEqualTo(Event.Type.REMOVE);
        Truth.assertThat(event.getWatchedPath()).isEqualTo(WATCHED_PATH);
        Truth.assertThat(event.getEventPath()).isEqualTo(eventPath);
        Truth.assertThat(event.getEventData()).isEqualTo(deletedValue);
    }

    private SubscribeListener capturingListener(final AtomicReference<Event> holder,
                                                final SubscribeListener.SubscribeScope scope) {
        return new SubscribeListener() {

            @Override
            public void notify(final Event event) {
                holder.set(event);
            }

            @Override
            public SubscribeScope getSubscribeScope() {
                return scope;
            }
        };
    }
}
