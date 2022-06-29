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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAVA;
import java.io.IOException;
public class JavaTaskTest {
    @Test
    public void testJavaHome() {
        Assert.assertNotNull(System.getenv().get("JAVA_HOME"));
    }


    @Test
    public void buildJarCommand() {
        String homePath = System.getenv(JavaConstants.JAVA_HOME);
        Assert.assertNotNull(homePath);
        String homeBinPath =  homePath+ System.getProperty("file.separator") + "bin" + System.getProperty("file.separator");
        JavaTask javaTask = runJarType();
        Assert.assertEquals(javaTask.buildJarCommand(), homeBinPath
                +"java --class-path .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar -jar /tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar -host 127.0.0.1 -port 8080 -xms:50m");
    }

    @Test
    public void buildJavaCompileCommand() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String publicClassName = javaTask.getPublicClassName(sourceCode);
        Assert.assertEquals("JavaTaskTest", publicClassName);
        String fileName = javaTask.buildJavaSourceCodeFileFullName(publicClassName);
        try {
            String homePath = System.getenv(JavaConstants.JAVA_HOME);
            Assert.assertNotNull(homePath);
            String homeBinPath = homePath + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator");
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            javaTask.createJavaSourceFileIfNotExists(sourceCode, fileName);
            Assert.assertEquals(homeBinPath
                    +"javac --class-path .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar /tmp/dolphinscheduler/test/executepath/JavaTaskTest.java", javaTask.buildJavaCompileCommand(fileName, sourceCode));

        } finally {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }

    }


    @Test
    public void buildJavaCommand() throws Exception {
        String homePath = System.getenv(JavaConstants.JAVA_HOME);
        Assert.assertNotNull(homePath);
        String homeBinPath =  homePath+ System.getProperty("file.separator") + "bin" + System.getProperty("file.separator");
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
                        "        }\n" +
                        "        System.out.println(builder);\n" +
                        "    }\n" +
                        "}\n");
        ArrayList<Property> localParams = new ArrayList<>();
        Property property = new Property();
        property.setProp("name");
        property.setValue("zhangsan");
        property.setDirect(IN);
        property.setType(VARCHAR);
        javaParameters.setLocalParams(localParams);
//        javaParameters.setVarPool("");
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setId(1);
        mainJar.setResourceName("/opt/share/jar/main.jar");
        mainJar.setRes("I'm main.jar");
        javaParameters.setMainJar(mainJar);
        return javaParameters;
    }

    public JavaTask runJavaType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_JAVA)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

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
