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

package org.apache.dolphinscheduler.data.quality.flow.writer;

import org.apache.dolphinscheduler.data.quality.configuration.WriterParameter;
import org.apache.dolphinscheduler.data.quality.context.DataQualityContext;
import org.apache.dolphinscheduler.data.quality.enums.WriterType;

import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

/**
 * WriterFactory
 */
public class WriterFactory {

    private static class Singleton {
        static WriterFactory instance = new WriterFactory();
    }

    public static WriterFactory getInstance() {
        return Singleton.instance;
    }

    public List<IWriter> getWriters(DataQualityContext context) throws Exception {

        List<IWriter> writerList = new ArrayList<>();

        for (WriterParameter writerParam:context.getWriterParamList()) {
            IWriter writer = getWriter(context.getSparkSession(),writerParam);
            if (writer != null) {
                writerList.add(writer);
            }
        }

        return writerList;
    }

    private IWriter getWriter(SparkSession sparkSession,WriterParameter writerParam) throws Exception {
        WriterType writerType = WriterType.getType(writerParam.getType());
        if (writerType != null) {
            if (writerType == WriterType.JDBC) {
                return new JdbcWriter(sparkSession, writerParam);
            }
            throw new Exception("writer type $readerType is not supported!");
        }

        return null;
    }

}
