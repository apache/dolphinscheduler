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

package org.apache.dolphinscheduler.plugin.task.api.utils;


import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static final String DATA_BASEDIR = PropertyUtils.getString(TaskConstants.DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

    protected CommonUtils() {
        throw new UnsupportedOperationException("Construct CommonUtils");
    }

    /**
     * @return get the path of system environment variables
     */
    public static String getSystemEnvPath() {
        String envPath = PropertyUtils.getString(TaskConstants.DOLPHINSCHEDULER_ENV_PATH);
        if (StringUtils.isEmpty(envPath)) {
            URL envDefaultPath = CommonUtils.class.getClassLoader().getResource(TaskConstants.ENV_PATH);

            if (envDefaultPath != null) {
                envPath = envDefaultPath.getPath();
                logger.debug("env path :{}", envPath);
            } else {
                envPath = TaskConstants.ETC_PROFILE_PATH;
            }
        }

        return envPath;
    }

    /**
     * directory of process execution
     *
     * @param projectCode project code
     * @param processDefineCode process definition Code
     * @param processDefineVersion process definition version
     * @param processInstanceId process instance id
     * @param taskInstanceId task instance id
     * @return directory of process execution
     */
    public static String getProcessExecDir(long projectCode, long processDefineCode, int processDefineVersion, int processInstanceId, int taskInstanceId) {
        String fileName = String.format("%s/exec/process/%d/%s/%d/%d", DATA_BASEDIR,
            projectCode, processDefineCode + "_" + processDefineVersion, processInstanceId, taskInstanceId);
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * create directory if absent
     *
     * @param execLocalPath execute local path
     * @throws IOException errors
     */
    public static void createWorkDirIfAbsent(String execLocalPath) throws IOException {
        //if work dir exists, first delete
        File execLocalPathFile = new File(execLocalPath);

        if (execLocalPathFile.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(execLocalPathFile);
            } catch (Exception ex) {
                if (ex instanceof NoSuchFileException || ex.getCause() instanceof NoSuchFileException) {
                    // this file is already be deleted.
                } else {
                    throw ex;
                }
            }
        }

        //create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        String mkdirLog = "create dir success " + execLocalPath;
        logger.info(mkdirLog);
    }
}
