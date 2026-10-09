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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.registry.api.Event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.protobuf.ByteString;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.watch.WatchEvent;

class EtcdRegistryEventTest {

    private static final String WATCHED_PATH = "/nodes";
    private static final String EVENT_PATH = "/nodes/master-1";

    private final EtcdRegistry registry = Mockito.mock(EtcdRegistry.class);

    @Test
    void testDeleteUsesPreviousValue() {
        // An etcd DELETE contains only the key and modification revision in its current KV.
        KeyValue deletedKeyValue = new KeyValue(io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8(EVENT_PATH))
                .setModRevision(3)
                .build(), ByteSequence.EMPTY);
        assertEvent(new WatchEvent(deletedKeyValue, keyValue(EVENT_PATH, "previous-heartbeat"),
                WatchEvent.EventType.DELETE), Event.Type.REMOVE, "previous-heartbeat");
    }

    @Test
    void testAddUsesCurrentValue() {
        assertEvent(new WatchEvent(keyValue(EVENT_PATH, "current-heartbeat"),
                new KeyValue(io.etcd.jetcd.api.KeyValue.getDefaultInstance(), ByteSequence.EMPTY),
                WatchEvent.EventType.PUT), Event.Type.ADD, "current-heartbeat");
    }

    @Test
    void testUpdateUsesCurrentValue() {
        assertEvent(new WatchEvent(keyValue(EVENT_PATH, "current-heartbeat"),
                keyValue(EVENT_PATH, "previous-heartbeat"), WatchEvent.EventType.PUT),
                Event.Type.UPDATE, "current-heartbeat");
    }

    @Test
    void testDeleteWithoutPreviousValuePreservesPath() {
        assertEvent(new WatchEvent(keyValue(EVENT_PATH, ""),
                new KeyValue(io.etcd.jetcd.api.KeyValue.getDefaultInstance(), ByteSequence.EMPTY),
                WatchEvent.EventType.DELETE), Event.Type.REMOVE, "");
    }

    private void assertEvent(WatchEvent watchEvent, Event.Type expectedType, String expectedData) {
        Event event = ReflectionTestUtils.invokeMethod(registry, "toEvent", watchEvent, WATCHED_PATH);
        Assertions.assertNotNull(event);
        Assertions.assertEquals(expectedType, event.getType());
        Assertions.assertEquals(WATCHED_PATH, event.getWatchedPath());
        Assertions.assertEquals(EVENT_PATH, event.getEventPath());
        Assertions.assertEquals(expectedData, event.getEventData());
    }

    private KeyValue keyValue(String key, String value) {
        return new KeyValue(io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build(), ByteSequence.EMPTY);
    }
}
