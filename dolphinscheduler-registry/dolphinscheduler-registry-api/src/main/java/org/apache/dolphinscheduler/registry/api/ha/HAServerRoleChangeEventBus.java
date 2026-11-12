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

import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;

final class HAServerRoleChangeEventBus extends AbstractDelayEventBus<HAServerRoleChangeEvent> {

    boolean hasOwnRemovalRequests() {
        return delayEventQueue.stream()
                .filter(event -> event instanceof HAServerRoleChangeRequest)
                .map(event -> (HAServerRoleChangeRequest) event)
                .anyMatch(HAServerRoleChangeRequest::isOwnSelectorRemoved);
    }

    void discardRequestsThrough(long removalVersion) {
        delayEventQueue.removeIf(event -> event instanceof HAServerRoleChangeRequest
                && ((HAServerRoleChangeRequest) event).getRemovalVersion() <= removalVersion);
    }

    void clear() {
        delayEventQueue.clear();
    }
}
