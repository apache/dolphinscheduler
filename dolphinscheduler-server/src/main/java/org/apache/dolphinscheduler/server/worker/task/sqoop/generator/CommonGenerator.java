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

package org.apache.dolphinscheduler.server.worker.task.sqoop.generator;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common script generator
 */
public class CommonGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CommonGenerator.class);

    public String generate(SqoopParameters sqoopParameters) {

        StringBuilder commonSb = new StringBuilder();

        try {
            //sqoop task model
            commonSb.append(SqoopConstants.SQOOP)
                .append(Constants.SPACE)
                .append(sqoopParameters.getModelType());

            //sqoop map-reduce job name
            commonSb.append(Constants.SPACE).append(Constants.D).append(Constants.SPACE)
                .append(String.format("%s%s%s", SqoopConstants.SQOOP_MR_JOB_NAME,
                    Constants.EQUAL_SIGN, sqoopParameters.getJobName()));

            //hadoop custom param
            List<Property> hadoopCustomParams = sqoopParameters.getHadoopCustomParams();
            if (CollectionUtils.isNotEmpty(hadoopCustomParams)) {
                StringBuilder hadoopCustomParamStr = new StringBuilder();
                for (Property hadoopCustomParam : hadoopCustomParams) {
                    hadoopCustomParamStr.append(Constants.D)
                            .append(Constants.SPACE).append(hadoopCustomParam.getProp())
                            .append(Constants.EQUAL_SIGN).append(hadoopCustomParam.getValue())
                            .append(Constants.SPACE);
                }
                commonSb.append(Constants.SPACE).append(hadoopCustomParamStr.substring(0, hadoopCustomParamStr.length() - 1));
            }

            //sqoop custom params
            List<Property> sqoopAdvancedParams = sqoopParameters.getSqoopAdvancedParams();
            if (CollectionUtils.isNotEmpty(sqoopAdvancedParams)) {
                for (Property sqoopAdvancedParam : sqoopAdvancedParams) {
                    commonSb.append(Constants.SPACE).append(sqoopAdvancedParam.getProp())
                        .append(Constants.SPACE).append(sqoopAdvancedParam.getValue());
                }
            }

            //sqoop parallelism
            if (sqoopParameters.getConcurrency() > 0) {
                commonSb.append(Constants.SPACE).append(SqoopConstants.SQOOP_PARALLELISM)
                    .append(Constants.SPACE).append(sqoopParameters.getConcurrency());
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop task general param build failed: [%s]", e.getMessage()));
        }

        return commonSb.toString();
    }
}
