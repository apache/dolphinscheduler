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

package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.task.datax.DataxParameters;

import org.junit.Assert;
import org.junit.Test;

public class DataxParametersTest {

    /**
     * jvm parameters
     */
    public static final String JVM_EVN = " --jvm=\"-Xms%sG -Xmx%sG\" ";

    @Test
    public void testLoadJvmEnv()   {

        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setXms(0);
        dataxParameters.setXmx(-100);

        String actual =  loadJvmEnvTest(dataxParameters);

        String except = " --jvm=\"-Xms1G -Xmx1G\" ";
        Assert.assertEquals(except,actual);

        dataxParameters.setXms(13);
        dataxParameters.setXmx(14);
        actual =  loadJvmEnvTest(dataxParameters);
        except = " --jvm=\"-Xms13G -Xmx14G\" ";
        Assert.assertEquals(except,actual);

    }


    public String loadJvmEnvTest(DataxParameters dataXParameters) {
        int xms = dataXParameters.getXms() < 1 ? 1 : dataXParameters.getXms();
        int xmx = dataXParameters.getXmx() < 1 ? 1 : dataXParameters.getXmx();
        return String.format(JVM_EVN, xms, xmx);
    }
}
