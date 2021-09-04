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

package org.apache.dolphinscheduler.server.utils;

import static org.apache.dolphinscheduler.common.Constants.AND;
import static org.apache.dolphinscheduler.common.Constants.BATCH;
import static org.apache.dolphinscheduler.common.Constants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.common.Constants.DATABASE;
import static org.apache.dolphinscheduler.common.Constants.DRIVER;
import static org.apache.dolphinscheduler.common.Constants.ERROR_OUTPUT_PATH;
import static org.apache.dolphinscheduler.common.Constants.HDFS_FILE;
import static org.apache.dolphinscheduler.common.Constants.INDEX;
import static org.apache.dolphinscheduler.common.Constants.INPUT_TABLE;
import static org.apache.dolphinscheduler.common.Constants.OUTPUT_TABLE;
import static org.apache.dolphinscheduler.common.Constants.PARAMETER_BUSINESS_DATE;
import static org.apache.dolphinscheduler.common.Constants.PARAMETER_CURRENT_DATE;
import static org.apache.dolphinscheduler.common.Constants.PARAMETER_DATETIME;
import static org.apache.dolphinscheduler.common.Constants.PASSWORD;
import static org.apache.dolphinscheduler.common.Constants.PATH;
import static org.apache.dolphinscheduler.common.Constants.SQL;
import static org.apache.dolphinscheduler.common.Constants.SRC_FILTER;
import static org.apache.dolphinscheduler.common.Constants.SRC_TABLE;
import static org.apache.dolphinscheduler.common.Constants.STATISTICS_EXECUTE_SQL;
import static org.apache.dolphinscheduler.common.Constants.STATISTICS_TABLE;
import static org.apache.dolphinscheduler.common.Constants.TABLE;
import static org.apache.dolphinscheduler.common.Constants.TARGET_FILTER;
import static org.apache.dolphinscheduler.common.Constants.TARGET_TABLE;
import static org.apache.dolphinscheduler.common.Constants.URL;
import static org.apache.dolphinscheduler.common.Constants.USER;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.dq.ExecuteSqlType;
import org.apache.dolphinscheduler.common.exception.DolphinException;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.server.entity.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.BaseConfig;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.EnvConfig;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parser.MappingColumn;

import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * RuleParserUtils
 */
public class RuleParserUtils {

    private RuleParserUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String AND_SRC_FILTER = "AND (${src_filter})";
    private static final String WHERE_SRC_FILTER = "WHERE (${src_filter})";
    private static final String AND_TARGET_FILTER = "AND (${target_filter})";
    private static final String WHERE_TARGET_FILTER = "WHERE (${target_filter})";

    public static List<BaseConfig> getReaderConfigList(
                                            Map<String, String> inputParameterValue,
                                            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<BaseConfig> readerConfigList = new ArrayList<>();

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getSourceConnectorType())) {
            BaseDataSource sourceDataSource = DataSourceFactory.getDatasource
                    (DbType.of(dataQualityTaskExecutionContext.getSourceType()),
                    dataQualityTaskExecutionContext.getSourceConnectionParams());
            BaseConfig sourceBaseConfig = new BaseConfig();
            sourceBaseConfig.setType(dataQualityTaskExecutionContext.getSourceConnectorType());
            Map<String,Object> config = new HashMap<>();
            if (sourceDataSource != null) {
                config.put(DATABASE,sourceDataSource.getDatabase());
                config.put(TABLE,inputParameterValue.get(SRC_TABLE));
                config.put(URL,sourceDataSource.getJdbcUrl());
                config.put(USER,sourceDataSource.getUser());
                config.put(PASSWORD,sourceDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getSourceType())));
                String outputTable = sourceDataSource.getDatabase() + "_" + inputParameterValue.get(SRC_TABLE);
                config.put(OUTPUT_TABLE,outputTable);
                inputParameterValue.put(SRC_TABLE,outputTable);
            }
            sourceBaseConfig.setConfig(config);

            readerConfigList.add(sourceBaseConfig);
        }

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getTargetConnectorType())) {
            BaseDataSource targetDataSource = DataSourceFactory.getDatasource(DbType.of(dataQualityTaskExecutionContext.getTargetType()),
                    dataQualityTaskExecutionContext.getTargetConnectionParams());
            BaseConfig targetBaseConfig = new BaseConfig();
            targetBaseConfig.setType(dataQualityTaskExecutionContext.getTargetConnectorType());
            Map<String,Object> config = new HashMap<>();
            if (targetDataSource != null) {
                config.put(DATABASE,targetDataSource.getDatabase());
                config.put(TABLE,inputParameterValue.get(TARGET_TABLE));
                config.put(URL,targetDataSource.getJdbcUrl());
                config.put(USER,targetDataSource.getUser());
                config.put(PASSWORD,targetDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getTargetType())));
                String outputTable = targetDataSource.getDatabase() + "_" + inputParameterValue.get(TARGET_TABLE);
                config.put(OUTPUT_TABLE,outputTable);
                inputParameterValue.put(TARGET_TABLE,outputTable);
            }
            targetBaseConfig.setConfig(config);

            readerConfigList.add(targetBaseConfig);
        }

        return readerConfigList;
    }

    public static int replaceExecuteSqlPlaceholder(List<DqRuleExecuteSql> executeSqlList,
                                             int index, Map<String, String> inputParameterValueResult,
                                             List<BaseConfig> transformerConfigList) {
        List<DqRuleExecuteSql> midExecuteSqlDefinitionList
                = getExecuteSqlListByType(executeSqlList, ExecuteSqlType.MIDDLE);

        List<DqRuleExecuteSql> statisticsExecuteSqlDefinitionList
                = getExecuteSqlListByType(executeSqlList, ExecuteSqlType.STATISTICS);

        checkAndReplace(midExecuteSqlDefinitionList,inputParameterValueResult.get(SRC_FILTER),AND_SRC_FILTER);
        checkAndReplace(midExecuteSqlDefinitionList,inputParameterValueResult.get(SRC_FILTER),WHERE_SRC_FILTER);
        checkAndReplace(statisticsExecuteSqlDefinitionList,inputParameterValueResult.get(SRC_FILTER),AND_SRC_FILTER);
        checkAndReplace(statisticsExecuteSqlDefinitionList,inputParameterValueResult.get(SRC_FILTER),WHERE_SRC_FILTER);

        checkAndReplace(midExecuteSqlDefinitionList,inputParameterValueResult.get(TARGET_FILTER),AND_TARGET_FILTER);
        checkAndReplace(midExecuteSqlDefinitionList,inputParameterValueResult.get(TARGET_FILTER),WHERE_TARGET_FILTER);
        checkAndReplace(statisticsExecuteSqlDefinitionList,inputParameterValueResult.get(TARGET_FILTER),AND_TARGET_FILTER);
        checkAndReplace(statisticsExecuteSqlDefinitionList,inputParameterValueResult.get(TARGET_FILTER),WHERE_TARGET_FILTER);

        if (CollectionUtils.isNotEmpty(midExecuteSqlDefinitionList)) {
            for (DqRuleExecuteSql executeSqlDefinition:midExecuteSqlDefinitionList) {
                index = setTransformerConfig(
                        index,
                        inputParameterValueResult,
                        transformerConfigList,
                        executeSqlDefinition);
            }
        }

        if (CollectionUtils.isNotEmpty(statisticsExecuteSqlDefinitionList)) {
            for (DqRuleExecuteSql executeSqlDefinition:statisticsExecuteSqlDefinitionList) {
                index = setTransformerConfig(
                        index,
                        inputParameterValueResult,
                        transformerConfigList,
                        executeSqlDefinition);
            }
        }

        return index;
    }

    private static int setTransformerConfig(int index,
                                     Map<String, String> inputParameterValueResult,
                                     List<BaseConfig> transformerConfigList,
                                     DqRuleExecuteSql executeSqlDefinition) {
        Map<String,Object> config = new HashMap<>();
        config.put(INDEX,index++);
        config.put(SQL,ParameterUtils.convertParameterPlaceholders(executeSqlDefinition.getSql(),inputParameterValueResult));
        config.put(OUTPUT_TABLE,executeSqlDefinition.getTableAlias());

        BaseConfig transformerConfig = new BaseConfig(SQL,config);
        transformerConfigList.add(transformerConfig);
        return index;
    }

    public static List<BaseConfig> getSingleTableCustomSqlTransformerConfigList(int index,
            Map<String, String> inputParameterValueResult) {
        List<BaseConfig> list = new ArrayList<>();

        Map<String,Object> config = new HashMap<>();
        config.put(INDEX,index + 1);
        config.put(SQL,ParameterUtils.convertParameterPlaceholders(inputParameterValueResult.get(STATISTICS_EXECUTE_SQL),inputParameterValueResult));
        config.put(OUTPUT_TABLE,inputParameterValueResult.get(SRC_TABLE));
        inputParameterValueResult.put(STATISTICS_TABLE,inputParameterValueResult.get(SRC_TABLE));
        BaseConfig transformerConfig = new BaseConfig(SQL,config);
        list.add(transformerConfig);
        return  list;
    }

    private static String getCoalesceString(String table, String column) {
        return "coalesce(" + table + "." + column + ", '')";
    }

    private static String getSrcColumnIsNullStr(String table,List<String> columns) {
        String[] columnList = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columnList[i] = table + "." + column + " IS NULL";
        }
        return  String.join(AND, columnList);
    }

    public static Map<String,String> getInputParameterMapFromEntryList(List<DqRuleInputEntry> defaultInputEntryList) {

        Map<String,String> defaultInputParameterValue = new HashMap<>();
        for (DqRuleInputEntry inputEntry:defaultInputEntryList) {
            defaultInputParameterValue.put(inputEntry.getField(),inputEntry.getValue());
        }

        return defaultInputParameterValue;
    }

    public static List<BaseConfig> getWriterConfigList(
            String sql,
            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<BaseConfig> writerConfigList = new ArrayList<>();
        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getWriterConnectorType())) {
            BaseDataSource writerDataSource = DataSourceFactory.getDatasource(DbType.of(dataQualityTaskExecutionContext.getWriterType()),
                    dataQualityTaskExecutionContext.getWriterConnectionParams());
            BaseConfig writerConfig = new BaseConfig();
            writerConfig.setType(dataQualityTaskExecutionContext.getWriterConnectorType());

            Map<String,Object> config = new HashMap<>();
            if (writerDataSource != null) {
                config.put(DATABASE,writerDataSource.getDatabase());
                config.put(TABLE,dataQualityTaskExecutionContext.getWriterTable());
                config.put(URL,writerDataSource.getJdbcUrl());
                config.put(USER,writerDataSource.getUser());
                config.put(PASSWORD,writerDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getWriterType())));
                config.put(SQL,sql);
            }
            writerConfig.setConfig(config);
            writerConfigList.add(writerConfig);
        }

        return writerConfigList;
    }

    public static void addStatisticsValueTableReaderConfig (List<BaseConfig> readerConfigList,
                                                            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        if (dataQualityTaskExecutionContext.isComparisonNeedStatisticsValueTable()) {
            List<BaseConfig> statisticsBaseConfigList = RuleParserUtils.getStatisticsValueConfigReaderList(dataQualityTaskExecutionContext);
            readerConfigList.addAll(statisticsBaseConfigList);
        }
    }

    public static List<BaseConfig> getStatisticsValueConfigWriterList (
            String sql,
            Map<String, String> inputParameterValueResult,
            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<BaseConfig> writerConfigList = new ArrayList<>();
        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getStatisticsValueConnectorType())) {
            BaseConfig writerConfig = getStatisticsValueConfig(dataQualityTaskExecutionContext);
            if (writerConfig != null) {
                writerConfig.getConfig().put(SQL,ParameterUtils.convertParameterPlaceholders(sql,inputParameterValueResult));
            }
            writerConfigList.add(writerConfig);
        }
        return writerConfigList;
    }

    public static List<BaseConfig> getStatisticsValueConfigReaderList (
            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<BaseConfig> readerConfigList = new ArrayList<>();
        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getStatisticsValueConnectorType())) {
            BaseConfig readerConfig = getStatisticsValueConfig(dataQualityTaskExecutionContext);
            if (readerConfig != null) {
                readerConfig.getConfig().put(OUTPUT_TABLE,dataQualityTaskExecutionContext.getStatisticsValueTable());
            }
            readerConfigList.add(readerConfig);
        }
        return readerConfigList;
    }

    public static BaseConfig getStatisticsValueConfig (
            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {
        BaseConfig baseConfig = null;
        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getStatisticsValueConnectorType())) {
            BaseDataSource writerDataSource = DataSourceFactory.getDatasource(DbType.of(dataQualityTaskExecutionContext.getStatisticsValueType()),
                    dataQualityTaskExecutionContext.getStatisticsValueWriterConnectionParams());
            baseConfig = new BaseConfig();
            baseConfig.setType(dataQualityTaskExecutionContext.getStatisticsValueConnectorType());

            Map<String,Object> config = new HashMap<>();
            if (writerDataSource != null) {
                config.put(DATABASE,writerDataSource.getDatabase());
                config.put(TABLE,dataQualityTaskExecutionContext.getStatisticsValueTable());
                config.put(URL,writerDataSource.getJdbcUrl());
                config.put(USER,writerDataSource.getUser());
                config.put(PASSWORD,writerDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getWriterType())));
            }

            baseConfig.setConfig(config);
        }

        return baseConfig;
    }

    public static String getOnClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.size()];
        for (int i = 0; i < mappingColumnList.size(); i++) {
            MappingColumn column = mappingColumnList.get(i);
            columnList[i] = getCoalesceString(inputParameterValueResult.get(SRC_TABLE),column.getSrcField())
                    + column.getOperator()
                    + getCoalesceString(inputParameterValueResult.get(TARGET_TABLE),column.getTargetField());
        }

        return String.join(AND,columnList);
    }

    public static String getWhereClause(List<MappingColumn> mappingColumnList,Map<String,String> inputParameterValueResult) {
        String srcColumnNotNull = "( NOT (" + getSrcColumnIsNullStr(inputParameterValueResult.get(SRC_TABLE),getSrcColumnList(mappingColumnList)) + " ))";
        String targetColumnIsNull = "( " + getSrcColumnIsNullStr(inputParameterValueResult.get(TARGET_TABLE),getTargetColumnList(mappingColumnList)) + " )";

        return srcColumnNotNull + AND + targetColumnIsNull;
    }

    public static List<BaseConfig> getWriterConfigList(
                                                  int index,
                                                  Map<String, String> inputParameterValueResult,
                                                  List<BaseConfig> transformerConfigList,
                                                  DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                                  String writerSql) throws DolphinException {
        List<DqRuleExecuteSql> comparisonExecuteSqlList =
                getExecuteSqlListByType(dataQualityTaskExecutionContext.getExecuteSqlList(), ExecuteSqlType.COMPARISON);

        if (CollectionUtils.isNotEmpty(comparisonExecuteSqlList)) {
            DqRuleExecuteSql comparisonSql = comparisonExecuteSqlList.get(0);
            inputParameterValueResult.put(COMPARISON_TABLE,comparisonSql.getTableAlias());

            checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(SRC_FILTER),AND_SRC_FILTER);
            checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(SRC_FILTER),WHERE_SRC_FILTER);
            checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(TARGET_FILTER),AND_TARGET_FILTER);
            checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(TARGET_FILTER),WHERE_TARGET_FILTER);

            for (DqRuleExecuteSql executeSqlDefinition:comparisonExecuteSqlList) {
                index = setTransformerConfig(
                        index,
                        inputParameterValueResult,
                        transformerConfigList,
                        executeSqlDefinition);
            }
        }

        return getWriterConfigList(
                ParameterUtils.convertParameterPlaceholders(writerSql,inputParameterValueResult),
                dataQualityTaskExecutionContext
                );
    }

    public static List<BaseConfig> getAllWriterConfigList (
            Map<String, String> inputParameterValue,
            DataQualityTaskExecutionContext context,
            int index,
            List<BaseConfig> transformerConfigList,
            String writerSql,
            String statisticsValueWriterSql) {

        List<BaseConfig> writerConfigList = RuleParserUtils.getWriterConfigList(
                index,
                inputParameterValue,
                transformerConfigList,
                context,
                writerSql);

        writerConfigList.addAll(
                RuleParserUtils.getStatisticsValueConfigWriterList(
                        statisticsValueWriterSql,
                        inputParameterValue,
                        context));

        BaseConfig errorOutputWriter = RuleParserUtils.getErrorOutputWriter(inputParameterValue,context);
        if (errorOutputWriter != null) {
            writerConfigList.add(errorOutputWriter);
        }

        return writerConfigList;
    }

    public static List<DqRuleExecuteSql> getExecuteSqlListByType(
            List<DqRuleExecuteSql> allExecuteSqlList, ExecuteSqlType executeSqlType) {
        if (CollectionUtils.isEmpty(allExecuteSqlList)) {
            return allExecuteSqlList;
        }

        return allExecuteSqlList
                .stream()
                .filter(x -> x.getType() == executeSqlType)
                .collect(Collectors.toList());
    }

    private static void checkAndReplace(List<DqRuleExecuteSql> list, String checkValue, String replaceSrc) {
        if (StringUtils.isEmpty(checkValue) && CollectionUtils.isNotEmpty(list)) {
            for (DqRuleExecuteSql executeSqlDefinition:list) {
                String sql = executeSqlDefinition.getSql();
                sql = sql.replace(replaceSrc,"");
                executeSqlDefinition.setSql(sql);
            }
        }
    }

    public static List<MappingColumn> getMappingColumnList(String mappingColumns) {
        ArrayNode mappingColumnList = JSONUtils.parseArray(mappingColumns);
        List<MappingColumn> list = new ArrayList<>();
        mappingColumnList.forEach(item -> {
            MappingColumn column = new MappingColumn(
                    String.valueOf(item.get(Constants.SRC_FIELD)).replace("\"",""),
                    String.valueOf(item.get(Constants.OPERATOR)).replace("\""," "),
                    String.valueOf(item.get(Constants.TARGET_FIELD)).replace("\"",""));
            list.add(column);
        });

        return list;
    }

    public static List<String> getSrcColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
            list.add(item.getSrcField())
        );

        return list;
    }

    public static List<String> getTargetColumnList(List<MappingColumn> mappingColumns) {
        List<String> list = new ArrayList<>();
        mappingColumns.forEach(item ->
            list.add(item.getTargetField())
        );

        return list;
    }

    public static EnvConfig getEnvConfig() {
        EnvConfig envConfig = new EnvConfig();
        envConfig.setType(BATCH);
        return  envConfig;
    }

    public static BaseConfig getErrorOutputWriter(Map<String, String> inputParameterValueResult,
                                                 DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DqRuleExecuteSql errorOutputSql = null;
        if (CollectionUtils.isEmpty(dataQualityTaskExecutionContext.getExecuteSqlList())) {
            return null;
        }

        for (DqRuleExecuteSql executeSql : dataQualityTaskExecutionContext.getExecuteSqlList()) {
            if (executeSql.isErrorOutputSql()) {
                errorOutputSql = executeSql;
                break;
            }
        }

        BaseConfig baseConfig = null;
        if (StringUtils.isNotEmpty(inputParameterValueResult.get(ERROR_OUTPUT_PATH))
                && errorOutputSql != null) {
            baseConfig = new BaseConfig();
            Map<String,Object> config = new HashMap<>();
            config.put(PATH,inputParameterValueResult.get(ERROR_OUTPUT_PATH));
            config.put(INPUT_TABLE,errorOutputSql.getTableAlias());
            baseConfig.setConfig(config);
            baseConfig.setType(HDFS_FILE);
        }

        return baseConfig;
    }

    public static String generateUniqueCode(Map<String, String> inputParameterValue) {

        if (MapUtils.isEmpty(inputParameterValue)) {
            return "-1";
        }

        Map<String,String> newInputParameterValue = new HashMap<>(inputParameterValue);

        newInputParameterValue.remove("rule_type");
        newInputParameterValue.remove("rule_name");
        newInputParameterValue.remove("create_time");
        newInputParameterValue.remove("update_time");
        newInputParameterValue.remove("process_definition_id");
        newInputParameterValue.remove("process_instance_id");
        newInputParameterValue.remove("task_instance_id");
        newInputParameterValue.remove("check_type");
        newInputParameterValue.remove("operator");
        newInputParameterValue.remove("threshold");
        newInputParameterValue.remove("failure_strategy");
        newInputParameterValue.remove("operator");
        newInputParameterValue.remove("threshold");
        newInputParameterValue.remove("data_time");
        newInputParameterValue.remove("error_output_path");
        newInputParameterValue.remove("comparison_type");
        newInputParameterValue.remove("comparison_name");
        newInputParameterValue.remove("comparison_table");
        newInputParameterValue.remove(PARAMETER_CURRENT_DATE);
        newInputParameterValue.remove(PARAMETER_BUSINESS_DATE);
        newInputParameterValue.remove(PARAMETER_DATETIME);

        StringBuilder sb = new StringBuilder();
        for (String value : newInputParameterValue.values()) {
            sb.append(value);
        }

        return Md5Utils.getMd5(sb.toString(),true);
    }
}
