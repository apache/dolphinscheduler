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

package org.apache.dolphinscheduler.server.worker.processor;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.KillTaskRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  task kill processor
 */
public class TaskKillProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskKillProcessor.class);

    /**
     *  task kill process
     *
     * @param channel channel
     * @param command command
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.KILL_TASK_REQUEST == command.getType(), String.format("invalid command type : %s", command.getType()));
        logger.info("received command : {}", command);
        KillTaskRequestCommand killCommand = FastJsonSerializer.deserialize(command.getBody(), KillTaskRequestCommand.class);
        doKill(killCommand);
    }

    /**
     * kill task logic
     *
     * @param killCommand killCommand
     */
    private void doKill(KillTaskRequestCommand killCommand){
        try {
            if(killCommand.getProcessId() == 0 ){
                logger.error("process kill failed, process id :{}, task id:{}", killCommand.getProcessId(), killCommand.getTaskInstanceId());
                return;
            }
            String cmd = String.format("sudo kill -9 %s", ProcessUtils.getPidsStr(killCommand.getProcessId()));

            logger.info("process id:{}, cmd:{}", killCommand.getProcessId(), cmd);

            OSUtils.exeCmd(cmd);

            // find log and kill yarn job
            killYarnJob(killCommand.getHost(), killCommand.getLogPath(), killCommand.getExecutePath(), killCommand.getTenantCode());

        } catch (Exception e) {
            logger.error("kill task failed", e);
        }
    }

    /**
     *  kill yarn job
     *
     * @param host host
     * @param logPath logPath
     * @param executePath executePath
     * @param tenantCode tenantCode
     */
    public void killYarnJob(String host, String logPath, String executePath, String tenantCode) {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            LogClientService logClient = null;
            String log = null;
            try {
                logClient = new LogClientService();
                log = logClient.viewLog(host, Constants.RPC_PORT, logPath);
            } finally {
                if(logClient != null){
                    logClient.close();
                }
            }
            if (StringUtils.isNotEmpty(log)) {
                List<String> appIds = LoggerUtils.getAppIds(log, logger);
                if (StringUtils.isEmpty(executePath)) {
                    logger.error("task instance work dir is empty");
                    throw new RuntimeException("task instance work dir is empty");
                }
                if (appIds.size() > 0) {
                    ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
                }
            }

        } catch (Exception e) {
            logger.error("kill yarn job failure",e);
        }
    }

}
