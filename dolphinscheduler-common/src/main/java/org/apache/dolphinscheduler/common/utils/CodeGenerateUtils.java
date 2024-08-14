/**
 * Copyright 2010-2012 Twitter, Inc.
 */

package org.apache.dolphinscheduler.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * Rewriting based on Twitter snowflake algorithm
 */
@Slf4j
public class CodeGenerateUtils {

    private static final CodeGenerator codeGenerator;

    static {
        try {
            codeGenerator = new CodeGenerator(InetAddress.getLocalHost().getHostName() + "-" + OSUtils.getProcessID());
        } catch (UnknownHostException e) {
            throw new CodeGenerateException(e.getMessage());
        }
    }

    public static long genCode() throws CodeGenerateException {
        return codeGenerator.genCode();
    }

    public static class CodeGenerator {

        // start timestamp
        private static final long START_TIMESTAMP = 1609430400000L; // 2021-01-01 00:00:00
        // Each machine generates 32 in the same millisecond
        private static final long LOW_DIGIT_BIT = 5L;
        private static final long MACHINE_BIT = 5L;
        private static final long MAX_LOW_DIGIT = ~(-1L << LOW_DIGIT_BIT);
        // The displacement to the left
        private static final long HIGH_DIGIT_LEFT = LOW_DIGIT_BIT + MACHINE_BIT;
        public final long machineHash;
        private long lowDigit = 0L;
        private long recordMillisecond = -1L;

        private static final long SYSTEM_TIMESTAMP = System.currentTimeMillis();
        private static final long SYSTEM_NANOTIME = System.nanoTime();

        public CodeGenerator(String appName) {
            this.machineHash = Math.abs(Objects.hash(appName)) % (1 << MACHINE_BIT);
        }

        public synchronized long genCode() throws CodeGenerateException {
            long nowtMillisecond = systemMillisecond();
            if (nowtMillisecond < recordMillisecond) {
                throw new CodeGenerateException("New code exception because time is set back.");
            }
            if (nowtMillisecond == recordMillisecond) {
                lowDigit = (lowDigit + 1) & MAX_LOW_DIGIT;
                if (lowDigit == 0L) {
                    while (nowtMillisecond <= recordMillisecond) {
                        nowtMillisecond = systemMillisecond();
                    }
                }
            } else {
                lowDigit = 0L;
            }
            recordMillisecond = nowtMillisecond;
            return (nowtMillisecond - START_TIMESTAMP) << HIGH_DIGIT_LEFT | machineHash << LOW_DIGIT_BIT | lowDigit;
        }

        private long systemMillisecond() {
            return SYSTEM_TIMESTAMP + (System.nanoTime() - SYSTEM_NANOTIME) / 1000000;
        }
    }

    public static class CodeGenerateException extends RuntimeException {

        public CodeGenerateException(String message) {
            super(message);
        }
    }
}
