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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.util.OSUtils;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProcessUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSATTERN = Pattern.compile("(\\d+)");

    /**
     * kill tasks according to different task types.
     */
    public static void kill(TaskRequest request) {
        try {
            int processId = request.getProcessId();
            if (processId == 0) {
                logger.error("process kill failed, process id :{}, task id:{}",
                        processId, request.getTaskInstanceId());
                return;
            }

            String cmd = String.format("kill -9 %s", getPidsStr(processId));
            cmd = OSUtils.getSudoCmd(request.getTenantCode(), cmd);
            logger.info("process id:{}, cmd:{}", processId, cmd);

            OSUtils.exeCmd(cmd);
        } catch (Exception e) {
            logger.error("kill task failed", e);
        }
    }

    /**
     * get pids str.
     *
     * @param processId process id
     * @return pids pid String
     * @throws Exception exception
     */
    public static String getPidsStr(int processId) throws Exception {
        StringBuilder sb = new StringBuilder();
        Matcher mat = null;
        // pstree pid get sub pids
        if (OSUtils.isMacOS()) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
            if (null != pids) {
                mat = MACPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            mat = WINDOWSATTERN.matcher(pids);
        }

        if (null != mat) {
            while (mat.find()) {
                sb.append(mat.group(1)).append(" ");
            }
        }

        return sb.toString().trim();
    }

}
