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

package org.apache.dolphinscheduler.eventbus;

import java.util.Optional;

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

/**
 * The interface of event bus, which is used to publish and poll events.
 *
 * @param <T> event type
 */
public interface IEventBus<T extends IEvent> {

    /**
     * Publish an event to the bus.
     */
    void publish(T event);

    /**
     * Remove the head event from the bus. This method will not block if the event bus is empty will return empty optional.
     * <p> If the thread is interrupted, an {@link InterruptedException} will be thrown.
     */
    Optional<T> poll() throws InterruptedException;

    /**
     * peek the head event from the bus. This method will not block if the event bus is empty will return empty optional.
     */
    Optional<T> peek();

    /**
     * Remove the head event from the bus. This method will not block if the event bus is empty will return empty optional.
     */
    Optional<T> remove();

    /**
     * Whether the bus is empty.
     */
    boolean isEmpty();
}
