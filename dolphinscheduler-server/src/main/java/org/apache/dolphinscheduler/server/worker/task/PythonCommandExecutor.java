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
package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * python command executor
 */
public class PythonCommandExecutor extends AbstractCommandExecutor {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PythonCommandExecutor.class);

    /**
     * python
     */
    public static final String PYTHON = "python";


    /**
     * constructor
     * @param logHandler    log handler
     * @param taskDir       task dir
     * @param taskAppId     task app id
     * @param taskInstId    task instance id
     * @param tenantCode    tenant code
     * @param envFile       env file
     * @param startTime     start time
     * @param timeout       timeout
     * @param logger        logger
     */
    public PythonCommandExecutor(Consumer<List<String>> logHandler,
                                 String taskDir,
                                 String taskAppId,
                                 int taskInstId,
                                 String tenantCode,
                                 String envFile,
                                 Date startTime,
                                 int timeout,
                                 Logger logger) {
        super(logHandler,taskDir,taskAppId,taskInstId,tenantCode, envFile, startTime, timeout, logger);
    }


    /**
     * build command file path
     *
     * @return command file path
     */
    @Override
    protected String buildCommandFilePath() {
        return String.format("%s/py_%s.command", taskDir, taskAppId);
    }

    /**
     * create command file if not exists
     * @param execCommand   exec command
     * @param commandFile   command file
     * @throws IOException  io exception
     */
    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        logger.info("tenantCode :{}, task dir:{}", tenantCode, taskDir);

        if (!Files.exists(Paths.get(commandFile))) {
            logger.info("generate command file:{}", commandFile);

            StringBuilder sb = new StringBuilder();
            sb.append("#-*- encoding=utf8 -*-\n");

            sb.append("\n\n");
            sb.append(execCommand);
            logger.info(sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(commandFile),
                    sb.toString(),
                    StandardCharsets.UTF_8);
        }
    }

    /**
     * get command options
     * @return command options list
     */
    @Override
    protected List<String> commandOptions() {
        // unbuffered binary stdout and stderr
        return Collections.singletonList("-u");
    }

    /**
     * get python home
     * @return python home
     */
    @Override
    protected String commandInterpreter() {
        String pythonHome = getPythonHome(envFile);
        if (StringUtils.isEmpty(pythonHome)){
            return PYTHON;
        }
        return pythonHome;
    }

    /**
     * check find yarn application id
     * @param line line
     * @return boolean
     */
    @Override
    protected boolean checkFindApp(String line) {
        return true;
    }


    /**
     *  get the absolute path of the Python command
     *  note :
     *  common.properties
     *  PYTHON_HOME configured under common.properties is Python absolute path, not PYTHON_HOME itself
     *
     *  for example :
     *  your PYTHON_HOM is /opt/python3.7/
     *  you must set PYTHON_HOME is /opt/python3.7/python under nder common.properties
     *  dolphinscheduler.env.path file.
     *
     * @param envPath env path
     * @return python home
     */
    private static String getPythonHome(String envPath){
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(envPath)));
            String line;
            while ((line = br.readLine()) != null){
                if (line.contains(Constants.PYTHON_HOME)){
                    sb.append(line);
                    break;
                }
            }
            String result = sb.toString();
            if (org.apache.commons.lang.StringUtils.isEmpty(result)){
                return null;
            }
            String[] arrs = result.split(Constants.EQUAL_SIGN);
            if (arrs.length == 2){
                return arrs[1];
            }

        }catch (IOException e){
            logger.error("read file failure",e);
        }finally {
            try {
                if (br != null){
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return null;
    }

}
