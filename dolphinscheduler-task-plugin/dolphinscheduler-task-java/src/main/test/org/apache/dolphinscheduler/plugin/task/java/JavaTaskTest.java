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

import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAVA;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.java.exception.JavaSourceFileExistException;
import org.apache.dolphinscheduler.plugin.task.java.exception.PublicClassNotFoundException;
import org.apache.dolphinscheduler.plugin.task.java.exception.RunTypeNotFoundException;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class JavaTaskTest {

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     **/
    @Test
    public void buildJarCommand() {
        String homeBinPath = JavaConstants.JAVA_HOME_VAR + JavaConstants.FILE_SEPARATOR + "bin" + JavaConstants.FILE_SEPARATOR;
        JavaTask javaTask = runJarType();
        Assert.assertEquals(javaTask.buildJarCommand(), homeBinPath
                + "java --class-path .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar -jar /tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar -host 127.0.0.1 -port 8080 -xms:50m");
    }

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     **/
    @Test
    public void buildJavaCompileCommand() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assert.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        try {
            String homeBinPath = JavaConstants.JAVA_HOME_VAR + JavaConstants.FILE_SEPARATOR + "bin" + JavaConstants.FILE_SEPARATOR;
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            javaTask.createJavaSourceFileIfNotExists(sourceCode, fileName);
            Assert.assertEquals(homeBinPath
                            + "javac --class-path .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar /tmp/dolphinscheduler/test/executepath/JavaTaskTest.java",
                    javaTask.buildJavaCompileCommand(fileName, sourceCode));
        } finally {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }

    }

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     **/
    @Test
    public void buildJavaCommand() throws Exception {
        String homeBinPath = JavaConstants.JAVA_HOME_VAR + JavaConstants.FILE_SEPARATOR + "bin" + JavaConstants.FILE_SEPARATOR;
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assert.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Assert.assertEquals(javaTask.buildJavaCommand(), homeBinPath + "java --class-path .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar JavaTaskTest -host 127.0.0.1 -port 8080 -xms:50m");
    }

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     * @throws IOException
     **/
    @Test(expected = JavaSourceFileExistException.class)
    public void  coverJavaSourceFileExistException() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assert.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        try {
            Path path = Paths.get(fileName);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            javaTask.createJavaSourceFileIfNotExists(sourceCode,fileName);
        } finally {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     **/
    @Test(expected = PublicClassNotFoundException.class)
    public void  coverPublicClassNotFoundException() {
        JavaTask javaTask = runJavaType();
        javaTask.getPublicClassName("");
    }

    /**
     * @description:
     * @date: 7/22/22 2:38 AM
     * @param: []
     * @return: void
     * @throws Exception
     **/
    @Test(expected = RunTypeNotFoundException.class)
    public void  coverRunTypeNotFoundException() throws Exception {
        JavaTask javaTask = runJavaType();
        Field javaParameters = JavaTask.class.getDeclaredField("javaParameters");
        javaParameters.setAccessible(true);
        ((JavaParameters)(javaParameters.get(javaTask))).setRunType("");
        javaTask.handle();
        javaTask.getPublicClassName("");
    }

    /**
     * @description:
     * @date: 7/22/22 2:39 AM
     * @param: [java.lang.String]
     * @return: org.apache.dolphinscheduler.plugin.task.java.JavaParameters
     **/
    public JavaParameters createJavaParametersObject(String runType) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(runType);
        javaParameters.setModulePath(false);
        javaParameters.setJvmArgs("-xms:50m");
        javaParameters.setMainArgs("-host 127.0.0.1 -port 8080");
        ResourceInfo resourceJar = new ResourceInfo();
        resourceJar.setId(2);
        resourceJar.setResourceName("/opt/share/jar/resource2.jar");
        resourceJar.setRes("I'm resource2.jar");
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
        mainJar.setId(1);
        mainJar.setResourceName("/opt/share/jar/main.jar");
        mainJar.setRes("I'm main.jar");
        javaParameters.setMainJar(mainJar);
        return javaParameters;
    }

    /**
     * @description:
     * @date: 7/22/22 2:39 AM
     * @param: []
     * @return: org.apache.dolphinscheduler.plugin.task.java.JavaTask
     **/
    public JavaTask runJavaType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_JAVA)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

    /**
     * @description:
     * @date: 7/22/22 2:39 AM
     * @param: []
     * @return: org.apache.dolphinscheduler.plugin.task.java.JavaTask
     **/
    public JavaTask runJarType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_JAR)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }
}
