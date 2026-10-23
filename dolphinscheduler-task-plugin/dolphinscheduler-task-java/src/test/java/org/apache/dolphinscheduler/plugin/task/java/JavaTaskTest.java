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
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_FAT_JAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_JSHELL;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_NORMAL_JAR;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Slf4j
class JavaTaskTest {

    /**
     * Construct a java -jar command
     *
     * @return void
     **/
    @Test
    void buildJarCommand() {
        JavaTask javaTask = runJarType();
        assertThat(javaTask.buildJarCommand())
                .isEqualTo(
                        "${JAVA_HOME}/bin/java -xms:50m -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar -jar /tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar -host 127.0.0.1 -port 8080");
    }

    /**
     * Construct a java -cp command
     *
     * @return void
     */
    @Test
    void buildNormalJarCommand() {
        JavaTask javaTask = runNormalJarType();
        assertThat(javaTask.buildNormalJarCommand())
                .isEqualTo(
                        "${JAVA_HOME}/bin/java -xms:50m -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar:/tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar Test -host 127.0.0.1 -port 8080");
    }

    @Test
    void testParameterPass() {
        JavaTask javaTask = createJavaTaskWithCusVar();
        assertThat(javaTask.buildNormalJarCommand())
                .isEqualTo(
                        "${JAVA_HOME}/bin/java -xms:1G -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar Test -host 0.0.0.0 -port 12345");
    }

    @Test
    void jshellParametersDoNotRequireMainJar() {
        JavaParameters parameters =
                createJshellJavaParameters("System.out.println(\"Hello JShell\");");

        assertThat(parameters.getMainJar()).isNull();
        assertThat(parameters.checkParameters()).isTrue();
    }

    @Test
    void jshellParametersRejectEmptyScript() {
        JavaParameters parameters = createJshellJavaParameters("");

        assertThat(parameters.checkParameters()).isFalse();

        parameters.setJshellScript("   \n  ");

        assertThat(parameters.checkParameters()).isFalse();
    }

    @Test
    void jarRunTypesStillRequireMainJar() {
        JavaParameters fatJarParameters =
                createJavaParametersObject(RUN_TYPE_FAT_JAR);
        fatJarParameters.setMainJar(null);

        assertThat(fatJarParameters.checkParameters()).isFalse();

        JavaParameters normalJarParameters =
                createNormalJarJavaParameters(RUN_TYPE_NORMAL_JAR);
        normalJarParameters.setMainJar(null);

        assertThat(normalJarParameters.checkParameters()).isFalse();
    }

    @Test
    void buildJshellCommand(@TempDir Path executePath) throws IOException {
        String source = "System.out.println(\"Hello JShell\");\n"
                + "int result = 10 + 20;\n"
                + "System.out.println(\"Result = \" + result);\n";

        JavaTask javaTask = createJshellTask(
                executePath,
                createJshellJavaParameters(source));

        Path scriptPath = null;

        try {
            String command = javaTask.buildJshellCommand();

            assertThat(command).contains("\"${JAVA_HOME}/bin/jshell\"");
            assertThat(command).contains("-R-Xms64m");
            assertThat(command).contains("--feedback concise");
            assertThat(command).contains("--class-path");
            assertThat(command).contains(executePath.toString());
            assertThat(command).contains(" - < '");

            try (Stream<Path> files = Files.list(executePath)) {
                scriptPath = files
                        .filter(path -> path.getFileName().toString()
                                .startsWith("jshell-"))
                        .filter(path -> path.getFileName().toString()
                                .endsWith(".jsh"))
                        .findFirst()
                        .orElse(null);
            }

            assertThat(scriptPath).isNotNull();
            assertThat(command).contains(scriptPath.toAbsolutePath().toString());

            String actualSource = new String(
                    Files.readAllBytes(scriptPath),
                    StandardCharsets.UTF_8);

            assertThat(actualSource).isEqualTo(source);
        } finally {
            if (scriptPath != null) {
                Files.deleteIfExists(scriptPath);
            }
        }
    }

    @Test
    void buildJshellResourcePathUsesClassPathByDefault(@TempDir Path executePath) {
        JavaParameters parameters =
                createJshellJavaParameters("System.out.println(1);");

        JavaTask javaTask = createJshellTask(executePath, parameters);

        String resourcePath = javaTask.buildJshellResourcePath();

        assertThat(resourcePath).startsWith("--class-path ");
        assertThat(resourcePath).contains(executePath.toString());
    }

    @Test
    void buildJshellResourcePathUsesModulePathWhenEnabled(@TempDir Path executePath) {
        JavaParameters parameters =
                createJshellJavaParameters("System.out.println(1);");
        parameters.setModulePath(true);

        JavaTask javaTask = createJshellTask(executePath, parameters);

        String resourcePath = javaTask.buildJshellResourcePath();

        assertThat(resourcePath).startsWith("--module-path ");
        assertThat(resourcePath).contains(executePath.toString());
    }

    private JavaParameters createJshellJavaParameters(String source) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(RUN_TYPE_JSHELL);
        javaParameters.setJshellScript(source);
        javaParameters.setJvmArgs("-Xms64m");
        javaParameters.setMainArgs("");
        javaParameters.setModulePath(false);
        javaParameters.setResourceList(new ArrayList<>());
        return javaParameters;
    }

    private JavaTask createJshellTask(
                                      Path executePath,
                                      JavaParameters javaParameters) {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        taskExecutionContext.setTaskParams(
                JSONUtils.toJsonString(javaParameters));
        taskExecutionContext.setExecutePath(executePath.toString());
        taskExecutionContext.setTaskAppId("runJshellType");
        taskExecutionContext.setResourceContext(new ResourceContext());

        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();

        return javaTask;
    }

    /**
     * add the Normal Jar parameters
     *
     * @param runType
     * @return JavaParameters
     */
    private JavaParameters createNormalJarJavaParameters(String runType) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(runType);
        javaParameters.setModulePath(false);
        javaParameters.setJvmArgs("-xms:50m");
        javaParameters.setMainArgs("-host 127.0.0.1 -port 8080");
        ResourceInfo resourceJar1 = new ResourceInfo();
        resourceJar1.setResourceName("/opt/share/jar/resource2.jar");
        ResourceInfo resourceJar2 = new ResourceInfo();
        resourceJar2.setResourceName("/opt/share/jar/main.jar");
        ArrayList<ResourceInfo> resourceInfoArrayList = new ArrayList<>();
        resourceInfoArrayList.add(resourceJar1);
        resourceInfoArrayList.add(resourceJar2);
        javaParameters.setResourceList(resourceInfoArrayList);
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
     * Add the fat jar parameters
     *
     * @param runType
     * @return JavaParameters
     */
    private JavaParameters createJavaParametersObject(String runType) {
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

    private JavaParameters createJavaTaskWithCusVar(String runType) {
        JavaParameters javaParameters = new JavaParameters();
        javaParameters.setRunType(runType);
        javaParameters.setModulePath(false);
        javaParameters.setJvmArgs("-xms:${max_mem}");
        javaParameters.setMainArgs("-host ${host} -port ${port}");
        ResourceInfo resourceJar = new ResourceInfo();
        resourceJar.setResourceName("/opt/share/jar/resource2.jar");
        ArrayList<ResourceInfo> resourceInfoArrayList = new ArrayList<>();
        resourceInfoArrayList.add(resourceJar);
        javaParameters.setResourceList(resourceInfoArrayList);
        ArrayList<Property> localParams = new ArrayList<>();
        javaParameters.setLocalParams(localParams);
        ResourceInfo mainJar = new ResourceInfo();
        mainJar.setResourceName("/opt/share/jar/main.jar");
        javaParameters.setMainJar(mainJar);
        return javaParameters;
    }

    private ResourceContext createResourceContext() {
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
        return resourceContext;
    }

    /**
     * The Java task to construct the jar run mode
     *
     * @return JavaTask
     **/
    private JavaTask runJarType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_FAT_JAR)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        ResourceContext resourceContext = createResourceContext();
        taskExecutionContext.setResourceContext(resourceContext);
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

    /**
     * The Java task to construct the normal jar run mode
     *
     * @return JavaTask
     */
    private JavaTask runNormalJarType() {
        packageTestJar();
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createNormalJarJavaParameters(RUN_TYPE_NORMAL_JAR)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        ResourceContext resourceContext = createResourceContext();
        taskExecutionContext.setResourceContext(resourceContext);
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

    private JavaTask createJavaTaskWithCusVar() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaTaskWithCusVar(RUN_TYPE_NORMAL_JAR)));
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/test/executepath");
        taskExecutionContext.setTaskAppId("runJavaType");
        Property property = new Property();
        property.setProp("max_mem");
        property.setValue("1G");
        property.setDirect(IN);
        property.setType(VARCHAR);

        Property property2 = new Property();
        property2.setProp("host");
        property2.setValue("0.0.0.0");
        property2.setDirect(IN);
        property2.setType(VARCHAR);

        Property property3 = new Property();
        property3.setProp("port");
        property3.setValue("12345");
        property3.setDirect(IN);
        property3.setType(VARCHAR);

        HashMap<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("max_mem", property);
        prepareParamsMap.put("host", property2);
        prepareParamsMap.put("port", property3);

        taskExecutionContext.setPrepareParamsMap(prepareParamsMap);
        ResourceContext resourceContext = createResourceContext();
        taskExecutionContext.setResourceContext(resourceContext);
        JavaTask javaTask = new JavaTask(taskExecutionContext);
        javaTask.init();
        return javaTask;
    }

    /**
     * Package the class to Jar
     */
    private void packageTestJar() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, "Test");
        String jarDirPath = "/tmp/dolphinscheduler/test/executepath/opt/share/jar";
        File jarDir = new File(jarDirPath);
        if (!jarDir.exists() && jarDir.mkdirs()) {
            log.info("Created directory: {}", jarDirPath);
        } else if (!jarDir.exists()) {
            throw new RuntimeException("Failed to create directory: " + jarDirPath);
        }
        File jarFile = new File(jarDirPath, "main.jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile.toPath()), manifest)) {
            jos.putNextEntry(new JarEntry("META-INF/"));
            jos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while creating the JAR file.", e);
        }
        log.info("main.jar created: {}", jarFile.getAbsolutePath());
    }

}
