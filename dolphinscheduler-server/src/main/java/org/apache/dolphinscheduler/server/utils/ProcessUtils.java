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
package org.apache.dolphinscheduler.server.utils;

import java.nio.charset.StandardCharsets;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  mainly used to get the start command line of a process
 */
public class ProcessUtils {
  /**
   * logger
   */
  private final static Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

  /**
   * build command line characters
   * @param commandList command list
   * @return command
   * @throws IOException io exception
   */
  public static String buildCommandStr(List<String> commandList) throws IOException {
    String cmdstr;
    String[] cmd = commandList.toArray(new String[commandList.size()]);
    SecurityManager security = System.getSecurityManager();
    boolean allowAmbiguousCommands = false;
    if (security == null) {
      allowAmbiguousCommands = true;
      String value = System.getProperty("jdk.lang.Process.allowAmbiguousCommands");
      if (value != null) {
        allowAmbiguousCommands = !"false".equalsIgnoreCase(value);
      }
    }
    if (allowAmbiguousCommands) {

      String executablePath = new File(cmd[0]).getPath();

      if (needsEscaping(VERIFICATION_LEGACY, executablePath)) {
        executablePath = quoteString(executablePath);
      }

      cmdstr = createCommandLine(
              VERIFICATION_LEGACY, executablePath, cmd);
    } else {
      String executablePath;
      try {
        executablePath = getExecutablePath(cmd[0]);
      } catch (IllegalArgumentException e) {

        StringBuilder join = new StringBuilder();
        for (String s : cmd) {
          join.append(s).append(' ');
        }

        cmd = getTokensFromCommand(join.toString());
        executablePath = getExecutablePath(cmd[0]);

        // Check new executable name once more
        if (security != null) {
          security.checkExec(executablePath);
        }
      }


      cmdstr = createCommandLine(

              isShellFile(executablePath) ? VERIFICATION_CMD_BAT : VERIFICATION_WIN32, quoteString(executablePath), cmd);
    }
    return cmdstr;
  }

  /**
   * get executable path
   *
   * @param path path
   * @return executable path
   * @throws IOException io exception
   */
  private static String getExecutablePath(String path) throws IOException {
    boolean pathIsQuoted = isQuoted(true, path, "Executable name has embedded quote, split the arguments");

    File fileToRun = new File(pathIsQuoted ? path.substring(1, path.length() - 1) : path);
    return fileToRun.getPath();
  }

  /**
   * whether is shell file
   *
   * @param executablePath executable path
   * @return true if endsWith .CMD or .BAT
   */
  private static boolean isShellFile(String executablePath) {
    String upPath = executablePath.toUpperCase();
    return (upPath.endsWith(".CMD") || upPath.endsWith(".BAT"));
  }

  /**
   * quote string
   *
   * @param arg argument
   * @return format arg
   */
  private static String quoteString(String arg) {
    StringBuilder argbuf = new StringBuilder(arg.length() + 2);
    return argbuf.append('"').append(arg).append('"').toString();
  }

  /**
   * get tokens from command
   *
   * @param command command
   * @return token string array
   */
  private static String[] getTokensFromCommand(String command) {
    ArrayList<String> matchList = new ArrayList<>(8);
    Matcher regexMatcher = LazyPattern.PATTERN.matcher(command);
    while (regexMatcher.find()) {
      matchList.add(regexMatcher.group());
    }
    return matchList.toArray(new String[matchList.size()]);
  }

  /**
   * Lazy Pattern
   */
  private static class LazyPattern {
    // Escape-support version:
    // "(\")((?:\\\\\\1|.)+?)\\1|([^\\s\"]+)";
    private static final Pattern PATTERN = Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
  }

  /**
   * verification cmd bat
   */
  private static final int VERIFICATION_CMD_BAT = 0;

  /**
   * verification win32
   */
  private static final int VERIFICATION_WIN32 = 1;

  /**
   * verification legacy
   */
  private static final int VERIFICATION_LEGACY = 2;

  /**
   * escape verification
   */
  private static final char[][] ESCAPE_VERIFICATION = {{' ', '\t', '<', '>', '&', '|', '^'},

          {' ', '\t', '<', '>'}, {' ', '\t'}};

  /**
   * matcher
   */
  private static Matcher matcher;

  /**
   * create command line
   * @param verificationType  verification type
   * @param executablePath    executable path
   * @param cmd               cmd
   * @return command line
   */
  private static String createCommandLine(int verificationType, final String executablePath, final String[] cmd) {
    StringBuilder cmdbuf = new StringBuilder(80);

    cmdbuf.append(executablePath);

    for (int i = 1; i < cmd.length; ++i) {
      cmdbuf.append(' ');
      String s = cmd[i];
      if (needsEscaping(verificationType, s)) {
        cmdbuf.append('"').append(s);

        if ((verificationType != VERIFICATION_CMD_BAT) && s.endsWith("\\")) {
          cmdbuf.append('\\');
        }
        cmdbuf.append('"');
      } else {
        cmdbuf.append(s);
      }
    }
    return cmdbuf.toString();
  }

  /**
   * whether is quoted
   * @param noQuotesInside
   * @param arg
   * @param errorMessage
   * @return boolean
   */
  private static boolean isQuoted(boolean noQuotesInside, String arg, String errorMessage) {
    int lastPos = arg.length() - 1;
    if (lastPos >= 1 && arg.charAt(0) == '"' && arg.charAt(lastPos) == '"') {
      // The argument has already been quoted.
      if (noQuotesInside) {
        if (arg.indexOf('"', 1) != lastPos) {
          // There is ["] inside.
          throw new IllegalArgumentException(errorMessage);
        }
      }
      return true;
    }
    if (noQuotesInside) {
      if (arg.indexOf('"') >= 0) {
        // There is ["] inside.
        throw new IllegalArgumentException(errorMessage);
      }
    }
    return false;
  }

  /**
   * whether needs escaping
   *
   * @param verificationType  verification type
   * @param arg               arg
   * @return boolean
   */
  private static boolean needsEscaping(int verificationType, String arg) {

    boolean argIsQuoted = isQuoted((verificationType == VERIFICATION_CMD_BAT), arg, "Argument has embedded quote, use the explicit CMD.EXE call.");

    if (!argIsQuoted) {
      char[] testEscape = ESCAPE_VERIFICATION[verificationType];
      for (int i = 0; i < testEscape.length; ++i) {
        if (arg.indexOf(testEscape[i]) >= 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * kill yarn application
   *
   * @param appIds      app id list
   * @param logger      logger
   * @param tenantCode  tenant code
   * @param executePath     execute path
   * @throws IOException io exception
   */
  public static void cancelApplication(List<String> appIds, Logger logger, String tenantCode,String executePath)
          throws IOException {
    if (appIds.size() > 0) {
      String appid = appIds.get(appIds.size() - 1);
      String commandFile = String
              .format("%s/%s.kill", executePath, appid);
      String cmd = "yarn application -kill " + appid;
      try {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/sh\n");
        sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
        sb.append("cd $BASEDIR\n");
        if (CommonUtils.getSystemEnvPath() != null) {
          sb.append("source " + CommonUtils.getSystemEnvPath() + "\n");
        }
        sb.append("\n\n");
        sb.append(cmd);

        File f = new File(commandFile);

        if (!f.exists()) {
          FileUtils.writeStringToFile(new File(commandFile), sb.toString(), StandardCharsets.UTF_8);
        }

        String runCmd = "sh " + commandFile;
        if (StringUtils.isNotEmpty(tenantCode)) {
          runCmd = "sudo -u " + tenantCode + " " + runCmd;
        }

        logger.info("kill cmd:{}", runCmd);

        Runtime.getRuntime().exec(runCmd);
      } catch (Exception e) {
        logger.error("kill application error", e);
      }
    }
  }

  /**
   * kill tasks according to different task types
   *
   * @param taskExecutionContext  taskExecutionContext
   */
  public static void kill(TaskExecutionContext taskExecutionContext) {
    try {
      int processId = taskExecutionContext.getProcessId();
      if(processId == 0 ){
        logger.error("process kill failed, process id :{}, task id:{}",
                processId, taskExecutionContext.getTaskInstanceId());
        return ;
      }

      String cmd = String.format("sudo kill -9 %s", getPidsStr(processId));

      logger.info("process id:{}, cmd:{}", processId, cmd);

      OSUtils.exeCmd(cmd);

      // find log and kill yarn job
      killYarnJob(taskExecutionContext);

    } catch (Exception e) {
      logger.error("kill task failed", e);
    }
  }

  /**
   * get pids str
   *
   * @param processId process id
   * @return pids
   * @throws Exception exception
   */
  public static String getPidsStr(int processId)throws Exception{
    StringBuilder sb = new StringBuilder();
    Matcher mat;
    // pstree pid get sub pids
    if (OSUtils.isMacOS()) {
      String pids = OSUtils.exeCmd("pstree -sp " + processId);
      mat = Pattern.compile("-[+|-]-\\s(\\d+)").matcher(pids);
    } else {
      String pids = OSUtils.exeCmd("pstree -p " + processId);
      mat = Pattern.compile("(\\d+)").matcher(pids);
    }

    while (mat.find()){
      sb.append(mat.group(1)).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * find logs and kill yarn tasks
   *
   * @param taskExecutionContext  taskExecutionContext
   */
  public static void killYarnJob(TaskExecutionContext taskExecutionContext) {
    try {
      Thread.sleep(Constants.SLEEP_TIME_MILLIS);
      LogClientService logClient = null;
      String log = null;
      try {
        logClient = new LogClientService();
        log = logClient.viewLog(Host.of(taskExecutionContext.getHost()).getIp(),
                Constants.RPC_PORT,
                taskExecutionContext.getLogPath());
      } finally {
        if(logClient != null){
          logClient.close();
        }
      }
      if (StringUtils.isNotEmpty(log)) {
        List<String> appIds = LoggerUtils.getAppIds(log, logger);
        String workerDir = taskExecutionContext.getExecutePath();
        if (StringUtils.isEmpty(workerDir)) {
          logger.error("task instance work dir is empty");
          throw new RuntimeException("task instance work dir is empty");
        }
        if (appIds.size() > 0) {
          cancelApplication(appIds, logger, taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());
        }
      }

    } catch (Exception e) {
      logger.error("kill yarn job failure",e);
    }
  }
}
