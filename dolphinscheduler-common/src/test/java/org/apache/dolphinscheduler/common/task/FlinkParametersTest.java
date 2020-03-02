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

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class FlinkParametersTest {
    @Test
    public void getResourceFilesList() {
        FlinkParameters flinkParameters = new FlinkParameters();
        Assert.assertNotNull(flinkParameters.getResourceFilesList());
        Assert.assertTrue(flinkParameters.getResourceFilesList().isEmpty());

        flinkParameters.setResourceList(Collections.singletonList(new ResourceInfo()));
        Assert.assertNotNull(flinkParameters.getResourceFilesList());
        Assert.assertEquals(1, flinkParameters.getResourceFilesList().size());
    }
}
