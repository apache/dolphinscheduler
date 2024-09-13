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

package org.apache.dolphinscheduler.plugin.task.dq.rule;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SINGLE_QUOTES;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.UNIQUE_CODE;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.RuleType;
import org.apache.dolphinscheduler.plugin.task.dq.exception.DataQualityException;
import org.apache.dolphinscheduler.plugin.task.dq.rule.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.DataQualityConfiguration;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parser.IRuleParser;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parser.MultiTableAccuracyRuleParser;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parser.MultiTableComparisonRuleParser;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parser.SingleTableCustomSqlRuleParser;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parser.SingleTableRuleParser;
import org.apache.dolphinscheduler.plugin.task.dq.utils.RuleParserUtils;

import java.util.Date;
import java.util.Map;

/**
 * RuleManager is responsible for parsing the input parameters to the DataQualityConfiguration
 * And DataQualityConfiguration will be used in DataQualityApplication
 */
public class RuleManager {

    private final Map<String, String> inputParameterValue;
    private final DataQualityTaskExecutionContext dataQualityTaskExecutionContext;

    private static final String NONE_COMPARISON_TYPE = "0";
    private static final String BASE_SQL =
            "select ${rule_type} as rule_type,"
                    + "${rule_name} as rule_name,"
                    + "${process_definition_id} as process_definition_id,"
                    + "${process_instance_id} as process_instance_id,"
                    + "${task_instance_id} as task_instance_id,"
                    + "${statistics_name} AS statistics_value,"
                    + "${comparison_name} AS comparison_value,"
                    + "${comparison_type} AS comparison_type,"
                    + "${check_type} as check_type,"
                    + "${threshold} as threshold,"
                    + "${operator} as operator,"
                    + "${failure_strategy} as failure_strategy,"
                    + "'${error_output_path}' as error_output_path,"
                    + "${create_time} as create_time,"
                    + "${update_time} as update_time ";

    public static final String DEFAULT_COMPARISON_WRITER_SQL =
            BASE_SQL + "from ${statistics_table} full join ${comparison_table}";

    public static final String MULTI_TABLE_COMPARISON_WRITER_SQL =
            BASE_SQL
                    + "from ( ${statistics_execute_sql} ) tmp1 "
                    + "join ( ${comparison_execute_sql} ) tmp2";

    public static final String SINGLE_TABLE_CUSTOM_SQL_WRITER_SQL =
            BASE_SQL
                    + "from ( ${statistics_table} ) tmp1 "
                    + "join ${comparison_table}";
    public static final String TASK_STATISTICS_VALUE_WRITER_SQL =
            "select "
                    + "${process_definition_id} as process_definition_id,"
                    + "${task_instance_id} as task_instance_id,"
                    + "${rule_id} as rule_id,"
                    + "${unique_code} as unique_code,"
                    + "'${statistics_name}'AS statistics_name,"
                    + "${statistics_name} AS statistics_value,"
                    + "${data_time} as data_time,"
                    + "${create_time} as create_time,"
                    + "${update_time} as update_time "
                    + "from ${statistics_table}";

    public RuleManager(Map<String, String> inputParameterValue,
                       DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        this.inputParameterValue = inputParameterValue;
        this.dataQualityTaskExecutionContext = dataQualityTaskExecutionContext;
    }

    /**
     * @return DataQualityConfiguration
     * @throws RuntimeException RuntimeException
     */
    public DataQualityConfiguration generateDataQualityParameter() throws RuntimeException {

        Map<String, String> inputParameterValueResult =
                RuleParserUtils.getInputParameterMapFromEntryList(
                        JSONUtils.toList(dataQualityTaskExecutionContext.getRuleInputEntryList(),
                                DqRuleInputEntry.class));
        inputParameterValueResult.putAll(inputParameterValue);
        inputParameterValueResult
                .putAll(BusinessTimeUtils.getBusinessTime(CommandType.START_PROCESS, new Date(), null));
        inputParameterValueResult.putIfAbsent(COMPARISON_TYPE, NONE_COMPARISON_TYPE);
        inputParameterValueResult.put(UNIQUE_CODE,
                SINGLE_QUOTES + RuleParserUtils.generateUniqueCode(inputParameterValueResult) + SINGLE_QUOTES);

        IRuleParser ruleParser = null;
        switch (RuleType.of(dataQualityTaskExecutionContext.getRuleType())) {
            case SINGLE_TABLE:
                ruleParser = new SingleTableRuleParser();
                break;
            case SINGLE_TABLE_CUSTOM_SQL:
                ruleParser = new SingleTableCustomSqlRuleParser();
                break;
            case MULTI_TABLE_ACCURACY:
                ruleParser = new MultiTableAccuracyRuleParser();
                break;
            case MULTI_TABLE_COMPARISON:
                ruleParser = new MultiTableComparisonRuleParser();
                break;
            default:
                throw new DataQualityException("rule type is not support");
        }

        return ruleParser.parse(inputParameterValueResult, dataQualityTaskExecutionContext);
    }
}
