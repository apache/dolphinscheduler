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

import org.apache.commons.configuration.Configuration;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.shell.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * os utils
 *
 */
public class OSUtils {

  private static final Logger logger = LoggerFactory.getLogger(OSUtils.class);

  public static final ThreadLocal<Logger> taskLoggerThreadLocal = new ThreadLocal<>();

  private static final SystemInfo SI = new SystemInfo();
  public static final String TWO_DECIMAL = "0.00";

  private static HardwareAbstractionLayer hal = SI.getHardware();

  private OSUtils() {}


  /**
   * get memory usage
   * Keep 2 decimal
   * @return  percent %
   */
  public static double memoryUsage() {
    GlobalMemory memory = hal.getMemory();
    double memoryUsage = (memory.getTotal() - memory.getAvailable() - memory.getSwapUsed()) * 0.1 / memory.getTotal() * 10;

    DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
    df.setRoundingMode(RoundingMode.HALF_UP);
    return Double.parseDouble(df.format(memoryUsage));
  }


  /**
   * get available physical memory size
   *
   * Keep 2 decimal
   * @return  available Physical Memory Size, unit: G
   */
  public static double availablePhysicalMemorySize() {
    GlobalMemory memory = hal.getMemory();
    double  availablePhysicalMemorySize = (memory.getAvailable() + memory.getSwapUsed()) /1024.0/1024/1024;

    DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
    df.setRoundingMode(RoundingMode.HALF_UP);
    return Double.parseDouble(df.format(availablePhysicalMemorySize));

  }

  /**
   * get total physical memory size
   *
   * Keep 2 decimal
   * @return  available Physical Memory Size, unit: G
   */
  public static double totalMemorySize() {
    GlobalMemory memory = hal.getMemory();
    double  availablePhysicalMemorySize = memory.getTotal() /1024.0/1024/1024;

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
    double loadAverage =  hal.getProcessor().getSystemLoadAverage();

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
    double cpuUsage = processor.getSystemCpuLoad();

    DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return Double.parseDouble(df.format(cpuUsage));
  }

  public static List<String> getUserList() {
    try {
      if (isMacOS()) {
        return getUserListFromMac();
      } else if (isWindows()) {
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

    try (BufferedReader bufferedReader = new BufferedReader(
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
   * @return user list
   */
  private static List<String> getUserListFromMac() throws IOException {
    String result = exeCmd("dscl . list /users");
    if (StringUtils.isNotEmpty(result)) {
      return Arrays.asList(result.split( "\n"));
    }

    return Collections.emptyList();
  }

  /**
   *  get user list from windows
   * @return user list
   * @throws IOException
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
      Pattern pattern = Pattern.compile("\\s+");
      users.addAll(Arrays.asList(pattern.split(lines[startPos])));
      startPos++;
    }

    return users;
  }

  /**
   * create user
   * @param userName user name
   * @return true if creation was successful, otherwise false
   */
  public static boolean createUser(String userName) {
    try {
      String userGroup = OSUtils.getGroup();
      if (StringUtils.isEmpty(userGroup)) {
        String errorLog = String.format("%s group does not exist for this operating system.", userGroup);
        LoggerUtils.logError(Optional.ofNullable(logger), errorLog);
        LoggerUtils.logError(Optional.ofNullable(taskLoggerThreadLocal.get()), errorLog);
        return false;
      }
      if (isMacOS()) {
        createMacUser(userName, userGroup);
      } else if (isWindows()) {
        createWindowsUser(userName, userGroup);
      } else {
        createLinuxUser(userName, userGroup);
      }
      return true;
    } catch (Exception e) {
      LoggerUtils.logError(Optional.ofNullable(logger), e);
      LoggerUtils.logError(Optional.ofNullable(taskLoggerThreadLocal.get()), e);
    }

    return false;
  }

  /**
   * create linux user
   * @param userName user name
   * @param userGroup user group
   * @throws IOException in case of an I/O error
   */
  private static void createLinuxUser(String userName, String userGroup) throws IOException {
    String infoLog1 = String.format("create linux os user : %s", userName);
    LoggerUtils.logInfo(Optional.ofNullable(logger), infoLog1);
    LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), infoLog1);

    String cmd = String.format("sudo useradd -g %s %s", userGroup, userName);
    String infoLog2 = String.format("execute cmd : %s", cmd);
    LoggerUtils.logInfo(Optional.ofNullable(logger), infoLog2);
    LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), infoLog2);
    OSUtils.exeCmd(cmd);
  }

  /**
   * create mac user (Supports Mac OSX 10.10+)
   * @param userName user name
   * @param userGroup user group
   * @throws IOException in case of an I/O error
   */
  private static void createMacUser(String userName, String userGroup) throws IOException {

    Optional<Logger> optionalLogger = Optional.ofNullable(logger);
    Optional<Logger> optionalTaskLogger = Optional.ofNullable(taskLoggerThreadLocal.get());

    String infoLog1 = String.format("create mac os user : %s", userName);
    LoggerUtils.logInfo(optionalLogger, infoLog1);
    LoggerUtils.logInfo(optionalTaskLogger, infoLog1);

    String createUserCmd = String.format("sudo sysadminctl -addUser %s -password %s", userName, userName);
    String infoLog2 = String.format("create user command : %s", createUserCmd);
    LoggerUtils.logInfo(optionalLogger, infoLog2);
    LoggerUtils.logInfo(optionalTaskLogger, infoLog2);
    OSUtils.exeCmd(createUserCmd);

    String appendGroupCmd = String.format("sudo dseditgroup -o edit -a %s -t user %s", userName, userGroup);
    String infoLog3 = String.format("append user to group : %s", appendGroupCmd);
    LoggerUtils.logInfo(optionalLogger, infoLog3);
    LoggerUtils.logInfo(optionalTaskLogger, infoLog3);
    OSUtils.exeCmd(appendGroupCmd);
  }

  /**
   * create windows user
   * @param userName user name
   * @param userGroup user group
   * @throws IOException in case of an I/O error
   */
  private static void createWindowsUser(String userName, String userGroup) throws IOException {
    String infoLog1 = String.format("create windows os user : %s", userName);
    LoggerUtils.logInfo(Optional.ofNullable(logger), infoLog1);
    LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), infoLog1);

    String userCreateCmd = String.format("net user \"%s\" /add", userName);
    String infoLog2 = String.format("execute create user command : %s", userCreateCmd);
    LoggerUtils.logInfo(Optional.ofNullable(logger), infoLog2);
    LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), infoLog2);
    OSUtils.exeCmd(userCreateCmd);

    String appendGroupCmd = String.format("net localgroup \"%s\" \"%s\" /add", userGroup, userName);
    String infoLog3 = String.format("execute append user to group : %s", appendGroupCmd);
    LoggerUtils.logInfo(Optional.ofNullable(logger), infoLog3);
    LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), infoLog3);
    OSUtils.exeCmd(appendGroupCmd);
  }

  /**
   * get system group information
   * @return system group info
   * @throws IOException errors
   */
  public static String getGroup() throws IOException {
    if (isWindows()) {
      String currentProcUserName = System.getProperty("user.name");
      String result = exeCmd(String.format("net user \"%s\"", currentProcUserName));
      String line = result.split("\n")[22];
      String group = Pattern.compile("\\s+").split(line)[1];
      if (group.charAt(0) == '*') {
        return group.substring(1);
      } else {
        return group;
      }
    } else {
      String result = exeCmd("groups");
      if (StringUtils.isNotEmpty(result)) {
        String[] groupInfo = result.split(" ");
        return groupInfo[0];
      }
    }

    return null;
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
   * @param command command
   * @return result of execute the shell
   * @throws IOException errors
   */
  public static String exeShell(String[] command) throws IOException {
    return ShellExecutor.execCommand(command);
  }

  /**
   * get process id
   * @return process id
   */
  public static int getProcessID() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
  }

  /**
   * get local host
   * @return host
   */
  public static String getHost(){
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      logger.error(e.getMessage(),e);
    }
    return null;
  }


  /**
   * whether is macOS
   * @return true if mac
   */
  public static boolean isMacOS() {
    return getOSName().startsWith("Mac");
  }


  /**
   * whether is windows
   * @return true if windows
   */
  public static boolean isWindows() {
    return getOSName().startsWith("Windows");
  }

  /**
   * get current OS name
   * @return current OS name
   */
  public static String getOSName() {
    return System.getProperty("os.name");
  }

  /**
   * check memory and cpu usage
   * @param systemCpuLoad systemCpuLoad
   * @param systemReservedMemory systemReservedMemory
   * @return check memory and cpu usage
   */
  public static Boolean checkResource(double systemCpuLoad, double systemReservedMemory){
    // system load average
    double loadAverage = OSUtils.loadAverage();
    // system available physical memory
    double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();

    if(loadAverage > systemCpuLoad || availablePhysicalMemorySize < systemReservedMemory){
      logger.warn("load is too high or availablePhysicalMemorySize(G) is too low, it's availablePhysicalMemorySize(G):{},loadAvg:{}", availablePhysicalMemorySize , loadAverage);
      return false;
    }else{
      return true;
    }
  }

  /**
   * check memory and cpu usage
   * @param conf conf
   * @param isMaster is master
   * @return check memory and cpu usage
   */
  public static Boolean checkResource(Configuration conf, Boolean isMaster){
    double systemCpuLoad;
    double systemReservedMemory;

    if(Boolean.TRUE.equals(isMaster)){
      systemCpuLoad = conf.getDouble(Constants.MASTER_MAX_CPULOAD_AVG, Constants.DEFAULT_MASTER_CPU_LOAD);
      systemReservedMemory = conf.getDouble(Constants.MASTER_RESERVED_MEMORY, Constants.DEFAULT_MASTER_RESERVED_MEMORY);
    }else{
      systemCpuLoad = conf.getDouble(Constants.WORKER_MAX_CPULOAD_AVG, Constants.DEFAULT_WORKER_CPU_LOAD);
      systemReservedMemory = conf.getDouble(Constants.WORKER_RESERVED_MEMORY, Constants.DEFAULT_WORKER_RESERVED_MEMORY);
    }
    return checkResource(systemCpuLoad,systemReservedMemory);
  }

}
