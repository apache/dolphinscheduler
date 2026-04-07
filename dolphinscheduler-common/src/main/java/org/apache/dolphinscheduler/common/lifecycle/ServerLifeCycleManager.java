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

package org.apache.dolphinscheduler.common.lifecycle;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ServerLifeCycleManager {

    private static volatile ServerStatus serverStatus = ServerStatus.RUNNING;

    private static long serverStartupTime = System.currentTimeMillis();

    public static long getServerStartupTime() {
        return serverStartupTime;
    }

    public static void toRunning() {
        serverStatus = ServerStatus.RUNNING;
    }

    public static boolean isStopped() {
        return serverStatus == ServerStatus.STOPPED;
    }

    public static ServerStatus getServerStatus() {
        return serverStatus;
    }

    public static synchronized boolean toStopped() {
        if (serverStatus == ServerStatus.STOPPED) {
            return false;
        }
        log.info("The current server status changed from {} to {}", serverStatus, ServerStatus.STOPPED);
        serverStatus = ServerStatus.STOPPED;
        return true;
    }

}
