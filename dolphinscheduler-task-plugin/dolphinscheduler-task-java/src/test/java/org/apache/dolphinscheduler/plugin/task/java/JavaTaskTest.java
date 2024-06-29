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

package org.apache.dolphinscheduler.plugin.task.java;

import static com.google.common.truth.Truth.assertThat;
import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAVA;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.java.exception.JavaSourceFileExistException;
import org.apache.dolphinscheduler.plugin.task.java.exception.PublicClassNotFoundException;
import org.apache.dolphinscheduler.plugin.task.java.exception.RunTypeNotFoundException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaTaskTest {

    private TaskCallBack taskCallBack = new TaskCallBack() {

        @Override
        public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {

        }

        @Override
        public void updateTaskInstanceInfo(int taskInstanceId) {

        }
    };

    @Test
    public void testGetPubllicClassName() {
        JavaTask javaTask = runJavaType();
        Assertions.assertEquals(javaTask.getPublicClassName("import java.io.IOException;\n" +
                "public class JavaTaskTest {\n" +
                "    public static void main(String[] args) throws IOException {\n" +
                "        StringBuilder builder = new StringBuilder(\"Hello: \");\n" +
                "        for (String arg : args) {\n" +
                "            builder.append(arg).append(\" \");\n" +
                "        }\n" +
                "        System.out.println(builder);\n" +
                "    }\n" +
                "}\n"), "JavaTaskTest");
    }

    /**
     * Construct a java -jar command
     *
     * @return void
     **/
    @Test
    public void buildJarCommand() {
        JavaTask javaTask = runJarType();
        assertThat(javaTask.buildJarCommand())
                .isEqualTo(
                        "${JAVA_HOME}/bin/java -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar -jar /tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar -host 127.0.0.1 -port 8080 -xms:50m");
    }

    /**
     * Construct the compile command
     *
     * @return void
     **/
    @Test
    public void buildJavaCompileCommand() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assertions.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            assertThat(javaTask.buildJavaCompileCommand(sourceCode))
                    .isEqualTo(
                            "${JAVA_HOME}/bin/javac -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar /tmp/dolphinscheduler/test/executepath/JavaTaskTest.java");
        } finally {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }

    }

    /**
     * Construct java to run the command
     *
     * @return void
     **/
    @Test
    public void buildJavaCommand() throws Exception {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);

        Assertions.assertEquals("JavaTaskTest", publicClassName);

        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        assertThat(javaTask.buildJavaCommand())
                .isEqualTo(
                        "${JAVA_HOME}/bin/javac -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar /tmp/dolphinscheduler/test/executepath/JavaTaskTest.java;${JAVA_HOME}/bin/java -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar JavaTaskTest -host 127.0.0.1 -port 8080 -xms:50m");
    }

    /**
     * There is no exception to overwriting the Java source file
     *
     * @return void
     * @throws IOException
     **/
    @Test
    public void coverJavaSourceFileExistException() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assertions.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);

        Assertions.assertThrows(JavaSourceFileExistException.class, () -> {
            try {
                Path path = Paths.get(fileName);
                if (!Files.exists(path)) {
                    FileUtils.createDirectoryWith755(path);
                }
                javaTask.createJavaSourceFileIfNotExists(sourceCode, fileName);
            } finally {
                Path path = Paths.get(fileName);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            }
        });
    }

    /**
     * The override class name could not find an exception
     *
     * @return void
     **/
    @Test
    public void coverPublicClassNotFoundException() {
        Assertions.assertThrows(PublicClassNotFoundException.class, () -> {
            JavaTask javaTask = runJavaType();
            javaTask.getPublicClassName("");
        });
    }

    /**
     * The override run mode could not find an exception
     *
     * @return void
     * @throws Exception
     **/
    @Test
    public void coverRunTypeNotFoundException() throws Exception {
        JavaTask javaTask = runJavaType();
        Field javaParameters = JavaTask.class.getDeclaredField("javaParameters");
        javaParameters.setAccessible(true);
        ((JavaParameters) (javaParameters.get(javaTask))).setRunType("");

        Assertions.assertThrows(RunTypeNotFoundException.class, () -> {
            javaTask.handle(taskCallBack);
            javaTask.getPublicClassName("");
        });
    }

    /**
     * Create a Java task parameter mock object
     *
     * @param runType
     * @return JavaParameters
     **/
    public JavaParameters createJavaParametersObject(String runType) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(runType);
        javaParameters.setModulePath(false);
        javaParameters.setJvmArgs("-xms:50m");
        javaParameters.setMainArgs("-host 127.0.0.1 -port 8080");
        ResourceInfo resourceJar = new ResourceInfo();
        resourceJar.setResourceName("/opt/share/jar/resource2.jar");
        ArrayList<ResourceInfo> resourceInfoArrayList = new ArrayList<>();
        resourceInfoArrayList.add(resourceJar);
        javaParameters.setResourceList(resourceInfoArrayList);
        javaParameters.setRawScript(
                "import java.io.IOException;\n" +
                        "public class JavaTaskTest {\n" +
                        "    public static void main(String[] args) throws IOException {\n" +
                        "        StringBuilder builder = new StringBuilder(\"Hello: \");\n" +
                        "        for (String arg : args) {\n" +
                        "            builder.append(arg).append(\" \");\n" +
                        "        }\n" + "        System.out.println(builder);\n" +
                        "    }\n" +
                        "}\n");
        ArrayList<Property> localParams = new ArrayList<>();
        Property property = new Property();
        property.setProp("name");
        property.setValue("zhangsan");
        property.setDirect(IN);
        property.setType(VARCHAR);
        javaParameters.setLocalParams(localParams);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/opt/share/jar/main.jar");
        javaParameters.setMainJar(mainJar);
        return javaParameters;
    }

    /**
     * A Java task that constructs the Java runtime pattern
     *
     * @return JavaTask
     **/
    public JavaTask runJavaType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_JAVA)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        ResourceContext.ResourceItem resourceItem1 = new ResourceContext.ResourceItem();
        resourceItem1.setResourceAbsolutePathInStorage("/opt/share/jar/resource2.jar");
        resourceItem1
                .setResourceAbsolutePathInLocal("/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar");

        ResourceContext.ResourceItem resourceItem2 = new ResourceContext.ResourceItem();
        resourceItem2.setResourceAbsolutePathInStorage("/opt/share/jar/main.jar");
        resourceItem2.setResourceAbsolutePathInLocal("/tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar");

        ResourceContext.ResourceItem resourceItem3 = new ResourceContext.ResourceItem();
        resourceItem3.setResourceAbsolutePathInStorage("/JavaTaskTest.java");
        resourceItem3.setResourceAbsolutePathInLocal("/tmp/dolphinscheduler/test/executepath/JavaTaskTest.java");

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem1);
        resourceContext.addResourceItem(resourceItem2);
        resourceContext.addResourceItem(resourceItem3);
        taskExecutionContext.setResourceContext(resourceContext);
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

    /**
     * The Java task to construct the jar run mode
     *
     * @return JavaTask
     **/
    private JavaTask runJarType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_JAR)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        ResourceContext.ResourceItem resourceItem1 = new ResourceContext.ResourceItem();
        resourceItem1.setResourceAbsolutePathInStorage("/opt/share/jar/resource2.jar");
        resourceItem1
                .setResourceAbsolutePathInLocal("/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar");

        ResourceContext.ResourceItem resourceItem2 = new ResourceContext.ResourceItem();
        resourceItem2.setResourceAbsolutePathInStorage("/opt/share/jar/main.jar");
        resourceItem2.setResourceAbsolutePathInLocal("/tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar");

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem1);
        resourceContext.addResourceItem(resourceItem2);
        taskExecutionContext.setResourceContext(resourceContext);

        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }
}
