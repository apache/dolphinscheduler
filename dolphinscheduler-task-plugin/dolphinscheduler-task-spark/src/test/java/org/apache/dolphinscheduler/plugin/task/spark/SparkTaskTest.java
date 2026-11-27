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

package org.apache.dolphinscheduler.plugin.task.spark;

import static org.apache.dolphinscheduler.plugin.task.spark.SparkConstants.TYPE_FILE;
import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ArgsUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SparkTaskTest {

    @Test
    public void testBuildCommandWithSparkSql() throws Exception {
        String parameters = buildSparkParametersWithSparkSql();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("5536");

        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        ResourceContext.ResourceItem resourceItem = new ResourceContext.ResourceItem();
        resourceItem.setResourceAbsolutePathInLocal("test");
        Mockito.when(resourceContext.getResourceItem(any())).thenReturn(resourceItem);

        try (MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMockedStatic
                    .when(() -> FileUtils
                            .readFileToString(any(File.class), any(Charset.class)))
                    .thenReturn("test");

            SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
            sparkTask.init();
            Assertions.assertEquals(
                    "${SPARK_HOME}/bin/spark-sql " +
                            "--master yarn " +
                            "--deploy-mode client " +
                            "--conf spark.driver.cores=1 " +
                            "--conf spark.driver.memory=512M " +
                            "--conf spark.executor.instances=2 " +
                            "--conf spark.executor.cores=2 " +
                            "--conf spark.executor.memory=1G " +
                            "--name sparksql " +
                            "-f /tmp/5536_node.sql",
                    sparkTask.getScript());
        }
    }

    @Test
    public void testBuildCommandWithSparkSubmit() {
        String parameters = buildSparkParametersWithSparkSubmit();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext.ResourceItem resourceItem = new ResourceContext.ResourceItem();
        resourceItem.setResourceAbsolutePathInStorage("/lib/dolphinscheduler-task-spark.jar");
        resourceItem.setResourceAbsolutePathInLocal("/lib/dolphinscheduler-task-spark.jar");
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem);

        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "/lib/dolphinscheduler-task-spark.jar",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithSparkSubmitMaster() {
        String parameters = buildSparkParametersWithMaster();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext.ResourceItem resourceItem = new ResourceContext.ResourceItem();
        resourceItem.setResourceAbsolutePathInStorage("/lib/dolphinscheduler-task-spark.jar");
        resourceItem.setResourceAbsolutePathInLocal("/lib/dolphinscheduler-task-spark.jar");
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem);

        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master spark://localhost:7077 " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "/lib/dolphinscheduler-task-spark.jar",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithPyFiles() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.PYTHON);
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/main.py");
        sparkParameters.setMainJar(mainJar);
        ResourceInfo pyFile1 = new ResourceInfo();
        pyFile1.setResourceName("/python libs/r&d.py");
        ResourceInfo pyFile2 = new ResourceInfo();
        pyFile2.setResourceName("/b'c.zip");
        sparkParameters.setPyFiles(new ArrayList<>(Arrays.asList(pyFile1, pyFile2)));
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/main.py");
        addResourceItem(resourceContext, "/python libs/r&d.py");
        addResourceItem(resourceContext, "/b'c.zip");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "--py-files " + quotedFileUris("/python libs/r&d.py", "/b'c.zip") + " " +
                        "/main.py",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithHashInPyFiles() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.PYTHON);
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/main.py");
        sparkParameters.setMainJar(mainJar);
        ResourceInfo pyFile = new ResourceInfo();
        pyFile.setResourceName("/spark files/config#prod.zip");
        sparkParameters.setPyFiles(new ArrayList<>(Arrays.asList(pyFile)));
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/main.py");
        addResourceItem(resourceContext, "/spark files/config#prod.zip");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        String script = sparkTask.getScript();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "--py-files " + quotedFileUris("/spark files/config#prod.zip") + " " +
                        "/main.py",
                script);
        // replicate Spark's URI resolution of the list entry: the '#' must stay inside the
        // encoded path instead of being read as a fragment, so Spark resolves the original file
        String pyFilesValue = script.substring(script.indexOf("--py-files ") + "--py-files ".length()).split(" ")[0];
        URI pyFileUri = URI.create(pyFilesValue.substring(1, pyFilesValue.length() - 1));
        Assertions.assertEquals("file", pyFileUri.getScheme());
        Assertions.assertNull(pyFileUri.getFragment());
        Assertions.assertTrue(pyFileUri.getPath().endsWith("config#prod.zip"));
    }

    @Test
    public void testBuildCommandWithArchives() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(mainJar);
        ResourceInfo archive = new ResourceInfo();
        archive.setResourceName("/opt archives/c&t.gz");
        sparkParameters.setArchives(new ArrayList<>(Arrays.asList(archive)));
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/lib/dolphinscheduler-task-spark.jar");
        addResourceItem(resourceContext, "/opt archives/c&t.gz");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "--archives " + quotedFileUris("/opt archives/c&t.gz") + " " +
                        "/lib/dolphinscheduler-task-spark.jar",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithHashInArchives() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(mainJar);
        ResourceInfo archive = new ResourceInfo();
        archive.setResourceName("/opt archives/c#prod.tar.gz");
        sparkParameters.setArchives(new ArrayList<>(Arrays.asList(archive)));
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/lib/dolphinscheduler-task-spark.jar");
        addResourceItem(resourceContext, "/opt archives/c#prod.tar.gz");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        String script = sparkTask.getScript();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "--archives " + quotedFileUris("/opt archives/c#prod.tar.gz") + " " +
                        "/lib/dolphinscheduler-task-spark.jar",
                script);
        // replicate Spark's URI resolution of the list entry: the '#' must stay inside the
        // encoded path instead of being read as a fragment, so Spark resolves the original file
        String archivesValue =
                script.substring(script.indexOf("--archives ") + "--archives ".length()).split(" ")[0];
        URI archiveUri = URI.create(archivesValue.substring(1, archivesValue.length() - 1));
        Assertions.assertEquals("file", archiveUri.getScheme());
        Assertions.assertNull(archiveUri.getFragment());
        Assertions.assertTrue(archiveUri.getPath().endsWith("c#prod.tar.gz"));
    }

    @Test
    public void testBuildCommandWithKerberos() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(mainJar);
        sparkParameters.setPrincipal("user@REALM");
        ResourceInfo keytab = new ResourceInfo();
        keytab.setResourceName("/kerberos keytabs/u&r.keytab");
        sparkParameters.setKeytab(keytab);
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/lib/dolphinscheduler-task-spark.jar");
        addResourceItem(resourceContext, "/kerberos keytabs/u&r.keytab");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "--principal user@REALM " +
                        "--keytab '/kerberos keytabs/u&r.keytab' " +
                        "/lib/dolphinscheduler-task-spark.jar",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithBlankPrincipal() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(mainJar);
        sparkParameters.setPrincipal("   ");
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        ResourceContext resourceContext = new ResourceContext();
        addResourceItem(resourceContext, "/lib/dolphinscheduler-task-spark.jar");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getResourceContext()).thenReturn(resourceContext);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertFalse(sparkTask.getScript().contains("--principal"));
    }

    @Test
    public void testBuildCommandWithBlankPrincipalWithKeytab() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(mainJar);
        sparkParameters.setPrincipal("   ");
        ResourceInfo keytab = new ResourceInfo();
        keytab.setResourceName("/kerberos keytabs/u&r.keytab");
        sparkParameters.setKeytab(keytab);
        String parameters = JSONUtils.toJsonString(sparkParameters);

        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);

        // a blank principal paired with a keytab is invalid and must be rejected at init()
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        Assertions.assertThrows(RuntimeException.class, sparkTask::init);
    }

    private String quotedFileUris(String... localPaths) {
        // mirror the command generation: encode each local path as a file: URI, then shell-quote
        return Arrays.stream(localPaths)
                .map(path -> ArgsUtils.wrapperSingleQuotes(new File(path).toURI().toString()))
                .collect(Collectors.joining(","));
    }

    private void addResourceItem(ResourceContext resourceContext, String path) {
        ResourceContext.ResourceItem item = new ResourceContext.ResourceItem();
        item.setResourceAbsolutePathInStorage(path);
        item.setResourceAbsolutePathInLocal(path);
        resourceContext.addResourceItem(item);
    }

    private String buildSparkParametersWithSparkSql() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setRawScript("selcet 11111;");
        sparkParameters.setProgramType(ProgramType.SQL);
        sparkParameters.setSqlExecutionType(TYPE_FILE);
        sparkParameters.setMainClass("");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("sparksql");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);

        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setResourceName("testSparkParameters1.jar");
        List<ResourceInfo> resourceInfos = new ArrayList<>(Arrays.asList(
                resourceInfo1));
        sparkParameters.setResourceList(resourceInfos);

        return JSONUtils.toJsonString(sparkParameters);
    }

    private String buildSparkParametersWithSparkSubmit() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(resourceInfo);
        return JSONUtils.toJsonString(sparkParameters);
    }

    private String buildSparkParametersWithMaster() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setMaster("spark://localhost:7077");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(resourceInfo);
        return JSONUtils.toJsonString(sparkParameters);
    }

}
