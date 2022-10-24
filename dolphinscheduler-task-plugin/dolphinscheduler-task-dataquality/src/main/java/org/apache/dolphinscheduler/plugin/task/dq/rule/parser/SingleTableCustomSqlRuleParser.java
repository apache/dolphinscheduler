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

package org.apache.dolphinscheduler.plugin.task.dq.rule.parser;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.dq.exception.DataQualityException;
import org.apache.dolphinscheduler.plugin.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.plugin.task.dq.rule.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.BaseConfig;
import org.apache.dolphinscheduler.plugin.task.dq.rule.parameter.DataQualityConfiguration;
import org.apache.dolphinscheduler.plugin.task.dq.utils.RuleParserUtils;

import java.util.List;
import java.util.Map;

/**
 * SingleTableCustomSqlRuleParser
 */
public class SingleTableCustomSqlRuleParser implements IRuleParser {

    @Override
    public DataQualityConfiguration parse(Map<String, String> inputParameterValue,
                                          DataQualityTaskExecutionContext context) throws DataQualityException {
        List<DqRuleExecuteSql> dqRuleExecuteSqlList =
                JSONUtils.toList(context.getExecuteSqlList(), DqRuleExecuteSql.class);

        int index = 1;

        List<BaseConfig> readerConfigList =
                RuleParserUtils.getReaderConfigList(inputParameterValue, context);
        RuleParserUtils.addStatisticsValueTableReaderConfig(readerConfigList, context);

        List<BaseConfig> transformerConfigList = RuleParserUtils
                .getSingleTableCustomSqlTransformerConfigList(index, inputParameterValue);

        // replace the placeholder in execute sql list
        index = RuleParserUtils.replaceExecuteSqlPlaceholder(
                dqRuleExecuteSqlList,
                index,
                inputParameterValue,
                transformerConfigList);

        String writerSql = RuleManager.SINGLE_TABLE_CUSTOM_SQL_WRITER_SQL;
        if (context.isCompareWithFixedValue()) {
            writerSql = writerSql.replaceAll("join \\$\\{comparison_table}", "");
        }

        List<BaseConfig> writerConfigList = RuleParserUtils.getAllWriterConfigList(inputParameterValue,
                context, index, transformerConfigList, writerSql, RuleManager.TASK_STATISTICS_VALUE_WRITER_SQL);

        return new DataQualityConfiguration(
                context.getRuleName(),
                RuleParserUtils.getEnvConfig(),
                readerConfigList,
                writerConfigList,
                transformerConfigList);
    }
}
