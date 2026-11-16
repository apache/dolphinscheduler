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

package org.apache.dolphinscheduler.registry.api.ha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.registry.api.Registry;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractHAServerTest {

    private static final String SELECTOR_PATH = "/coordinator";
    private static final String ADDRESS = "master-0:5678";

    private final AtomicReference<String> owner = new AtomicReference<>();
    private Registry registry;
    private AbstractHAServer server;

    @BeforeEach
    void setUp() {
        registry = mock(Registry.class);
        when(registry.acquireLock(SELECTOR_PATH + "-lock")).thenReturn(true);
        when(registry.exists(SELECTOR_PATH)).thenAnswer(invocation -> owner.get() != null);
        when(registry.get(SELECTOR_PATH)).thenAnswer(invocation -> owner.get());
        doAnswer(invocation -> {
            owner.set(invocation.getArgument(1));
            return null;
        }).when(registry).put(eq(SELECTOR_PATH), anyString(), eq(true));
        server = new AbstractHAServer(registry, SELECTOR_PATH, ADDRESS) {

            @Override
            public void close() {
            }
        };
    }

    @Test
    void testInitialElectionWritesAddressAndTimestamp() {
        server.start();

        assertTrue(server.isActive());
        assertTrue(owner.get().startsWith(ADDRESS + "#"));
        assertTrue(Long.parseLong(owner.get().substring(ADDRESS.length() + 1)) > 0);
        verify(registry).put(SELECTOR_PATH, owner.get(), true);
    }

    @Test
    void testDoesNotAdoptLegacySelectorAtSameAddress() {
        // The predecessor's selector can remain until its Registry lease or session expires.
        owner.set(ADDRESS);

        server.start();

        assertFalse(server.isActive());
        assertEquals(ADDRESS, owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotAdoptPredecessorWithEarlierTimestamp() {
        String predecessor = ADDRESS + "#0";
        owner.set(predecessor);

        server.start();

        assertFalse(server.isActive());
        assertEquals(predecessor, owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testRecheckRecognizesCurrentProcess() {
        server.start();
        String identity = owner.get();

        assertTrue(server.participateElection());

        assertEquals(identity, owner.get());
        verify(registry, times(1)).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }

    @Test
    void testDoesNotReplaceExistingPeerSelector() {
        owner.set("master-1:5678#0");

        server.start();

        assertFalse(server.isActive());
        assertEquals("master-1:5678#0", owner.get());
        verify(registry, never()).put(eq(SELECTOR_PATH), anyString(), eq(true));
    }
}
