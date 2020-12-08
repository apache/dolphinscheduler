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

import org.apache.dolphinscheduler.common.enums.SqoopJobType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HdfsSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HiveSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.MysqlSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HdfsTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HiveTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.MysqlTargetGenerator;

/**
 * Sqoop Job Scripts Generator
 */
public class SqoopJobGenerator {

    private static final String MYSQL = "MYSQL";
    private static final String HIVE = "HIVE";
    private static final String HDFS = "HDFS";

    /**
     * target script generator
     */
    private ITargetGenerator targetGenerator;
    /**
     * source script generator
     */
    private ISourceGenerator sourceGenerator;
    /**
     * common script generator
     */
    private final CommonGenerator commonGenerator;

    public SqoopJobGenerator() {
        commonGenerator = new CommonGenerator();
    }

    private void createSqoopJobGenerator(String sourceType, String targetType) {
        sourceGenerator = createSourceGenerator(sourceType);
        targetGenerator = createTargetGenerator(targetType);
    }

    /**
     * get the final sqoop scripts
     *
     * @param sqoopParameters sqoop params
     * @return sqoop scripts
     */
    public String generateSqoopJob(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        String sqoopScripts = "";

        if (SqoopJobType.TEMPLATE.getDescp().equals(sqoopParameters.getJobType())) {
            createSqoopJobGenerator(sqoopParameters.getSourceType(), sqoopParameters.getTargetType());
            if (sourceGenerator == null || targetGenerator == null) {
                throw new RuntimeException("sqoop task source type or target type is null");
            }

            sqoopScripts = String.format("%s%s%s", commonGenerator.generate(sqoopParameters),
                sourceGenerator.generate(sqoopParameters, taskExecutionContext),
                targetGenerator.generate(sqoopParameters, taskExecutionContext));
        } else if (SqoopJobType.CUSTOM.getDescp().equals(sqoopParameters.getJobType())) {
            sqoopScripts = sqoopParameters.getCustomShell().replaceAll("\\r\\n", "\n");
        }

        return sqoopScripts;
    }

    /**
     * get the source generator
     *
     * @param sourceType sqoop source type
     * @return sqoop source generator
     */
    private ISourceGenerator createSourceGenerator(String sourceType) {
        switch (sourceType) {
            case MYSQL:
                return new MysqlSourceGenerator();
            case HIVE:
                return new HiveSourceGenerator();
            case HDFS:
                return new HdfsSourceGenerator();
            default:
                return null;
        }
    }

    /**
     * get the target generator
     *
     * @param targetType sqoop target type
     * @return sqoop target generator
     */
    private ITargetGenerator createTargetGenerator(String targetType) {
        switch (targetType) {
            case MYSQL:
                return new MysqlTargetGenerator();
            case HIVE:
                return new HiveTargetGenerator();
            case HDFS:
                return new HdfsTargetGenerator();
            default:
                return null;
        }
    }
}
