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

package org.apache.dolphinscheduler.workflow.engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryEventRepository implements IEventRepository {

    private final LinkedBlockingDeque<IEvent> eventQueue;

    private MemoryEventRepository() {
        this.eventQueue = new LinkedBlockingDeque<>();
    }

    public static MemoryEventRepository newInstance() {
        return new MemoryEventRepository();
    }

    @Override
    public void storeEventToTail(IEvent event) {
        log.info("Store event to tail: {}", event);
        eventQueue.offerLast(event);
    }

    @Override
    public void storeEventToHead(IEvent event) {
        eventQueue.offerFirst(event);
    }

    @Override
    public IEvent poolEvent() {
        return eventQueue.poll();
    }

    @Override
    public int getEventSize() {
        return eventQueue.size();
    }

    public List<IEvent> getAllEvent() {
        return new ArrayList<>(eventQueue);
    }

}
