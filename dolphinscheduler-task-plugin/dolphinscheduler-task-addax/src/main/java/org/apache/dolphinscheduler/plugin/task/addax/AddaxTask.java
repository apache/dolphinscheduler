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

package org.apache.dolphinscheduler.plugin.task.addax;

import static org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.log.SensitiveDataConverter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.Flag;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
public class AddaxTask extends AbstractTask {

    /**
     * jvm parameters
     */
    public static final String JVM_PARAM = "--jvm=\"-Xms%sM -Xmx%sM\" ";

    public static final String CUSTOM_PARAM = " -D%s='%s'";

    /**
     * select all
     */
    private static final String SELECT_ALL_CHARACTER = "*";

    /**
     * post jdbc info regex
     */
    private static final String POST_JDBC_INFO_REGEX = "(?<=(post jdbc info:)).*(?=)";
    /**
     * addax path
     */
    private static final String ADDAX_LAUNCHER = "${ADDAX_LAUNCHER}";
    /**
     * addax channel count
     */
    private static final int ADDAX_CHANNEL_COUNT = 1;

    /**
     * addax parameters
     */
    private AddaxParameters addaxParameters;

    /**
     * shell command executor
     */
    private final ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;

    private AddaxTaskExecutionContext addaxTaskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public AddaxTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle, taskExecutionContext);
    }

    /**
     * init Addax config
     */
    @Override
    public void init() {
        addaxParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), AddaxParameters.class);
        log.info("Initialize addax task params {}", JSONUtils.toPrettyJsonString(addaxParameters));

        if (addaxParameters == null || !addaxParameters.checkParameters()) {
            throw new RuntimeException("addax task params is not valid");
        }
        SensitiveDataConverter.addMaskPattern(POST_JDBC_INFO_REGEX);
        addaxTaskExecutionContext =
                addaxParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // replace placeholder,and combine local and global parameters
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .properties(ParameterUtils.convert(paramsMap))
                    .appendScript(buildCommand(buildAddaxJsonFile(paramsMap), paramsMap));

            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setProcessId(commandExecuteResult.getProcessId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current Addax task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current Addax task has been interrupted", e);
        } catch (Exception e) {
            log.error("addax task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute Addax task failed", e);
        }
    }

    /**
     * cancel Addax process
     *
     * @throws TaskException if error throws Exception
     */
    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    /**
     * build addax configuration file
     *
     * @return addax json file name
     * @throws Exception if error throws Exception
     */
    private String buildAddaxJsonFile(Map<String, Property> paramsMap) throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (addaxParameters.getCustomConfig() == Flag.YES.ordinal()) {
            json = addaxParameters.getJson().replaceAll("\\r\\n", System.lineSeparator());
        } else {
            ObjectNode job = JSONUtils.createObjectNode();
            job.putArray("content").addAll(buildAddaxJobContentJson());
            job.set("setting", buildAddaxJobSettingJson());

            ObjectNode root = JSONUtils.createObjectNode();
            root.set("job", job);
            root.set("core", buildAddaxCoreJson());
            json = root.toString();
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParameterUtils.convert(paramsMap));

        log.debug("addax job json : {}", json);

        // create addax json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * build addax job config
     *
     * @return collection of addax job config JSONObject
     */
    private List<ObjectNode> buildAddaxJobContentJson() {

        BaseConnectionParam dataSourceCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                addaxTaskExecutionContext.getSourcetype(),
                addaxTaskExecutionContext.getSourceConnectionParams());

        BaseConnectionParam dataTargetCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                addaxTaskExecutionContext.getTargetType(),
                addaxTaskExecutionContext.getTargetConnectionParams());

        List<ObjectNode> readerConnArr = new ArrayList<>();
        ObjectNode readerConn = JSONUtils.createObjectNode();

        ArrayNode sqlArr = readerConn.putArray("querySql");
        for (String sql : new String[]{addaxParameters.getSql()}) {
            sqlArr.add(sql);
        }

        ArrayNode urlArr = readerConn.putArray("jdbcUrl");
        urlArr.add(DataSourceUtils.getJdbcUrl(DbType.valueOf(addaxParameters.getDsType()), dataSourceCfg));

        readerConnArr.add(readerConn);

        ObjectNode readerParam = JSONUtils.createObjectNode();
        readerParam.put("username", dataSourceCfg.getUser());
        readerParam.put("password", decodePassword(dataSourceCfg.getPassword()));
        readerParam.putArray("connection").addAll(readerConnArr);

        ObjectNode reader = JSONUtils.createObjectNode();
        reader.put("name", AddaxUtils.getReaderPluginName(addaxTaskExecutionContext.getSourcetype()));
        reader.set("parameter", readerParam);

        List<ObjectNode> writerConnArr = new ArrayList<>();
        ObjectNode writerConn = JSONUtils.createObjectNode();
        ArrayNode tableArr = writerConn.putArray("table");
        tableArr.add(addaxParameters.getTargetTable());

        writerConn.put("jdbcUrl",
                DataSourceUtils.getJdbcUrl(DbType.valueOf(addaxParameters.getDtType()), dataTargetCfg));
        writerConnArr.add(writerConn);

        ObjectNode writerParam = JSONUtils.createObjectNode();
        writerParam.put("username", dataTargetCfg.getUser());
        writerParam.put("password", decodePassword(dataTargetCfg.getPassword()));

        String[] columns = parsingSqlColumnNames(addaxTaskExecutionContext.getSourcetype(),
                addaxTaskExecutionContext.getTargetType(),
                dataSourceCfg, addaxParameters.getSql());

        ArrayNode columnArr = writerParam.putArray("column");
        for (String column : columns) {
            columnArr.add(column);
        }
        writerParam.putArray("connection").addAll(writerConnArr);

        if (CollectionUtils.isNotEmpty(addaxParameters.getPreStatements())) {
            ArrayNode preSqlArr = writerParam.putArray("preSql");
            for (String preSql : addaxParameters.getPreStatements()) {
                preSqlArr.add(preSql);
            }

        }

        if (CollectionUtils.isNotEmpty(addaxParameters.getPostStatements())) {
            ArrayNode postSqlArr = writerParam.putArray("postSql");
            for (String postSql : addaxParameters.getPostStatements()) {
                postSqlArr.add(postSql);
            }
        }

        ObjectNode writer = JSONUtils.createObjectNode();
        writer.put("name", AddaxUtils.getWriterPluginName(addaxTaskExecutionContext.getTargetType()));
        writer.set("parameter", writerParam);

        List<ObjectNode> contentList = new ArrayList<>();
        ObjectNode content = JSONUtils.createObjectNode();
        content.set("reader", reader);
        content.set("writer", writer);
        contentList.add(content);

        return contentList;
    }

    /**
     * build addax setting config
     *
     * @return addax setting config JSONObject
     */
    private ObjectNode buildAddaxJobSettingJson() {

        ObjectNode speed = JSONUtils.createObjectNode();

        speed.put("channel", ADDAX_CHANNEL_COUNT);

        if (addaxParameters.getJobSpeedByte() > 0) {
            speed.put("byte", addaxParameters.getJobSpeedByte());
        }

        if (addaxParameters.getJobSpeedRecord() > 0) {
            speed.put("record", addaxParameters.getJobSpeedRecord());
        }

        ObjectNode errorLimit = JSONUtils.createObjectNode();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0);

        ObjectNode setting = JSONUtils.createObjectNode();
        setting.set("speed", speed);
        setting.set("errorLimit", errorLimit);

        return setting;
    }

    private ObjectNode buildAddaxCoreJson() {

        ObjectNode speed = JSONUtils.createObjectNode();
        speed.put("channel", ADDAX_CHANNEL_COUNT);

        if (addaxParameters.getJobSpeedByte() > 0) {
            speed.put("byte", addaxParameters.getJobSpeedByte());
        }

        if (addaxParameters.getJobSpeedRecord() > 0) {
            speed.put("record", addaxParameters.getJobSpeedRecord());
        }

        ObjectNode channel = JSONUtils.createObjectNode();
        channel.set("speed", speed);

        ObjectNode transport = JSONUtils.createObjectNode();
        transport.set("channel", channel);

        ObjectNode core = JSONUtils.createObjectNode();
        core.set("transport", transport);

        return core;
    }

    /**
     * create command
     *
     * @return shell command file name
     */
    protected String buildCommand(String jobConfigFilePath, Map<String, Property> paramsMap) {
        // addax shell command
        return ADDAX_LAUNCHER +
                " " +
                loadJvmEnv(addaxParameters) +
                addCustomParameters(paramsMap) +
                " " +
                jobConfigFilePath;
    }

    private StringBuilder addCustomParameters(Map<String, Property> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return new StringBuilder();
        }
        StringBuilder customParameters = new StringBuilder("-p \"");
        for (Map.Entry<String, Property> entry : paramsMap.entrySet()) {
            customParameters.append(String.format(CUSTOM_PARAM, entry.getKey(), entry.getValue().getValue()));
        }
        customParameters.replace(4, 5, "");
        customParameters.append("\"");
        return customParameters;
    }

    public String loadJvmEnv(AddaxParameters addaxParameters) {
        int xms = Math.max(addaxParameters.getXms(), 1);
        int xmx = Math.max(addaxParameters.getXmx(), 1);
        return String.format(JVM_PARAM, xms, xmx);
    }

    /**
     * parsing synchronized column names in SQL statements
     *
     * @param sourceType the database type of the data source
     * @param targetType the database type of the data target
     * @param dataSourceCfg the database connection parameters of the data source
     * @param sql sql for data synchronization
     * @return Keyword converted column names
     */
    private String[] parsingSqlColumnNames(DbType sourceType, DbType targetType, BaseConnectionParam dataSourceCfg,
                                           String sql) {
        String[] columnNames = tryGrammaticalAnalysisSqlColumnNames(sourceType, sql);

        if (columnNames == null || columnNames.length == 0) {
            log.info("try to execute sql analysis query column name");
            columnNames = tryExecuteSqlResolveColumnNames(sourceType, dataSourceCfg, sql);
        }

        notNull(columnNames, String.format("parsing sql columns failed : %s", sql));

        return AddaxUtils.convertKeywordsColumns(targetType, columnNames);
    }

    /**
     * try grammatical parsing column
     *
     * @param dbType database type
     * @param sql sql for data synchronization
     * @return column name array
     * @throws RuntimeException if error throws RuntimeException
     */
    private String[] tryGrammaticalAnalysisSqlColumnNames(DbType dbType, String sql) {
        String[] columnNames;

        try {
            SQLStatementParser parser = AddaxUtils.getSqlStatementParser(dbType, sql);
            if (parser == null) {
                log.warn("database driver [{}] is not support grammatical analysis sql", dbType);
                return new String[0];
            }

            SQLStatement sqlStatement = parser.parseStatement();
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
            SQLSelect sqlSelect = sqlSelectStatement.getSelect();

            List<SQLSelectItem> selectItemList = null;
            if (sqlSelect.getQuery() instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) sqlSelect.getQuery();
                selectItemList = block.getSelectList();
            } else if (sqlSelect.getQuery() instanceof SQLUnionQuery) {
                SQLUnionQuery unionQuery = (SQLUnionQuery) sqlSelect.getQuery();
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) unionQuery.getRight();
                selectItemList = block.getSelectList();
            }

            notNull(selectItemList,
                    String.format("select query type [%s] is not support", sqlSelect.getQuery().toString()));

            columnNames = new String[selectItemList.size()];
            for (int i = 0; i < selectItemList.size(); i++) {
                SQLSelectItem item = selectItemList.get(i);

                String columnName = null;

                if (item.getAlias() != null) {
                    columnName = item.getAlias();
                } else if (item.getExpr() != null) {
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr) item.getExpr();
                        columnName = expr.getName();
                    } else if (item.getExpr() instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();
                        columnName = expr.getName();
                    }
                } else {
                    throw new RuntimeException(
                            String.format("grammatical analysis sql column [ %s ] failed", item));
                }

                if (SELECT_ALL_CHARACTER.equals(item.toString())) {
                    log.info("sql contains *, grammatical analysis failed");
                    return new String[0];
                }

                if (columnName == null) {
                    throw new RuntimeException(
                            String.format("grammatical analysis sql column [ %s ] failed", item));
                }

                columnNames[i] = columnName;
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return new String[0];
        }

        return columnNames;
    }

    /**
     * try to execute sql to resolve column names
     *
     * @param baseDataSource the database connection parameters
     * @param sql sql for data synchronization
     * @return column name array
     */
    public String[] tryExecuteSqlResolveColumnNames(DbType sourceType, BaseConnectionParam baseDataSource, String sql) {
        String[] columnNames;
        sql = String.format("SELECT t.* FROM ( %s ) t WHERE 0 = 1", sql);
        sql = sql.replace(";", "");

        try (
                Connection connection =
                        DataSourceClientProvider.getAdHocConnection(sourceType, baseDataSource);
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet resultSet = stmt.executeQuery()) {

            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            columnNames = new String[num];
            for (int i = 1; i <= num; i++) {
                columnNames[i - 1] = md.getColumnName(i).replace("t.", "");
            }
        } catch (SQLException | ExecutionException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    @Override
    public AbstractParameters getParameters() {
        return addaxParameters;
    }

    private void notNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }

}
