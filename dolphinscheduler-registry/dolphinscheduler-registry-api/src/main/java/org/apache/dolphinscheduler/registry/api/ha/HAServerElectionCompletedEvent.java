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

import lombok.Getter;

/** Returns a finished Registry call to the role worker; it does not publish a role itself. */
@Getter
final class HAServerElectionCompletedEvent extends HAServerRoleChangeEvent {

    private final HAServerRoleChangeRequest request;
    private final boolean elected;
    private final Throwable failure;

    HAServerElectionCompletedEvent(HAServerRoleChangeRequest request, boolean elected, Throwable failure) {
        super(0);
        this.request = request;
        this.elected = elected;
        this.failure = failure;
    }
}
