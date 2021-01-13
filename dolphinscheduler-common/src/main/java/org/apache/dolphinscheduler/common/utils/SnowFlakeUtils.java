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
    private static final long START_TIMESTAMP = 1609430400000L; //2021-01-01
    // Number of digits
    private static final long SEQUENCE_BIT = 12;
    private static final long MACHINE_BIT = 5;
    private static final long DATA_CENTER_BIT = 5;
    // Maximum value
    private static final long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    // The displacement to the left
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;
    private final int dataCenterId;
    private final int machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private SnowFlakeUtils() throws SnowFlakeException {
        this.dataCenterId = PropertyUtils.getInt(Constants.SNOW_FLAKE_DATA_CENTER_ID, 1);
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenterId can't be greater than %d or less than 0", MAX_DATA_CENTER_NUM));
        }
        try {
            this.machineId = Math.abs(Objects.hash(InetAddress.getLocalHost().getHostName())) % 32;
        } catch (UnknownHostException e) {
            throw new SnowFlakeException(e.getMessage());
        }
    }

    private static SnowFlakeUtils instance = null;

    public static synchronized SnowFlakeUtils getInstance() throws Exception {
        if (instance == null) {
            instance = new SnowFlakeUtils();
        }
        return instance;
    }

    public synchronized long nextId() {
        long currStmp = nowTimestamp();
        if (currStmp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStmp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currStmp;
        return (currStmp - START_TIMESTAMP) << TIMESTAMP_LEFT
                | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT
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

    public static class SnowFlakeException extends Exception {
        public SnowFlakeException(String message) {
            super(message);
        }
    }
}
