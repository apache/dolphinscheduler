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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.HeartBeatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  HeartBeat utils
 */
public class HeartBeatUtils {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatUtils.class);

    /**
     * decode master heartbeat
     */
    public static HeartBeatModel decodeMasterHeartBeat(String heartBeatInfo) {
        String[] parts = heartBeatInfo.split(Constants.COMMA);
        if (parts.length != Constants.MASTER_HEARTBEAT_LENGTH) {
            return null;
        }
        HeartBeatModel heartBeat = new HeartBeatModel();
        heartBeat.setCpuUsage(Double.parseDouble(parts[0]));
        heartBeat.setMemoryUsage(Double.parseDouble(parts[1]));
        heartBeat.setLoadAverage(Double.parseDouble(parts[2]));
        heartBeat.setAvailablePhysicalMemorySize(Double.parseDouble(parts[3]));
        heartBeat.setMaxCpuloadAvg(Double.parseDouble(parts[4]));
        heartBeat.setReservedMemory(Double.parseDouble(parts[5]));
        heartBeat.setStartupTime(Long.parseLong(parts[6]));
        heartBeat.setReportTime(Long.parseLong(parts[7]));
        heartBeat.setServerStatus(Integer.parseInt(parts[8]));
        heartBeat.setProcessId(Integer.parseInt(parts[9]));
        heartBeat.setDiskAvailable(Double.parseDouble(parts[10]));
        return heartBeat;
    }

    /**
     * decode worker heartbeat
     */
    public static HeartBeatModel decodeWorkerHeartBeat(String heartBeatInfo) {
        String[] parts = heartBeatInfo.split(Constants.COMMA);
        if (parts.length != Constants.WORKER_HEARTBEAT_LENGTH) {
            return null;
        }
        HeartBeatModel heartBeat = new HeartBeatModel();
        heartBeat.setCpuUsage(Double.parseDouble(parts[0]));
        heartBeat.setMemoryUsage(Double.parseDouble(parts[1]));
        heartBeat.setLoadAverage(Double.parseDouble(parts[2]));
        heartBeat.setAvailablePhysicalMemorySize(Double.parseDouble(parts[3]));
        heartBeat.setMaxCpuloadAvg(Double.parseDouble(parts[4]));
        heartBeat.setReservedMemory(Double.parseDouble(parts[5]));
        heartBeat.setStartupTime(Long.parseLong(parts[6]));
        heartBeat.setReportTime(Long.parseLong(parts[7]));
        heartBeat.setServerStatus(Integer.parseInt(parts[8]));
        heartBeat.setProcessId(Integer.parseInt(parts[9]));
        heartBeat.setWorkerHostWeight(Integer.parseInt(parts[10]));
        heartBeat.setWorkerExecThreadCount(Integer.parseInt(parts[11]));
        heartBeat.setWorkerWaitingTaskCount(Integer.parseInt(parts[12]));
        heartBeat.setDiskAvailable(Double.parseDouble(parts[13]));
        return heartBeat;
    }

    public static HeartBeatModel decodeHeartBeat(String heartBeatInfo, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                return decodeMasterHeartBeat(heartBeatInfo);
            case WORKER:
                return decodeWorkerHeartBeat(heartBeatInfo);
            default:
                throw new IllegalStateException("Should not reach here");
        }
    }
}
