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

package org.apache.dolphinscheduler.server.worker.task.dq.rule.parser;

import static org.apache.dolphinscheduler.server.worker.task.dq.rule.RuleManager.MULTI_TABLE_COMPARISON_WRITER_SQL;

import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.entity.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.RuleParserUtils;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.ConnectorParameter;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.DataQualityConfiguration;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.ExecutorParameter;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter.WriterParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MultiTableComparisonRuleParser
 */
public class MultiTableComparisonRuleParser implements IRuleParser {

    @Override
    public DataQualityConfiguration parse(Map<String, String> inputParameterValue,
                                          DataQualityTaskExecutionContext context) throws Exception {

        List<ConnectorParameter> connectorParameterList =
                RuleParserUtils.getConnectorParameterList(inputParameterValue,context);
        List<ExecutorParameter> executorParameterList = new ArrayList<>();

        List<WriterParameter> writerParameterList = RuleParserUtils.getWriterParameterList(
                ParameterUtils.convertParameterPlaceholders(MULTI_TABLE_COMPARISON_WRITER_SQL,inputParameterValue),
                context);

        return new DataQualityConfiguration(
                context.getRuleName(),
                connectorParameterList,
                writerParameterList,
                executorParameterList);
    }
}
