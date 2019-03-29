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

import cn.escheduler.common.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * python command executor
 */
public class PythonCommandExecutor extends AbstractCommandExecutor {

    public static final String PYTHON = "python";



    public PythonCommandExecutor(Consumer<List<String>> logHandler,
                                 String taskDir, String taskAppId, String tenantCode, String envFile,
                                 Date startTime, int timeout, Logger logger) {
        super(logHandler,taskDir,taskAppId, tenantCode, envFile, startTime, timeout, logger);
    }


    /**
     * build command file path
     *
     * @return
     */
    @Override
    protected String buildCommandFilePath() {
        return String.format("%s/py_%s.command", taskDir, taskAppId);
    }

    /**
     * create command file if not exists
     *
     * @param commandFile
     * @throws IOException
     */
    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        logger.info("proxy user:{}, work dir:{}", tenantCode, taskDir);

        if (!Files.exists(Paths.get(commandFile))) {
            logger.info("generate command file:{}", commandFile);

            StringBuilder sb = new StringBuilder(200);
            sb.append("#-*- encoding=utf8 -*-\n");
            sb.append("import os,sys\n");
            sb.append("BASEDIR = os.path.dirname(os.path.realpath(__file__))\n");
            sb.append("os.chdir(BASEDIR)\n");

            if (StringUtils.isNotEmpty(envFile)) {
                String[] envArray = envFile.split("\\.");
                if(envArray.length == 2){
                    String path = envArray[0];
                    logger.info("path:"+path);
                    int index =  path.lastIndexOf("/");
                    sb.append(String.format("sys.path.append('%s')\n",path.substring(0,index)));
                    sb.append(String.format("import %s\n",path.substring(index+1)));
                }
            }

            sb.append("\n\n");
            sb.append(String.format("import py_%s_node\n",taskAppId));
            logger.info(sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(commandFile), sb.toString(), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected String commandType() {
        return PYTHON;
    }

    @Override
    protected boolean checkShowLog(String line) {
        return true;
    }

    @Override
    protected boolean checkFindApp(String line) {
        return true;
    }

}
