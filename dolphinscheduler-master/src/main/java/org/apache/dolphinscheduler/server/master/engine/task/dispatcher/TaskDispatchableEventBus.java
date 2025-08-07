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

package org.apache.dolphinscheduler.server.master.engine.task.dispatcher;

import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.event.TaskDispatchableEvent;

import lombok.SneakyThrows;

public class TaskDispatchableEventBus<V extends TaskDispatchableEvent<T>, T extends Comparable<T>>
        extends
            AbstractDelayEventBus<V> {

    public void add(V v) {
        super.publish(v);
    }

    @SneakyThrows
    public V take() {
        return super.take();
    }

    // Only use in test
    public int size() {
        return delayEventQueue.size();
    }

    // Only use in test
    public void clear() {
        delayEventQueue.clear();
    }
}
