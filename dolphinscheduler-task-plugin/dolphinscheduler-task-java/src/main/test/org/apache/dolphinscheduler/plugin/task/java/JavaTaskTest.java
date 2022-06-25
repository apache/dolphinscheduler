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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JAVA;

public class JavaTaskTest {

    public JavaParameters createJavaParametersObject(String runType) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(runType);
        javaParameters.setJavaVersion(JavaVersion.JAVA_8);
        javaParameters.setJvmArgs("-xms:50m");
        javaParameters.setMainArgs("-host 127.0.0.1 -port 8080");
        ResourceInfo resourceJar = new ResourceInfo();
        resourceJar.setId(2);
        resourceJar.setResourceName("/opt/share/jar/resource2.jar");
        resourceJar.setRes("I'm resource2.jar");
        ArrayList<ResourceInfo> resourceInfoArrayList = new ArrayList<>();
        resourceInfoArrayList.add(resourceJar);
        javaParameters.setResourceList(resourceInfoArrayList);
        javaParameters.setRawScript("public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"hello world : \");\n" +
                "        for (String arg : args) {\n" +
                "            System.out.print(arg+ \" \");\n" +
                "        }\n" +
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

    @Test
    public void buildJarCommand() {
        JavaTask javaTask = runJarType();
        Assert.assertEquals(javaTask.buildJarCommand(), "java -jar /opt/share/jar/main.jar -host 127.0.0.1 -port 8080 -xms:50m -class-path .:/opt/share/jar/resource2.jar");
    }

    @Test
    public void buildJavaCompileCommand() throws IOException {
        JavaTask javaTask = runJavaType();
        String sourceCode = javaTask.buildJavaSourceContent();
        String fileName =  javaTask.buildJavaSourceCodeFileFullName();
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println(1);
        }
        javaTask.createJavaSourceFileIfNotExists(sourceCode, fileName);
        String className = fileName.substring(0 ,fileName.lastIndexOf('.'));
        className = className.substring(className.lastIndexOf('.') + 1);
        Assert.assertEquals(className,"/tmp/dolphinscheduler/test/executepath/java_runJavaType");
        Assert.assertEquals(javaTask.buildJavaCompileCommand(className,sourceCode),"javac /tmp/dolphinscheduler/test/executepath/java_runJavaType.java -class-path .:/opt/share/jar/resource2.jar");
    }

    @Test
    public void buildJavaCommand() throws Exception {
        JavaTask javaTask = runJavaType();
        Assert.assertEquals(javaTask.buildJavaCommand(),"java /tmp/dolphinscheduler/test/executepath/java_runJavaType -host 127.0.0.1 -port 8080 -xms:50m -class-path .:/opt/share/jar/resource2.jar");
    }


}
