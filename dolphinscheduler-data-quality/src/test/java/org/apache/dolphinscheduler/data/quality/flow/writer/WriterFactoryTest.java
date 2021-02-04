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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * WriterFactoryTest
 */
public class WriterFactoryTest {

    @Test
    public void testWriterGenerate() {

        DataQualityContext context = new DataQualityContext();
        List<WriterParameter> writerParameters = new ArrayList<>();
        WriterParameter writerParameter = new WriterParameter();
        writerParameter.setType("JDBC");
        writerParameter.setConfig(null);
        writerParameters.add(writerParameter);
        context.setWriterParamList(writerParameters);

        int flag = 0;
        try {
            List<IWriter> writers = WriterFactory.getInstance().getWriters(context);
            if(writers != null && writers.size() >= 1){
                flag = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1,flag);
    }
}
