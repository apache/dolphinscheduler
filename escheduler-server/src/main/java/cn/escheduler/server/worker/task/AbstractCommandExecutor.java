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
package cn.escheduler.server.worker.task;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.thread.ThreadUtils;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.LoggerUtils;
import cn.escheduler.server.utils.ProcessUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * abstract command executor
 */
public abstract class AbstractCommandExecutor {
    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(Constants.APPLICATION_REGEX);

    /**
     *  process
     */
    private Process process;

    /**
     *  log handler
     */
    protected Consumer<List<String>> logHandler;

    /**
     *  task dir
     */
    protected final String taskDir;

    /**
     *  task appId
     */
    protected final String taskAppId;

    /**
     *  task appId
     */
    protected final int taskInstId;

    /**
     *  tenant code , execute task linux user
     */
    protected final String tenantCode;

    /**
     *  env file
     */
    protected final String envFile;

    /**
     *  start time
     */
    protected final Date startTime;

    /**
     *  timeout
     */
    protected int timeout;

    /**
     *  logger
     */
    protected Logger logger;

    /**
     *  log list
     */
    protected final List<String> logBuffer;


    public AbstractCommandExecutor(Consumer<List<String>> logHandler,
                                   String taskDir, String taskAppId,int taskInstId,String tenantCode, String envFile,
                                   Date startTime, int timeout, Logger logger){
        this.logHandler = logHandler;
        this.taskDir = taskDir;
        this.taskAppId = taskAppId;
        this.taskInstId = taskInstId;
        this.tenantCode = tenantCode;
        this.envFile = envFile;
        this.startTime = startTime;
        this.timeout = timeout;
        this.logger = logger;
        this.logBuffer = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * task specific execution logic
     *
     * @param execCommand
     * @param processDao
     * @return
     */
    public int run(String execCommand, ProcessDao processDao) {
        int exitStatusCode;

        try {
            if (StringUtils.isEmpty(execCommand)) {
                exitStatusCode = 0;
                return exitStatusCode;
            }

            String commandFilePath = buildCommandFilePath();

            // create command file if not exists
            createCommandFileIfNotExists(execCommand, commandFilePath);

            //build process
            buildProcess(commandFilePath);

            // parse process output
            parseProcessOutput(process);

            // get process id
            int pid = getProcessId(process);

            // task instance id
            int taskInstId = Integer.parseInt(taskAppId.split("_")[2]);

            processDao.updatePidByTaskInstId(taskInstId, pid);

            logger.info("process start, process id is: {}", pid);

            // if timeout occurs, exit directly
            long remainTime = getRemaintime();

            // waiting for the run to finish
            boolean status = process.waitFor(remainTime, TimeUnit.SECONDS);

            if (status) {
                exitStatusCode = process.exitValue();
                logger.info("process has exited, work dir:{}, pid:{} ,exitStatusCode:{}", taskDir, pid,exitStatusCode);
                //update process state to db
                exitStatusCode = updateState(processDao, exitStatusCode, pid, taskInstId);

            } else {
                TaskInstance taskInstance = processDao.findTaskInstanceById(taskInstId);
                if (taskInstance == null) {
                    logger.error("task instance id:{} not exist", taskInstId);
                } else {
                    ProcessUtils.kill(taskInstance);
                }
                exitStatusCode = -1;
                logger.warn("process timeout, work dir:{}, pid:{}", taskDir, pid);
            }

        } catch (InterruptedException e) {
            exitStatusCode = -1;
            logger.error(String.format("interrupt exception: {}, task may be cancelled or killed",e.getMessage()), e);
            throw new RuntimeException("interrupt exception. exitCode is :  " + exitStatusCode);
        } catch (Exception e) {
            exitStatusCode = -1;
            logger.error(e.getMessage(), e);
            throw new RuntimeException("process error . exitCode is :  " + exitStatusCode);
        }

        return exitStatusCode;
    }

    /**
     * build process
     *
     * @param commandFile
     * @throws IOException
     */
    private void buildProcess(String commandFile) throws IOException {
        //init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(taskDir));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);
        // setting up user to run commands
        processBuilder.command("sudo", "-u", tenantCode, commandType(), commandFile);

        process = processBuilder.start();

        // print command
        printCommand(processBuilder);
    }

    /**
     * update process state to db
     *
     * @param processDao
     * @param exitStatusCode
     * @param pid
     * @param taskInstId
     * @return
     */
    private int updateState(ProcessDao processDao, int exitStatusCode, int pid, int taskInstId) {
        //get yarn state by log
        if (exitStatusCode != 0) {
            TaskInstance taskInstance = processDao.findTaskInstanceById(taskInstId);
            logger.info("process id is {}", pid);

            List<String> appIds = getAppLinks(taskInstance.getLogPath());
            if (appIds.size() > 0) {
                String appUrl = String.join(Constants.COMMA, appIds);
                logger.info("yarn log url:{}",appUrl);
                processDao.updatePidByTaskInstId(taskInstId, pid, appUrl);
            }

            // check if all operations are completed
            if (!isSuccessOfYarnState(appIds)) {
                exitStatusCode = -1;
            }
        }
        return exitStatusCode;
    }


    /**
     *  cancel python task
     */
    public void cancelApplication() throws Exception {
        if (process == null) {
            return;
        }

        // clear log
        clear();

        int processId = getProcessId(process);

        logger.info("cancel process: {}", processId);

        // kill , waiting for completion
        boolean killed = softKill(processId);

        if (!killed) {
            // hard kill
            hardKill(processId);

            // destory
            process.destroy();

            process = null;
        }
    }

    /**
     *  soft kill
     * @param processId
     * @return
     * @throws InterruptedException
     */
    private boolean softKill(int processId) {

        if (processId != 0 && process.isAlive()) {
            try {
                // sudo -u user command to run command
                String cmd = String.format("sudo kill %d", processId);

                logger.info("soft kill task:{}, process id:{}, cmd:{}", taskAppId, processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.info("kill attempt failed." + e.getMessage(), e);
            }
        }

        return process.isAlive();
    }

    /**
     *  hard kill
     * @param processId
     */
    private void hardKill(int processId) {
        if (processId != 0 && process.isAlive()) {
            try {
                String cmd = String.format("sudo kill -9 %d", processId);

                logger.info("hard kill task:{}, process id:{}, cmd:{}", taskAppId, processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.error("kill attempt failed." + e.getMessage(), e);
            }
        }
    }

    /**
     *  print command
     * @param processBuilder
     */
    private void printCommand(ProcessBuilder processBuilder) {
        String cmdStr;

        try {
            cmdStr = ProcessUtils.buildCommandStr(processBuilder.command());
            logger.info("task run command:\n{}", cmdStr);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     *  clear
     */
    private void clear() {
        if (!logBuffer.isEmpty()) {
            // log handle
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
    }

    /**
     * get the standard output of the process
     */
    private void parseProcessOutput(Process process) {
        String threadLoggerInfoName = String.format(LoggerUtils.TASK_LOGGER_THREAD_NAME + "-%s", taskAppId);
        ExecutorService parseProcessOutputExecutorService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName);
        parseProcessOutputExecutorService.submit(new Runnable(){
            @Override
            public void run() {
                BufferedReader inReader = null;

                try {
                    inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;

                    long lastFlushTime = System.currentTimeMillis();

                    while ((line = inReader.readLine()) != null) {
                        logBuffer.add(line);
                        lastFlushTime = flush(lastFlushTime);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                } finally {
                    clear();
                    close(inReader);
                }
            }
        });
        parseProcessOutputExecutorService.shutdown();
    }

    public int getPid() {
        return getProcessId(process);
    }

    /**
     * check yarn state
     *
     * @param appIds
     * @return
     */
    public boolean isSuccessOfYarnState(List<String> appIds) {

        boolean result = true;
        try {
            for (String appId : appIds) {
                while(true){
                    ExecutionStatus applicationStatus = HadoopUtils.getInstance().getApplicationStatus(appId);
                    logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                    if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                            applicationStatus.equals(ExecutionStatus.KILL)) {
                        return false;
                    }

                    if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                        break;
                    }
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
           }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed : " + e.getMessage(), appIds.toString()),e);
            result = false;
        }
        return result;

    }

    /**
     *  get app links
     * @param fileName
     * @return
     */
    private List<String> getAppLinks(String fileName) {
        List<String> logs = convertFile2List(fileName);

        List<String> appIds = new ArrayList<String>();
        /**
         * analysis log，get submited yarn application id
         */
        for (String log : logs) {

            String appId = findAppId(log);
            if (StringUtils.isNotEmpty(appId) && !appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    /**
     *  convert file to list
     * @param filename
     * @return
     */
    private List<String> convertFile2List(String filename) {
        List lineList = new ArrayList<String>(100);
        File file=new File(filename);

        if (!file.exists()){
            return lineList;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ",filename),e);
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }

        }
        return lineList;
    }

    /**
     *  find app id
     *
     * @return appid
     */
    private String findAppId(String line) {
        Matcher matcher = APPLICATION_REGEX.matcher(line);

        if (matcher.find() && checkFindApp(line)) {
            return matcher.group();
        }

        return null;
    }


    /**
     * get remain time（s）
     *
     * @return
     */
    private long getRemaintime() {
        long usedTime = (System.currentTimeMillis() - startTime.getTime()) / 1000;
        long remainTime = timeout - usedTime;

        if (remainTime < 0) {
            throw new RuntimeException("task execution time out");
        }

        return remainTime;
    }

    /**
     * get process id
     *
     * @param process
     * @return
     */
    private int getProcessId(Process process) {
        int processId = 0;

        try {
            Field f = process.getClass().getDeclaredField(Constants.PID);
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return processId;
    }

    /**
     * when log buffer siz or flush time reach condition , then flush
     *
     * @param lastFlushTime  last flush time
     * @return
     */
    private long flush(long lastFlushTime) {
        long now = System.currentTimeMillis();

        /**
         * when log buffer siz or flush time reach condition , then flush
         */
        if (logBuffer.size() >= Constants.defaultLogRowsNum  || now - lastFlushTime > Constants.defaultLogFlushInterval) {
            lastFlushTime = now;
            /** log handle */
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
        return lastFlushTime;
    }

    /**
     * close buffer reader
     *
     * @param inReader
     */
    private void close(BufferedReader inReader) {
        if (inReader != null) {
            try {
                inReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    protected abstract String buildCommandFilePath();
    protected abstract String commandType();
    protected abstract boolean checkFindApp(String line);
    protected abstract void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException;
}
