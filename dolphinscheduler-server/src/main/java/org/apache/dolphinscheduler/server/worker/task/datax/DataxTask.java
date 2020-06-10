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
package org.apache.dolphinscheduler.server.worker.task.datax;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.DataxUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.CommandExecuteResult;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.fastjson.JSONObject;


/**
 * DataX task
 */
public class DataxTask extends AbstractTask {

    /**
     * python process(datax only supports version 2.7 by default)
     */
    private static final String DATAX_PYTHON = "python2.7";

    /**
     * datax home path
     */
    private static final String DATAX_HOME_EVN = "${DATAX_HOME}";

    /**
     * datax channel count
     */
    private static final int DATAX_CHANNEL_COUNT = 1;

    /**
     * datax parameters
     */
    private DataxParameters dataXParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    public DataxTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;


        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,logger);
    }

    /**
     * init DataX config
     */
    @Override
    public void init() {
        logger.info("datax task params {}", taskExecutionContext.getTaskParams());
        dataXParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DataxParameters.class);

        if (!dataXParameters.checkParameters()) {
            throw new RuntimeException("datax task params is not valid");
        }
    }

    /**
     * run DataX process
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void handle() throws Exception {
        try {
            // set the name of the current thread
            String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskExecutionContext.getTaskAppId());
            Thread.currentThread().setName(threadLoggerInfoName);

            // combining local and global parameters
            Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                    taskExecutionContext.getDefinedParams(),
                    dataXParameters.getLocalParametersMap(),
                    CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                    taskExecutionContext.getScheduleTime());

            // run datax process
            String jsonFilePath = buildDataxJsonFile(paramsMap);
            String shellCommandFilePath = buildShellCommandFile(jsonFilePath, paramsMap);
            CommandExecuteResult commandExecuteResult = shellCommandExecutor.run(shellCommandFilePath);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
        }
        catch (Exception e) {
            logger.error("datax task failure", e);
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            throw e;
        }
    }

    /**
     * cancel DataX process
     *
     * @param cancelApplication cancelApplication
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancelApplication(boolean cancelApplication)
        throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    /**
     * build datax configuration file
     * 
     * @return datax json file name
     * @throws Exception if error throws Exception
     */
    private String buildDataxJsonFile(Map<String, Property> paramsMap)
        throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (dataXParameters.getCustomConfig() == Flag.YES.ordinal()){
            json = dataXParameters.getJson().replaceAll("\\r\\n", "\n");
        }else {
            JSONObject job = new JSONObject();
            job.put("content", buildDataxJobContentJson());
            job.put("setting", buildDataxJobSettingJson());

            JSONObject root = new JSONObject();
            root.put("job", job);
            root.put("core", buildDataxCoreJson());
            json = root.toString();
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));

        logger.debug("datax job json : {}", json);

        // create datax json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * build datax job config
     * 
     * @return collection of datax job config JSONObject
     * @throws SQLException if error throws SQLException
     */
    private List<JSONObject> buildDataxJobContentJson() throws SQLException {
        DataxTaskExecutionContext dataxTaskExecutionContext = taskExecutionContext.getDataxTaskExecutionContext();


        BaseDataSource dataSourceCfg = DataSourceFactory.getDatasource(DbType.of(dataxTaskExecutionContext.getSourcetype()),
                dataxTaskExecutionContext.getSourceConnectionParams());

        BaseDataSource dataTargetCfg = DataSourceFactory.getDatasource(DbType.of(dataxTaskExecutionContext.getTargetType()),
                dataxTaskExecutionContext.getTargetConnectionParams());

        List<JSONObject> readerConnArr = new ArrayList<>();
        JSONObject readerConn = new JSONObject();
        readerConn.put("querySql", new String[] {dataXParameters.getSql()});
        readerConn.put("jdbcUrl", new String[] {dataSourceCfg.getJdbcUrl()});
        readerConnArr.add(readerConn);

        JSONObject readerParam = new JSONObject();
        readerParam.put("username", dataSourceCfg.getUser());
        readerParam.put("password", dataSourceCfg.getPassword());
        readerParam.put("connection", readerConnArr);

        JSONObject reader = new JSONObject();
        reader.put("name", DataxUtils.getReaderPluginName(DbType.of(dataxTaskExecutionContext.getSourcetype())));
        reader.put("parameter", readerParam);

        List<JSONObject> writerConnArr = new ArrayList<>();
        JSONObject writerConn = new JSONObject();
        writerConn.put("table", new String[] {dataXParameters.getTargetTable()});
        writerConn.put("jdbcUrl", dataTargetCfg.getJdbcUrl());
        writerConnArr.add(writerConn);

        JSONObject writerParam = new JSONObject();
        writerParam.put("username", dataTargetCfg.getUser());
        writerParam.put("password", dataTargetCfg.getPassword());
        writerParam.put("column",
            parsingSqlColumnNames(DbType.of(dataxTaskExecutionContext.getSourcetype()),
                    DbType.of(dataxTaskExecutionContext.getTargetType()),
                    dataSourceCfg, dataXParameters.getSql()));
        writerParam.put("connection", writerConnArr);

        if (CollectionUtils.isNotEmpty(dataXParameters.getPreStatements())) {
            writerParam.put("preSql", dataXParameters.getPreStatements());
        }

        if (CollectionUtils.isNotEmpty(dataXParameters.getPostStatements())) {
            writerParam.put("postSql", dataXParameters.getPostStatements());
        }

        JSONObject writer = new JSONObject();
        writer.put("name", DataxUtils.getWriterPluginName(DbType.of(dataxTaskExecutionContext.getTargetType())));
        writer.put("parameter", writerParam);

        List<JSONObject> contentList = new ArrayList<>();
        JSONObject content = new JSONObject();
        content.put("reader", reader);
        content.put("writer", writer);
        contentList.add(content);

        return contentList;
    }

    /**
     * build datax setting config
     * 
     * @return datax setting config JSONObject
     */
    private JSONObject buildDataxJobSettingJson() {
        JSONObject speed = new JSONObject();
        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        JSONObject errorLimit = new JSONObject();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0);

        JSONObject setting = new JSONObject();
        setting.put("speed", speed);
        setting.put("errorLimit", errorLimit);

        return setting;
    }

    private JSONObject buildDataxCoreJson() {
        JSONObject speed = new JSONObject();
        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        JSONObject channel = new JSONObject();
        channel.put("speed", speed);

        JSONObject transport = new JSONObject();
        transport.put("channel", channel);

        JSONObject core = new JSONObject();
        core.put("transport", transport);

        return core;
    }

    /**
     * create command
     * 
     * @return shell command file name
     * @throws Exception if error throws Exception
     */
    private String buildShellCommandFile(String jobConfigFilePath, Map<String, Property> paramsMap)
        throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId(),
                OSUtils.isWindows() ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

        // datax python command
        StringBuilder sbr = new StringBuilder();
        sbr.append(DATAX_PYTHON);
        sbr.append(" ");
        sbr.append(DATAX_HOME_EVN);
        sbr.append(" ");
        sbr.append(jobConfigFilePath);

        // replace placeholder
        String dataxCommand = ParameterUtils.convertParameterPlaceholders(sbr.toString(), ParamUtils.convert(paramsMap));

        logger.debug("raw script : {}", dataxCommand);

        // create shell command file
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(Constants.RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, dataxCommand.getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    /**
     * parsing synchronized column names in SQL statements
     * 
     * @param dsType
     *            the database type of the data source
     * @param dtType
     *            the database type of the data target
     * @param dataSourceCfg
     *            the database connection parameters of the data source
     * @param sql
     *            sql for data synchronization
     * @return Keyword converted column names
     */
    private String[] parsingSqlColumnNames(DbType dsType, DbType dtType, BaseDataSource dataSourceCfg, String sql) {
        String[] columnNames = tryGrammaticalAnalysisSqlColumnNames(dsType, sql);

        if (columnNames == null || columnNames.length == 0) {
            logger.info("try to execute sql analysis query column name");
            columnNames = tryExecuteSqlResolveColumnNames(dataSourceCfg, sql);
        }

        notNull(columnNames, String.format("parsing sql columns failed : %s", sql));

        return DataxUtils.convertKeywordsColumns(dtType, columnNames);
    }

    /**
     * try grammatical parsing column
     * 
     * @param dbType
     *            database type
     * @param sql
     *            sql for data synchronization
     * @return column name array
     * @throws RuntimeException if error throws RuntimeException
     */
    private String[] tryGrammaticalAnalysisSqlColumnNames(DbType dbType, String sql) {
        String[] columnNames;

        try {
            SQLStatementParser parser = DataxUtils.getSqlStatementParser(dbType, sql);
            notNull(parser, String.format("database driver [%s] is not support", dbType.toString()));

            SQLStatement sqlStatement = parser.parseStatement();
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement)sqlStatement;
            SQLSelect sqlSelect = sqlSelectStatement.getSelect();

            List<SQLSelectItem> selectItemList = null;
            if (sqlSelect.getQuery() instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock)sqlSelect.getQuery();
                selectItemList = block.getSelectList();
            } else if (sqlSelect.getQuery() instanceof SQLUnionQuery) {
                SQLUnionQuery unionQuery = (SQLUnionQuery)sqlSelect.getQuery();
                SQLSelectQueryBlock block = (SQLSelectQueryBlock)unionQuery.getRight();
                selectItemList = block.getSelectList();
            }

            notNull(selectItemList,
                String.format("select query type [%s] is not support", sqlSelect.getQuery().toString()));

            columnNames = new String[selectItemList.size()];
            for (int i = 0; i < selectItemList.size(); i++ ) {
                SQLSelectItem item = selectItemList.get(i);

                String columnName = null;

                if (item.getAlias() != null) {
                    columnName = item.getAlias();
                } else if (item.getExpr() != null) {
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr)item.getExpr();
                        columnName = expr.getName();
                    } else if (item.getExpr() instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr)item.getExpr();
                        columnName = expr.getName();
                    }
                } else {
                    throw new RuntimeException(
                        String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                if (columnName == null) {
                    throw new RuntimeException(
                        String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                columnNames[i] = columnName;
            }
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    /**
     * try to execute sql to resolve column names
     * 
     * @param baseDataSource
     *            the database connection parameters
     * @param sql
     *            sql for data synchronization
     * @return column name array
     */
    public String[] tryExecuteSqlResolveColumnNames(BaseDataSource baseDataSource, String sql) {
        String[] columnNames;
        sql = String.format("SELECT t.* FROM ( %s ) t WHERE 0 = 1", sql);
        sql = sql.replace(";", "");

        try (
            Connection connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(), baseDataSource.getUser(),
                baseDataSource.getPassword());
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery()) {

            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            columnNames = new String[num];
            for (int i = 1; i <= num; i++ ) {
                columnNames[i - 1] = md.getColumnName(i);
            }
        }
        catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    @Override
    public AbstractParameters getParameters() {
        return dataXParameters;
    }

    private void notNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }

}
