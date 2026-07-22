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

package org.apache.dolphinscheduler.server.master.engine.system.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import lombok.Getter;

@Getter
public class GlobalMasterFailoverEvent extends AbstractSystemEvent {

    private final Date eventTime;

    public GlobalMasterFailoverEvent(Date eventTime) {
        super();
        this.eventTime = eventTime;
    }

    public static GlobalMasterFailoverEvent of(final Date eventTime) {
        checkNotNull(eventTime);
        return new GlobalMasterFailoverEvent(eventTime);
    }

    @Override
    public SystemEventType getEventType() {
        return SystemEventType.GLOBAL_MASTER_FAILOVER;
    }

    @Override
    public String toString() {
        return "GlobalMasterFailoverEvent{" +
                "eventTime=" + eventTime +
                '}';
    }
}
