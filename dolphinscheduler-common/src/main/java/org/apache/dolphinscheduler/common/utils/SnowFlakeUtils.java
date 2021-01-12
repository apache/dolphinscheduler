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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class SnowFlakeUtils {
    // start timestamp
    private final static long startTimestamp = 1609430400000L; //2021-01-01
    // Number of digits
    private final static long sequenceBit = 12;
    private final static long machineBit = 5;
    private final static long dataCenterBit = 5;
    // Maximum value
    private final static long maxDataCenterNum = ~(-1L << dataCenterBit);
    private final static long maxSequence = ~(-1L << sequenceBit);
    // The displacement to the left
    private final static long machineLeft = sequenceBit;
    private final static long dataCenterLeft = sequenceBit + machineBit;
    private final static long timestampLeft = dataCenterLeft + dataCenterBit;
    private final int dataCenterId;
    private final int machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private SnowFlakeUtils() throws UnknownHostException {
        this.dataCenterId = PropertyUtils.getInt(Constants.SNOW_FLAKE_DATA_CENTER_ID, 1);
        if (dataCenterId > maxDataCenterNum || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenterId can't be greater than %d or less than 0", maxDataCenterNum));
        }
        this.machineId = Math.abs(Objects.hash(InetAddress.getLocalHost().getHostName())) % 32;
    }

    private static SnowFlakeUtils instance = null;

    public static SnowFlakeUtils getInstance() throws UnknownHostException {
        if (instance == null) {
            synchronized (SnowFlakeUtils.class) {
                if (instance == null) {
                    instance = new SnowFlakeUtils();
                }
            }
        }
        return instance;
    }

    public synchronized long nextId() {
        long currStmp = nowTimestamp();
        if (currStmp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStmp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currStmp;
        return (currStmp - startTimestamp) << timestampLeft
                | dataCenterId << dataCenterLeft
                | machineId << machineLeft
                | sequence;
    }

    private long getNextMill() {
        long mill = nowTimestamp();
        while (mill <= lastTimestamp) {
            mill = nowTimestamp();
        }
        return mill;
    }

    private long nowTimestamp() {
        return System.currentTimeMillis();
    }
}
