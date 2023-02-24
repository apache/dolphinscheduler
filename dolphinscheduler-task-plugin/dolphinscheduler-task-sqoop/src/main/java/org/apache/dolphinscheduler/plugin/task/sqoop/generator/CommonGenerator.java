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

package org.apache.dolphinscheduler.plugin.task.sqoop.generator;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.D;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EQUAL_SIGN;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * common script generator
 */
@Slf4j
public class CommonGenerator {

    public String generate(SqoopParameters sqoopParameters) {

        StringBuilder commonSb = new StringBuilder();

        try {
            // sqoop task model
            commonSb.append(SqoopConstants.SQOOP)
                    .append(SPACE)
                    .append(sqoopParameters.getModelType());

            // sqoop map-reduce job name
            commonSb.append(SPACE).append(D).append(SPACE)
                    .append(String.format("%s%s%s", SqoopConstants.SQOOP_MR_JOB_NAME,
                            EQUAL_SIGN, sqoopParameters.getJobName()));

            // hadoop custom param
            List<Property> hadoopCustomParams = sqoopParameters.getHadoopCustomParams();
            if (CollectionUtils.isNotEmpty(hadoopCustomParams)) {
                for (Property hadoopCustomParam : hadoopCustomParams) {
                    String hadoopCustomParamStr = String.format("%s%s%s", hadoopCustomParam.getProp(),
                            EQUAL_SIGN, hadoopCustomParam.getValue());

                    commonSb.append(SPACE).append(D)
                            .append(SPACE).append(hadoopCustomParamStr);
                }
            }

            // sqoop custom params
            List<Property> sqoopAdvancedParams = sqoopParameters.getSqoopAdvancedParams();
            if (CollectionUtils.isNotEmpty(sqoopAdvancedParams)) {
                for (Property sqoopAdvancedParam : sqoopAdvancedParams) {
                    commonSb.append(SPACE).append(sqoopAdvancedParam.getProp())
                            .append(SPACE).append(sqoopAdvancedParam.getValue());
                }
            }

            // sqoop parallelism
            if (sqoopParameters.getConcurrency() > 0) {
                commonSb.append(SPACE).append(SqoopConstants.SQOOP_PARALLELISM)
                        .append(SPACE).append(sqoopParameters.getConcurrency());
                if (sqoopParameters.getConcurrency() > 1) {
                    commonSb.append(SPACE).append(SqoopConstants.SPLIT_BY)
                            .append(SPACE).append(sqoopParameters.getSplitBy());
                }
            }
        } catch (Exception e) {
            log.error(String.format("Sqoop task general param build failed: [%s]", e.getMessage()));
        }

        return commonSb.toString();
    }
}
