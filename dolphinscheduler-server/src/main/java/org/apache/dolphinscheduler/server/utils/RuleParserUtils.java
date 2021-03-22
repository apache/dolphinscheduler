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
import static org.apache.dolphinscheduler.common.Constants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.common.Constants.DATABASE;
import static org.apache.dolphinscheduler.common.Constants.DRIVER;
import static org.apache.dolphinscheduler.common.Constants.PASSWORD;
import static org.apache.dolphinscheduler.common.Constants.SQL;
import static org.apache.dolphinscheduler.common.Constants.SRC_FILTER;
import static org.apache.dolphinscheduler.common.Constants.SRC_TABLE;
import static org.apache.dolphinscheduler.common.Constants.TABLE;
import static org.apache.dolphinscheduler.common.Constants.TARGET_FILTER;
import static org.apache.dolphinscheduler.common.Constants.TARGET_TABLE;
import static org.apache.dolphinscheduler.common.Constants.URL;
import static org.apache.dolphinscheduler.common.Constants.USER;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.dq.ExecuteSqlType;
import org.apache.dolphinscheduler.common.exception.DolphinException;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.server.entity.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.ConnectorParameter;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.ExecutorParameter;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.WriterParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<ConnectorParameter> getConnectorParameterList(
                                            Map<String, String> inputParameterValue,
                                            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<ConnectorParameter> connectorParameterList = new ArrayList<>();

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getSourceConnectorType())) {
            BaseDataSource baseDataSource = DataSourceFactory.getDatasource
                    (DbType.of(dataQualityTaskExecutionContext.getSourceType()),
                    dataQualityTaskExecutionContext.getSourceConnectionParams());
            ConnectorParameter sourceConnectorParameter = new ConnectorParameter();
            sourceConnectorParameter.setType(dataQualityTaskExecutionContext.getSourceConnectorType());
            Map<String,Object> config = new HashMap<>();
            if (baseDataSource != null) {
                config.put(DATABASE,baseDataSource.getDatabase());
                config.put(TABLE,inputParameterValue.get(SRC_TABLE));
                config.put(URL,baseDataSource.getJdbcUrl());
                config.put(USER,baseDataSource.getUser());
                config.put(PASSWORD,baseDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getSourceType())));
            }
            sourceConnectorParameter.setConfig(config);

            connectorParameterList.add(sourceConnectorParameter);
        }

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getTargetConnectorType())) {
            BaseDataSource targetDataSource = DataSourceFactory.getDatasource(DbType.of(dataQualityTaskExecutionContext.getTargetType()),
                    dataQualityTaskExecutionContext.getTargetConnectionParams());
            ConnectorParameter targetConnectorParameter = new ConnectorParameter();
            targetConnectorParameter.setType(dataQualityTaskExecutionContext.getTargetConnectorType());
            Map<String,Object> config = new HashMap<>();
            if (targetDataSource != null) {
                config.put(DATABASE,targetDataSource.getDatabase());
                config.put(TABLE,inputParameterValue.get(TARGET_TABLE));
                config.put(URL,targetDataSource.getJdbcUrl());
                config.put(USER,targetDataSource.getUser());
                config.put(PASSWORD,targetDataSource.getPassword());
                config.put(DRIVER, DataSourceFactory.getDriver(DbType.of(dataQualityTaskExecutionContext.getTargetType())));
            }
            targetConnectorParameter.setConfig(config);

            connectorParameterList.add(targetConnectorParameter);
        }

        return connectorParameterList;
    }

    public static int replaceExecuteSqlPlaceholder(List<DqRuleExecuteSql> executeSqlList,
                                             int index, Map<String, String> inputParameterValueResult,
                                             List<ExecutorParameter> executorParameterList) {
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

        if (midExecuteSqlDefinitionList != null) {
            for (DqRuleExecuteSql executeSqlDefinition:midExecuteSqlDefinitionList) {
                index = setExecutorParameter(
                        index,
                        inputParameterValueResult,
                        executorParameterList,
                        executeSqlDefinition);
            }
        }

        for (DqRuleExecuteSql executeSqlDefinition:statisticsExecuteSqlDefinitionList) {
            index = setExecutorParameter(
                    index,
                    inputParameterValueResult,
                    executorParameterList,
                    executeSqlDefinition);
        }

        return index;
    }

    private static int setExecutorParameter(int index,
                                     Map<String, String> inputParameterValueResult,
                                     List<ExecutorParameter> executorParameterList,
                                            DqRuleExecuteSql executeSqlDefinition) {
        ExecutorParameter executorParameter = new ExecutorParameter();
        executorParameter.setIndex(index++ + "");
        executorParameter.setExecuteSql(ParameterUtils.convertParameterPlaceholders(executeSqlDefinition.getSql(),inputParameterValueResult));
        executorParameter.setTableAlias(executeSqlDefinition.getTableAlias());
        executorParameterList.add(executorParameter);
        return index;
    }

    private static String getCoalesceString(String table, String column) {
        return "coalesce(" + table + "." + column + ", '')";
    }

    private static String getSrcColumnIsNullStr(String table,String[] columns) {
        String[] columnList = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
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

    public static List<WriterParameter> getWriterParameterList(
            String sql,
            DataQualityTaskExecutionContext dataQualityTaskExecutionContext) throws DolphinException {

        List<WriterParameter> writerParameterList = new ArrayList<>();

        if (StringUtils.isNotEmpty(dataQualityTaskExecutionContext.getWriterConnectorType())) {
            BaseDataSource writerDataSource = DataSourceFactory.getDatasource(DbType.of(dataQualityTaskExecutionContext.getWriterType()),
                    dataQualityTaskExecutionContext.getWriterConnectionParams());
            WriterParameter writerParameter = new WriterParameter();
            writerParameter.setType(dataQualityTaskExecutionContext.getWriterConnectorType());
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
            writerParameter.setConfig(config);

            writerParameterList.add(writerParameter);
        }

        return writerParameterList;

    }

    public static String getOnClause(String[] mappingColumnList,Map<String,String> inputParameterValueResult) {
        //get on clause
        String[] columnList = new String[mappingColumnList.length];
        for (int i = 0; i < mappingColumnList.length; i++) {
            String column = mappingColumnList[i];
            columnList[i] = getCoalesceString(inputParameterValueResult.get(SRC_TABLE),column)
                    + " = "
                    + getCoalesceString(inputParameterValueResult.get(TARGET_TABLE),column);
        }

        return String.join(AND,columnList);
    }

    public static String getWhereClause(String[] mappingColumnList,Map<String,String> inputParameterValueResult) {
        String srcColumnNotNull = "( NOT (" + getSrcColumnIsNullStr(inputParameterValueResult.get(SRC_TABLE),mappingColumnList) + " ))";
        String targetColumnIsNull = "( " + getSrcColumnIsNullStr(inputParameterValueResult.get(TARGET_TABLE),mappingColumnList) + " )";

        return srcColumnNotNull + AND + targetColumnIsNull;
    }

    public static List<WriterParameter> getWriterParameterList(
                                                  int index,
                                                  Map<String, String> inputParameterValueResult,
                                                  List<ExecutorParameter> executorParameterList,
                                                  DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                                  String writerSql) throws DolphinException {
        List<DqRuleExecuteSql> comparisonExecuteSqlList =
                getExecuteSqlListByType(dataQualityTaskExecutionContext.getExecuteSqlList(), ExecuteSqlType.COMPARISON);

        DqRuleExecuteSql comparisonSql = comparisonExecuteSqlList.get(0);
        inputParameterValueResult.put(COMPARISON_TABLE,comparisonSql.getTableAlias());

        checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(SRC_FILTER),AND_SRC_FILTER);
        checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(SRC_FILTER),WHERE_SRC_FILTER);
        checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(TARGET_FILTER),AND_TARGET_FILTER);
        checkAndReplace(comparisonExecuteSqlList,inputParameterValueResult.get(TARGET_FILTER),WHERE_TARGET_FILTER);

        for (DqRuleExecuteSql executeSqlDefinition:comparisonExecuteSqlList) {
            index = setExecutorParameter(
                    index,
                    inputParameterValueResult,
                    executorParameterList,
                    executeSqlDefinition);
        }

        return getWriterParameterList(
                ParameterUtils.convertParameterPlaceholders(writerSql,inputParameterValueResult),
                dataQualityTaskExecutionContext
                );
    }

    public static List<DqRuleExecuteSql> getExecuteSqlListByType(
            List<DqRuleExecuteSql> allExecuteSqlList, ExecuteSqlType executeSqlType) {
        return allExecuteSqlList
                .stream()
                .filter(x -> x.getType() == executeSqlType)
                .collect(Collectors.toList());
    }

    private static void checkAndReplace(List<DqRuleExecuteSql> list, String checkValue, String replaceSrc) {
        if (StringUtils.isEmpty(checkValue)) {
            for (DqRuleExecuteSql executeSqlDefinition:list) {
                String sql = executeSqlDefinition.getSql();
                sql = sql.replace(replaceSrc,"");
                executeSqlDefinition.setSql(sql);
            }
        }
    }
}
