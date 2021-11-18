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

package org.apacheduler.dolphinscheduler.plugin.task.flinkx;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.DatasourceUtil;
import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.paramparser.ParamUtils;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;
import org.apache.dolphinscheduler.spi.task.request.FlinkxTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FlinkxTask extends AbstractYarnTask {

    private static final String FLINKX_COMMAND = "flinkx";

    private static final int FLINKX_CHANNEL_COUNT = 1;

    private static final String FLINKX_JOB_MAGIC_NAME = "flinkx_job_%s";

    private FlinkxParameters flinkxParameters;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    private String flinkxJobId;

    /**
     * Abstract Yarn Task
     *
     * @param taskExecutionContext taskRequest
     */
    public FlinkxTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("flinkx task params {}", taskExecutionContext.getTaskParams());
        flinkxParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkxParameters.class);
        flinkxJobId = String.format(FLINKX_JOB_MAGIC_NAME, taskExecutionContext.getTaskAppId());
        //check flinkx task params
        if (null == flinkxParameters) {
            throw new IllegalArgumentException("Flinkx Task params is null");
        }

        if (!flinkxParameters.checkParameters()) {
            throw new RuntimeException("flinkx task params is not valid");
        }
    }

    @Override
    protected String buildCommand() {
        // combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());

        // build flinkx json file
        String jsonFilePath = buildFlinkxJsonFile(paramsMap);

        // flinkx -mode <local> -job <job path> -flinkconf <flinkconf> -pluginRoot(fixed) <syncplugins>
        List<String> args = new ArrayList<>();
        args.add(FLINKX_COMMAND);
        logger.info("flink task args : {}", args);
        args.addAll(FlinkxArgsUtils.buildArgs(flinkxJobId, jsonFilePath, flinkxParameters));

        String command = ParameterUtils
                .convertParameterPlaceholders(String.join(" ", args), taskExecutionContext.getDefinedParams());

        logger.info("flink task command : {}", command);
        return command;
    }

    private String buildFlinkxJsonFile(Map<String, Property> paramsMap) {
        String fileName = String.format("%s/%s_job.json", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskAppId());
        String json;
        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (flinkxParameters.getCustomConfig() == Flag.YES.ordinal()) {
            json = flinkxParameters.getJson().replaceAll("\\r\\n", "\n");
        } else {
            ObjectNode job = JSONUtils.createObjectNode();
            job.putArray("content").addAll(buildFlinkxJobContentJson());
            job.set("setting", buildFlinkxJobSettingJson());

            ObjectNode root = JSONUtils.createObjectNode();
            root.set("job", job);
            json = root.toString();
        }
        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));
        logger.debug("flinkx job json : {}", json);

        // create flinkx json file
        try {
            FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("generate flinkx job file failure", e);
            throw new RuntimeException("generate flinkx job file failure");
        }
        return fileName;
    }

    private List<ObjectNode> buildFlinkxJobContentJson() {
        FlinkxTaskExecutionContext flinkxTaskExecutionContext = taskExecutionContext.getFlinkxTaskExecutionContext();
        DbType sourceDBType = DbType.of(flinkxTaskExecutionContext.getSourcetype());
        DbType targetDBType = DbType.of(flinkxTaskExecutionContext.getTargetType());

        BaseConnectionParam dataSourceCfg = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(sourceDBType,
                flinkxTaskExecutionContext.getSourceConnectionParams());
        BaseConnectionParam dataTargetCfg = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(targetDBType,
                flinkxTaskExecutionContext.getTargetConnectionParams());

        //reader
        List<ObjectNode> readerConnArr = new ArrayList<>();
        ObjectNode readerConn = JSONUtils.createObjectNode();
        readerConn.putArray("jdbcUrl").add(DatasourceUtil.getJdbcUrl(DbType.valueOf(flinkxParameters.getDtType()), dataSourceCfg));
        readerConn.putArray("table").add(flinkxParameters.getSourceTable());
        readerConnArr.add(readerConn);

        ObjectNode readerParam = JSONUtils.createObjectNode();
        readerParam.put("username", dataSourceCfg.getUser());
        readerParam.put("password", dataSourceCfg.getPassword());
        readerParam.putArray("connection").addAll(readerConnArr);
        ArrayNode sourceColumn = readerParam.putArray("column");
        flinkxParameters.getSourceColumns().forEach(sourceColumn::addPOJO);

        if (flinkxParameters.getSqlStatement()) {
            readerParam.put("customSql", flinkxParameters.getCustomSql());
        }

        if (flinkxParameters.getPolling()) {
            readerParam.put("polling", flinkxParameters.getPolling());
            readerParam.put("pollingInterval", flinkxParameters.getPollingInterval());
            readerParam.put("increColumn", flinkxParameters.getIncreColumn());
            if (StringUtils.isNotEmpty(flinkxParameters.getStartLocation())) {
                readerParam.put("startLocation", flinkxParameters.getStartLocation());
            }
        }

        if (flinkxParameters.getSplit() && flinkxParameters.getJobSpeedChannel() > 1) {
            readerParam.put("splitPk", flinkxParameters.getSplitPk());
        }

        ObjectNode reader = JSONUtils.createObjectNode();
        reader.put("name", FlinkxUtils.getReaderPluginName(sourceDBType));
        reader.set("parameter", readerParam);

        //writer
        List<ObjectNode> writerConnArr = new ArrayList<>();
        ObjectNode writerConn = JSONUtils.createObjectNode();
        writerConn.putArray("jdbcUrl").add(DatasourceUtil.getJdbcUrl(DbType.valueOf(flinkxParameters.getDtType()), dataTargetCfg));
        writerConn.putArray("table").add(flinkxParameters.getTargetTable());
        writerConnArr.add(writerConn);

        ObjectNode writerParam = JSONUtils.createObjectNode();
        writerParam.put("username", dataTargetCfg.getUser());
        writerParam.put("password", dataTargetCfg.getPassword());
        writerParam.putArray("connection").addAll(writerConnArr);

        ArrayNode targetColumn = writerParam.putArray("column");
        flinkxParameters.getTargetColumns().forEach(targetColumn::addPOJO);

        writerParam.putPOJO("writeMode", flinkxParameters.getWriteMode());

        writerParam.put("batchSize", flinkxParameters.getBatchSize());

        WriteMode writeMode = flinkxParameters.getWriteMode();
        if (writeMode != null && writeMode == WriteMode.INSERT) {
            if (targetDBType != null && targetDBType == DbType.POSTGRESQL) {
                writerParam.put("insertSqlMode", "copy");
            }
        }

        if (writeMode != null && writeMode == WriteMode.UPDATE) {
            //唯一索引
            ObjectNode updateKey = JSONUtils.createObjectNode();
            ArrayNode key = updateKey.putArray("key");
            flinkxParameters.getUniqueKey().forEach(key::add);
            writerParam.set("updateKey", updateKey);
        }

        if (CollectionUtils.isNotEmpty(flinkxParameters.getPreStatements())) {
            ArrayNode preSql = writerParam.putArray("preSql");
            flinkxParameters.getPreStatements().forEach(preSql::add);
        }

        if (CollectionUtils.isNotEmpty(flinkxParameters.getPostStatements())) {
            ArrayNode postSql = writerParam.putArray("postSql");
            flinkxParameters.getPostStatements().forEach(postSql::add);
        }

        ObjectNode writer = JSONUtils.createObjectNode();
        writer.put("name", FlinkxUtils.getWriterPluginName(targetDBType));
        writer.set("parameter", writerParam);

        List<ObjectNode> contentList = new ArrayList<>();
        ObjectNode content = JSONUtils.createObjectNode();
        content.set("reader", reader);
        content.set("writer", writer);
        contentList.add(content);
        return contentList;
    }

    private ObjectNode buildFlinkxJobSettingJson() {
        ObjectNode speed = JSONUtils.createObjectNode();
        if (flinkxParameters.getSplit() && flinkxParameters.getJobSpeedChannel() > 1) {
            speed.put("channel", flinkxParameters.getJobSpeedChannel());
        } else {
            speed.put("channel", FLINKX_CHANNEL_COUNT);
        }

        if (flinkxParameters.getJobSpeedByte() > 0) {
            speed.put("byte", flinkxParameters.getJobSpeedByte());
        }

        ObjectNode errorLimit = JSONUtils.createObjectNode();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0.0D);

        ObjectNode restore = JSONUtils.createObjectNode();
        restore.put("isStream", flinkxParameters.getStream());
        restore.put("isRestore", flinkxParameters.getRestore());

        if (flinkxParameters.getRestore()) {
            restore.put("restoreColumnName", flinkxParameters.getRestoreColumnName());
            restore.put("restoreColumnIndex", flinkxParameters.getRestoreColumnIndex());
        }
        restore.put("maxRowNumForCheckpoint", flinkxParameters.getMaxRowNumForCheckpoint());

        ObjectNode setting = JSONUtils.createObjectNode();
        setting.set("speed", speed);
        setting.set("errorLimit", errorLimit);
        setting.set("restore", restore);

        return setting;
    }

    @Override
    protected void setMainJarName() {
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkxParameters;
    }
}
