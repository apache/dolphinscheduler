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

package org.apache.dolphinscheduler.plugin.task.seatunnel.self;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTask;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeatunnelEngineTask extends SeatunnelTask {

    private SeatunnelEngineParameters seatunnelParameters;

    public SeatunnelEngineTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        seatunnelParameters =
                JSONUtils.parseObject(taskRequest.getTaskParams(), SeatunnelEngineParameters.class);
        setSeatunnelParameters(seatunnelParameters);
        super.init();
    }

    @Override
    public List<String> buildOptions() throws Exception {
        List<String> args = super.buildOptions();
        if (!Objects.isNull(seatunnelParameters.getDeployMode())) {
            args.add(Constants.DEPLOY_MODE_OPTIONS);
            args.add(seatunnelParameters.getDeployMode().getCommand());
        }
        if (StringUtils.isNotBlank(seatunnelParameters.getOthers())) {
            args.add(seatunnelParameters.getOthers());
        }
        return args;
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        if (StringUtils.isNotEmpty(getAppIds())) {
            return Arrays.asList(getAppIds().split(","));
        }
        List<String> jobIds = findSeaTunnelJobIdsFromLog();
        if (jobIds != null && !jobIds.isEmpty()) {
            setAppIds(String.join(",", jobIds));
        }
        return jobIds == null ? Collections.emptyList() : jobIds;
    }

    @Override
    public void cancelApplication() throws TaskException {
        List<String> jobIds = Collections.emptyList();
        try {
            jobIds = getApplicationIds();
        } catch (Exception e) {
            log.warn("Failed to resolve SeaTunnel job id before cancel, will still kill local process", e);
        }

        try {
            // Kill local seatunnel client process tree (relies on SeaTunnel -cj/--close-job default)
            cancelShellProcess();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }

        // Also cancel the engine job explicitly. Needed when the client is already gone,
        // --async was used, or process kill alone did not stop a cluster streaming job.
        if (jobIds != null && !jobIds.isEmpty()) {
            for (String jobId : jobIds) {
                cancelSeaTunnelJob(jobId);
            }
        } else {
            log.warn("SeaTunnel job id not found in logs, skipped seatunnel.sh -can");
        }
    }

    void cancelSeaTunnelJob(String jobId) throws TaskException {
        String seatunnelHome = System.getenv("SEATUNNEL_HOME");
        if (StringUtils.isBlank(seatunnelHome)) {
            log.warn("SEATUNNEL_HOME is not set, cannot run seatunnel.sh -can {}", jobId);
            return;
        }
        List<String> args = new ArrayList<>();
        args.add(seatunnelHome + "/bin/seatunnel.sh");
        args.add(Constants.CANCEL_JOB_OPTIONS);
        args.add(jobId);
        log.info("Cancel SeaTunnel job with args: {}", args);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new TaskException("Failed to cancel SeaTunnel job: " + jobId, e);
        }
    }

}
