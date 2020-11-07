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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common script generator
 */
public class CommonGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String generate(SqoopParameters sqoopParameters) {

        LinkedList<String> sqoopCommonParamsList = new LinkedList<>();

        try {
            //sqoop task model
            sqoopCommonParamsList.add(SqoopConstants.SQOOP);
            sqoopCommonParamsList.add(sqoopParameters.getModelType());

            //sqoop map-reduce job name
            sqoopCommonParamsList.add(Constants.D);
            sqoopCommonParamsList.add(String.format("%s%s%s", SqoopConstants.SQOOP_MR_JOB_NAME,
                Constants.EQUAL_SIGN, sqoopParameters.getJobName()));

            //hadoop custom param
            List<Property> hadoopCustomParams = sqoopParameters.getHadoopCustomParams();
            if (CollectionUtils.isNotEmpty(hadoopCustomParams)) {
                for (Property hadoopCustomParam : hadoopCustomParams) {
                    String hadoopCustomParamStr = String.format("%s%s%s", hadoopCustomParam.getProp(),
                        Constants.EQUAL_SIGN, hadoopCustomParam.getValue());

                    sqoopCommonParamsList.add(Constants.D);
                    sqoopCommonParamsList.add(hadoopCustomParamStr);
                }
            }

            //sqoop custom params
            List<Property> sqoopAdvancedParams = sqoopParameters.getSqoopAdvancedParams();
            if (CollectionUtils.isNotEmpty(sqoopAdvancedParams)) {
                for (Property sqoopAdvancedParam : sqoopAdvancedParams) {
                    sqoopCommonParamsList.add(sqoopAdvancedParam.getProp());
                    sqoopCommonParamsList.add(sqoopAdvancedParam.getValue());
                }
            }

            //sqoop parallelism
            if (sqoopParameters.getConcurrency() > 0) {
                sqoopCommonParamsList.add(SqoopConstants.SQOOP_PARALLELISM);
                sqoopCommonParamsList.add(String.valueOf(sqoopParameters.getConcurrency()));
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop task general param build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", sqoopCommonParamsList);
    }
}
