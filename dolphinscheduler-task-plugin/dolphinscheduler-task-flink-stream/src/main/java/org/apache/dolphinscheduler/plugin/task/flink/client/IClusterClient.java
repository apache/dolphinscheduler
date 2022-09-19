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

package org.apache.dolphinscheduler.plugin.task.flink.client;

import org.apache.dolphinscheduler.plugin.task.flink.entity.CheckpointInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.enums.YarnTaskStatus;

import java.util.List;

/**
 * cluster client interface
 */
public interface IClusterClient {

    /**
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo submitFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * submit flink job in kerberos env
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo submitFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * kill flink job in yarn and delete application files in hdfs.
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo killYarnJob(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * kill yarn job in kerberos env
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo killYarnJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * get yarn job status
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    YarnTaskStatus getYarnJobStatus(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * get yarn job status
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    YarnTaskStatus getYarnJobStatusWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * get checkpoint path
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    List<CheckpointInfo> getCheckpointPaths(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * get checkpoint path
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    List<CheckpointInfo> getCheckpointPathsWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * print finished job logs
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    String printFinishedLogToFile(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * print finished job logs
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    String printFinishedLogToFileWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * cancel flink job by applicationId and flink job id
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    FlinkResultInfo cancelFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * cancel flink job by applicationId and flink job id
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    FlinkResultInfo cancelFlinkJobDoSavepoint(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * cancel flink job by applicationId and flink job id with Kerberos
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    FlinkResultInfo cancelFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * cancel flink job by applicationId and flink job id with Kerberos
     *
     * @param jobParamsInfo
     * @return file path
     * @throws Exception
     */
    FlinkResultInfo cancelFlinkJobDoSavepointWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     * save
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo savePointFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception;

    /**
     *
     * @param jobParamsInfo
     * @return
     * @throws Exception
     */
    FlinkResultInfo savePointFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception;
}
