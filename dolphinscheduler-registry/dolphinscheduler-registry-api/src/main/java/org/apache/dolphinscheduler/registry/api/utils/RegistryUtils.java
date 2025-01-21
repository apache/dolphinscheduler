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

package org.apache.dolphinscheduler.registry.api.utils;

import org.apache.dolphinscheduler.common.model.BaseHeartBeat;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import com.google.common.base.Preconditions;

public class RegistryUtils {

    public static String getMasterFailoverLockPath(final String masterAddress) {
        Preconditions.checkNotNull(masterAddress, "master address cannot be null");
        return RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath() + "/" + masterAddress;
    }

    public static String getFailoveredNodePathWhichStartupTimeIsUnknown(final String serverAddress) {
        return RegistryNodeType.FAILOVER_FINISH_NODES.getRegistryPath() + "/" + serverAddress + "-" + "unknown" + "-"
                + "unknown";
    }

    public static String getFailoveredNodePath(final BaseHeartBeat baseHeartBeat) {
        return getFailoveredNodePath(
                baseHeartBeat.getHost() + ":" + baseHeartBeat.getPort(),
                baseHeartBeat.getStartupTime(),
                baseHeartBeat.getProcessId());
    }

    public static String getFailoveredNodePath(final String serverAddress, final long serverStartupTime,
                                               final int processId) {
        return RegistryNodeType.FAILOVER_FINISH_NODES.getRegistryPath() + "/" + serverAddress + "-" + serverStartupTime
                + "-" + processId;
    }
}
