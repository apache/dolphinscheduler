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
package cn.escheduler.common.utils;

import cn.escheduler.common.Constants;
import cn.escheduler.common.shell.ShellExecutor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;

/**
 * os utils
 *
 */
public class OSUtils {

  private static final Logger logger = LoggerFactory.getLogger(OSUtils.class);

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
   * @return
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
   * @return
   */
  public static double cpuUsage() {
    CentralProcessor processor = hal.getProcessor();
    double cpuUsage = processor.getSystemCpuLoad();

    DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
    df.setRoundingMode(RoundingMode.HALF_UP);

    return Double.parseDouble(df.format(cpuUsage));
  }


  /**
   * get user list
   *
   * @return
   */
  public static List<String> getUserList() {
    List<String> userList = new ArrayList<>();
    BufferedReader bufferedReader = null;

    try {
      bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/passwd")));
      String line;

      while ((line = bufferedReader.readLine()) != null) {
        if (line.contains(":")) {
          String[] userInfo = line.split(":");
          userList.add(userInfo[0]);
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      try {
        bufferedReader.close();
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }

    return userList;
  }

  /**
   * get system group information
   * @return
   * @throws IOException
   */
  public static String getGroup() throws IOException {
    String result = exeCmd("groups");

    if (StringUtils.isNotEmpty(result)) {
      String[] groupInfo = StringUtils.split(result);
      return groupInfo[0];
    }

    return null;
  }

  /**
   * Execute the corresponding command of Linux or Windows
   *
   * @param command
   * @return
   * @throws IOException
   */
  public static String exeCmd(String command) throws IOException {
    BufferedReader br = null;

    try {
      Process p = Runtime.getRuntime().exec(command);
      br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      StringBuilder sb = new StringBuilder();

      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }

      return sb.toString();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Execute the shell
   * @param command
   * @return
   * @throws IOException
   */
  public static String exeShell(String command) throws IOException {
    return ShellExecutor.execCommand(command);
  }

  /**
   * get process id
   * @return
   */
  public static int getProcessID() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
  }

  /**
   * get local host
   * @return
   */
  public static String getHost(){
    try {
      String hostIP = System.getenv("DS_HOST_IP");
      if (StringUtils.isNotBlank(hostIP)) {
        return hostIP;
      } else {
        return InetAddress.getLocalHost().getHostAddress();
      }
    } catch (UnknownHostException e) {
      logger.error(e.getMessage(),e);
    }
    return null;
  }


  /**
   * whether is macOS
   */
  public static boolean isMacOS() {
    String os = System.getProperty("os.name");
    return os.startsWith("Mac");
  }


  /**
   * whether is windows
   */
  public static boolean isWindows() {
    String os = System.getProperty("os.name");
    return os.startsWith("Windows");
  }


  /**
   * check memory and cpu usage
   * @param conf
   * @return
   */
  public static Boolean checkResource(Configuration conf, Boolean isMaster){
    double systemCpuLoad;
    double systemReservedMemory;

    if(isMaster){
      systemCpuLoad = conf.getDouble(Constants.MASTER_MAX_CPULOAD_AVG, Constants.defaultMasterCpuLoad);
      systemReservedMemory = conf.getDouble(Constants.MASTER_RESERVED_MEMORY, Constants.defaultMasterReservedMemory);
    }else{
      systemCpuLoad = conf.getDouble(Constants.WORKER_MAX_CPULOAD_AVG, Constants.defaultWorkerCpuLoad);
      systemReservedMemory = conf.getDouble(Constants.WORKER_RESERVED_MEMORY, Constants.defaultWorkerReservedMemory);
    }

    // judging usage
    double loadAverage = OSUtils.loadAverage();
    //
    double availablePhysicalMemorySize = OSUtils.availablePhysicalMemorySize();

    if(loadAverage > systemCpuLoad || availablePhysicalMemorySize < systemReservedMemory){
      logger.warn("load or availablePhysicalMemorySize(G) is too high, it's availablePhysicalMemorySize(G):{},loadAvg:{}", availablePhysicalMemorySize , loadAverage);
      return false;
    }else{
      return true;
    }
  }

}
