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

package org.apache.dolphinscheduler.plugin.task.dvc;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DvcTaskTest {

    public TaskExecutionContext createContext(DvcParameters dvcParameters) {
        String parameters = JSONUtils.toJsonString(dvcParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);

        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        return taskExecutionContext;
    }

    private DvcTask initTask(DvcParameters parameters) {
        TaskExecutionContext taskExecutionContext = createContext(parameters);
        DvcTask dvcTask = new DvcTask(taskExecutionContext);
        dvcTask.init();
        dvcTask.getParameters().setVarPool(taskExecutionContext.getVarPool());
        return dvcTask;

    }

    @Test
    public void testDvcUpload() throws Exception {
        DvcTask dvcTask = initTask(createUploadParameters());
        Assertions.assertEquals(dvcTask.buildCommand(),
                "which dvc || { echo \"dvc does not exist\"; exit 1; }; DVC_REPO=git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example\n"
                        +
                        "DVC_DATA_PATH=/home/<YOUR-NAME-OR-ORG>/test\n" +
                        "DVC_DATA_LOCATION=test\n" +
                        "DVC_VERSION=iris_v2.3.1\n" +
                        "DVC_MESSAGE=\"add test iris data\"\n" +
                        "git clone $DVC_REPO dvc-repository; cd dvc-repository; pwd\n" +
                        "dvc config core.autostage true --local || exit 1\n" +
                        "dvc add $DVC_DATA_PATH -v -o $DVC_DATA_LOCATION --to-remote || exit 1\n" +
                        "git commit -am \"$DVC_MESSAGE\"\n" +
                        "git tag \"$DVC_VERSION\" -m \"$DVC_MESSAGE\"\n" +
                        "git push --all\n" +
                        "git push --tags");

    }

    @Test
    public void testDvcDownload() throws Exception {
        DvcTask dvcTask = initTask(createDownloadParameters());
        Assertions.assertEquals(dvcTask.buildCommand(),
                "which dvc || { echo \"dvc does not exist\"; exit 1; }; DVC_REPO=git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example\n"
                        +
                        "DVC_DATA_PATH=data\n" +
                        "DVC_DATA_LOCATION=iris\n" +
                        "DVC_VERSION=iris_v2.3.1\n" +
                        "dvc get $DVC_REPO $DVC_DATA_LOCATION -o $DVC_DATA_PATH -v --rev $DVC_VERSION");
    }

    @Test
    public void testInitDvc() throws Exception {
        DvcTask dvcTask = initTask(createInitDvcParameters());
        Assertions.assertEquals(dvcTask.buildCommand(),
                "which dvc || { echo \"dvc does not exist\"; exit 1; }; DVC_REPO=git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example\n"
                        +
                        "git clone $DVC_REPO dvc-repository; cd dvc-repository; pwd\n" +
                        "dvc init || exit 1\n" +
                        "dvc remote add origin ~/.dvc_test -d\n" +
                        "git commit -am \"init dvc project and add remote\"; git push");
    }

    private DvcParameters createUploadParameters() {
        DvcParameters parameters = new DvcParameters();
        parameters.setDvcTaskType(DvcConstants.DVC_TASK_TYPE.UPLOAD);
        parameters.setDvcRepository("git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example");
        parameters.setDvcLoadSaveDataPath("/home/<YOUR-NAME-OR-ORG>/test");
        parameters.setDvcDataLocation("test");
        parameters.setDvcVersion("iris_v2.3.1");
        parameters.setDvcMessage("add test iris data");
        return parameters;
    }

    private DvcParameters createDownloadParameters() {
        DvcParameters parameters = new DvcParameters();
        parameters.setDvcTaskType(DvcConstants.DVC_TASK_TYPE.DOWNLOAD);
        parameters.setDvcRepository("git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example");
        parameters.setDvcLoadSaveDataPath("data");
        parameters.setDvcDataLocation("iris");
        parameters.setDvcVersion("iris_v2.3.1");
        return parameters;
    }

    private DvcParameters createInitDvcParameters() {
        DvcParameters parameters = new DvcParameters();
        parameters.setDvcTaskType(DvcConstants.DVC_TASK_TYPE.INIT);
        parameters.setDvcRepository("git@github.com:<YOUR-NAME-OR-ORG>/dvc-data-repository-example");
        parameters.setDvcStoreUrl("~/.dvc_test");
        return parameters;
    }
}
