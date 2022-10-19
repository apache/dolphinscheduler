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
import org.apache.dolphinscheduler.common.shell.ShellExecutor;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * os utils
 */
public class OSUtils {

    private static final Logger logger = LoggerFactory.getLogger(OSUtils.class);

    private static final SystemInfo SI = new SystemInfo();
    public static final String TWO_DECIMAL = "0.00";

    /**
     * return -1 when the function can not get hardware env info
     * e.g {@link OSUtils#loadAverage()} {@link OSUtils#cpuUsage()}
     */
    public static final double NEGATIVE_ONE = -1;

    private static final HardwareAbstractionLayer hal = SI.getHardware();
    private static long[] prevTicks = new long[CentralProcessor.TickType.values().length];
    private static long prevTickTime = 0L;
    private static double cpuUsage = 0.0D;

    private OSUtils() {
        throw new UnsupportedOperationException("Construct OSUtils");
    }

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern PATTERN = Pattern.compile("\\s+");

    /**
     * get memory usage
     * Keep 2 decimal
     *
     * @return percent %
     */
    public static double memoryUsage() {
        GlobalMemory memory = hal.getMemory();
        double memoryUsage = (memory.getTotal() - memory.getAvailable()) * 1.0 / memory.getTotal();

        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(memoryUsage));
    }

    /**
     * get disk usage
     * Keep 2 decimal
     *
     * @return disk free size, unit: GB
     */
    public static double diskAvailable() {
        File file = new File(".");
        long freeSpace = file.getFreeSpace(); // unallocated / free disk space in bytes.

        double diskAvailable = freeSpace / 1024.0 / 1024 / 1024;

        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(diskAvailable));
    }

    /**
     * get available physical memory size
     * <p>
     * Keep 2 decimal
     *
     * @return available Physical Memory Size, unit: G
     */
    public static double availablePhysicalMemorySize() {
        GlobalMemory memory = hal.getMemory();
        double availablePhysicalMemorySize = memory.getAvailable() / 1024.0 / 1024 / 1024;

        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(availablePhysicalMemorySize));
    }

    /**
     * load average
     *
     * @return load average
     */
    public static double loadAverage() {
        double loadAverage;
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            loadAverage = osBean.getSystemLoadAverage();
        } catch (Exception e) {
            logger.error("get operation system load average exception, try another method ", e);
            loadAverage = hal.getProcessor().getSystemLoadAverage(1)[0];
            if (Double.isNaN(loadAverage)) {
                return NEGATIVE_ONE;
            }
        }
        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(loadAverage));
    }

    /**
     * get cpu usage
     *
     * @return cpu usage
     */
    public static double cpuUsage() {
        CentralProcessor processor = hal.getProcessor();

        // Check if > ~ 0.95 seconds since last tick count.
        long now = System.currentTimeMillis();
        if (now - prevTickTime > 950) {
            // Enough time has elapsed.
            cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks);
            prevTickTime = System.currentTimeMillis();
            prevTicks = processor.getSystemCpuLoadTicks();
        }

        if (Double.isNaN(cpuUsage)) {
            return NEGATIVE_ONE;
        }

        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(cpuUsage));
    }

    public static List<String> getUserList() {
        try {
            if (SystemUtils.IS_OS_MAC) {
                return getUserListFromMac();
            } else if (SystemUtils.IS_OS_WINDOWS) {
                return getUserListFromWindows();
            } else {
                return getUserListFromLinux();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * get user list from linux
     *
     * @return user list
     */
    private static List<String> getUserListFromLinux() throws IOException {
        List<String> userList = new ArrayList<>();

        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(new FileInputStream("/etc/passwd")))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(":")) {
                    String[] userInfo = line.split(":");
                    userList.add(userInfo[0]);
                }
            }
        }

        return userList;
    }

    /**
     * get user list from mac
     *
     * @return user list
     */
    private static List<String> getUserListFromMac() throws IOException {
        String result = exeCmd("dscl . list /users");
        if (!StringUtils.isEmpty(result)) {
            return Arrays.asList(result.split("\n"));
        }

        return Collections.emptyList();
    }

    /**
     * get user list from windows
     *
     * @return user list
     */
    private static List<String> getUserListFromWindows() throws IOException {
        String result = exeCmd("net user");
        String[] lines = result.split("\n");

        int startPos = 0;
        int endPos = lines.length - 2;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                continue;
            }

            int count = 0;
            if (lines[i].charAt(0) == '-') {
                for (int j = 0; j < lines[i].length(); j++) {
                    if (lines[i].charAt(i) == '-') {
                        count++;
                    }
                }
            }

            if (count == lines[i].length()) {
                startPos = i + 1;
                break;
            }
        }

        List<String> users = new ArrayList<>();
        while (startPos <= endPos) {
            users.addAll(Arrays.asList(PATTERN.split(lines[startPos])));
            startPos++;
        }

        return users;
    }

    /**
     * whether the user exists in linux
     *
     * @return boolean
     */
    public static boolean existTenantCodeInLinux(String tenantCode) {
        try {
            String result = exeCmd("id " + tenantCode);
            if (!StringUtils.isEmpty(result)) {
                return result.contains("uid=");
            }
        } catch (Exception e) {
            // because ShellExecutor method throws exception to the linux return status is not 0
            // not exist user return status is 1
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * create user
     *
     * @param userName user name
     */
    public static void createUserIfAbsent(String userName) {
        // if not exists this user, then create
        if (!getUserList().contains(userName)) {
            boolean isSuccess = createUser(userName);
            logger.info("create user {} {}", userName, isSuccess ? "success" : "fail");
        }
    }

    /**
     * create user
     *
     * @param userName user name
     * @return true if creation was successful, otherwise false
     */
    public static boolean createUser(String userName) {
        try {
            String userGroup = getGroup();
            if (StringUtils.isEmpty(userGroup)) {
                String errorLog = String.format("%s group does not exist for this operating system.", userGroup);
                logger.error(errorLog);
                return false;
            }
            if (SystemUtils.IS_OS_MAC) {
                createMacUser(userName, userGroup);
            } else if (SystemUtils.IS_OS_WINDOWS) {
                createWindowsUser(userName, userGroup);
            } else {
                createLinuxUser(userName, userGroup);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * create linux user
     *
     * @param userName user name
     * @param userGroup user group
     * @throws IOException in case of an I/O error
     */
    private static void createLinuxUser(String userName, String userGroup) throws IOException {
        logger.info("create linux os user: {}", userName);
        String cmd = String.format("sudo useradd -g %s %s", userGroup, userName);
        logger.info("execute cmd: {}", cmd);
        exeCmd(cmd);
    }

    /**
     * create mac user (Supports Mac OSX 10.10+)
     *
     * @param userName user name
     * @param userGroup user group
     * @throws IOException in case of an I/O error
     */
    private static void createMacUser(String userName, String userGroup) throws IOException {
        logger.info("create mac os user: {}", userName);

        String createUserCmd = String.format("sudo sysadminctl -addUser %s -password %s", userName, userName);
        logger.info("create user command: {}", createUserCmd);
        exeCmd(createUserCmd);

        String appendGroupCmd = String.format("sudo dseditgroup -o edit -a %s -t user %s", userName, userGroup);
        logger.info("append user to group: {}", appendGroupCmd);
        exeCmd(appendGroupCmd);
    }

    /**
     * create windows user
     *
     * @param userName user name
     * @param userGroup user group
     * @throws IOException in case of an I/O error
     */
    private static void createWindowsUser(String userName, String userGroup) throws IOException {
        logger.info("create windows os user: {}", userName);

        String userCreateCmd = String.format("net user \"%s\" /add", userName);
        logger.info("execute create user command: {}", userCreateCmd);
        exeCmd(userCreateCmd);

        String appendGroupCmd = String.format("net localgroup \"%s\" \"%s\" /add", userGroup, userName);
        logger.info("execute append user to group: {}", appendGroupCmd);
        exeCmd(appendGroupCmd);
    }

    /**
     * get system group information
     *
     * @return system group info
     * @throws IOException errors
     */
    public static String getGroup() throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            String currentProcUserName = System.getProperty("user.name");
            String result = exeCmd(String.format("net user \"%s\"", currentProcUserName));
            String line = result.split("\n")[22];
            String group = PATTERN.split(line)[1];
            if (group.charAt(0) == '*') {
                return group.substring(1);
            } else {
                return group;
            }
        } else {
            String result = exeCmd("groups");
            if (!StringUtils.isEmpty(result)) {
                String[] groupInfo = result.split(" ");
                return groupInfo[0];
            }
        }

        return null;
    }

    /**
     * get sudo command
     *
     * @param tenantCode tenantCode
     * @param command command
     * @return result of sudo execute command
     */
    public static String getSudoCmd(String tenantCode, String command) {
        if (!isSudoEnable() || StringUtils.isEmpty(tenantCode)) {
            return command;
        }
        return String.format("sudo -u %s %s", tenantCode, command);
    }

    public static boolean isSudoEnable() {
        return PropertyUtils.getBoolean(Constants.SUDO_ENABLE, true);
    }

    /**
     * Execute the corresponding command of Linux or Windows
     *
     * @param command command
     * @return result of execute command
     * @throws IOException errors
     */
    public static String exeCmd(String command) throws IOException {
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdArray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            cmdArray[i] = st.nextToken();
        }
        return exeShell(cmdArray);
    }

    /**
     * Execute the shell
     *
     * @param command command
     * @return result of execute the shell
     * @throws IOException errors
     */
    public static String exeShell(String[] command) throws IOException {
        return ShellExecutor.execCommand(command);
    }

    /**
     * get process id
     *
     * @return process id
     */
    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

    /**
     * Check memory and cpu usage is overload the given thredshod.
     *
     * @param maxCpuLoadAvg  maxCpuLoadAvg
     * @param reservedMemory reservedMemory
     * @return True, if the cpu or memory exceed the given thredshod.
     */
    public static Boolean isOverload(double maxCpuLoadAvg, double reservedMemory) {
        // system load average
        double loadAverage = loadAverage();
        // system available physical memory
        double availablePhysicalMemorySize = availablePhysicalMemorySize();
        if (loadAverage > maxCpuLoadAvg || availablePhysicalMemorySize < reservedMemory) {
            logger.warn(
                    "Current cpu load average {} is too high or available memory {}G is too low, under max.cpuLoad.avg={} and reserved.memory={}G",
                    loadAverage, availablePhysicalMemorySize, maxCpuLoadAvg, reservedMemory);
            return true;
        }
        return false;
    }

}
