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
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.RUN_TYPE_NORMAL_JAR;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class JavaTaskTest {

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
     * Construct a java -cp command
     *
     * @return void
     */
    @Test
    public void buildNormalJarCommand() {
        JavaTask javaTask = runNormalJarType();
        try{
        assertThat(javaTask.buildNormalJarCommand())
                .isEqualTo("${JAVA_HOME}/bin/java -classpath .:/tmp/dolphinscheduler/test/executepath:/tmp/dolphinscheduler/test/executepath/opt/share/jar/resource2.jar:/tmp/dolphinscheduler/test/executepath/opt/share/jar/main.jar HelloWorldWithGuava -host 127.0.0.1 -port 8080 -xms:50m");
                        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * add the Normal Jar parameters
     *
     * @param runType
     * @return
     */
    public JavaParameters createNormalJarJavaParameters(String runType) {
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
     * @return
     */
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
     * The Java task to construct the jar run mode
     *
     * @return JavaTask
     **/
    private JavaTask runJarType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createJavaParametersObject(RUN_TYPE_FAT_JAR)));
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

    /**
     * The Java task to construct the normal jar run mode
     *
     * @return JavaTask
     */
    private JavaTask runNormalJarType() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(createNormalJarJavaParameters(RUN_TYPE_NORMAL_JAR)));
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
