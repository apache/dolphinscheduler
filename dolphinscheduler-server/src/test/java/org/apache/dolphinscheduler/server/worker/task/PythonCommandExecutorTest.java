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

package org.apache.dolphinscheduler.server.worker.task;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonCommandExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(PythonCommandExecutorTest.class);

    @Test
    public void testGetPythonHome() {
        String path = System.getProperty("user.dir") + "/script/env/dolphinscheduler_env.sh";
        if (path.contains("dolphinscheduler-server/")) {
            path = path.replace("dolphinscheduler-server/", "");
        }
        String pythonHome = PythonCommandExecutor.getPythonHome(path);
        logger.info(pythonHome);
        Assert.assertNotNull(pythonHome);
    }

    @Test
    public void testGetPythonHomeFromEnvironmentConfig() {
        String environmentConfig = "export HADOOP_HOME=/opt/hadoop-2.6.5\n"
                + "export HADOOP_CONF_DIR=/etc/hadoop/conf\n"
                + "export SPARK_HOME=/opt/soft/spark\n"
                + "export PYTHON_HOME=/opt/soft/python\n"
                + "export JAVA_HOME=/opt/java/jdk1.8.0_181-amd64\n"
                + "export HIVE_HOME=/opt/soft/hive\n"
                + "export FLINK_HOME=/opt/soft/flink\n"
                + "export DATAX_HOME=/opt/soft/datax\n"
                + "export YARN_CONF_DIR=/etc/hadoop/conf\n"
                + "export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH\n"
                + "export HADOOP_CLASSPATH=`hadoop classpath`";

        String expected = "/opt/soft/python";

        String pythonHome = PythonCommandExecutor.getPythonHomeFromEnvironmentConfig(environmentConfig);
        logger.info(pythonHome);
        Assert.assertEquals(expected,pythonHome);
    }

    @Test
    public void testGetPythonCommand() {
        String pythonCommand = PythonCommandExecutor.getPythonCommand(null);
        Assert.assertEquals(PythonCommandExecutor.PYTHON, pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("");
        Assert.assertEquals(PythonCommandExecutor.PYTHON, pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("/usr/bin/python");
        Assert.assertEquals("/usr/bin/python", pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("/usr/local/bin/python2");
        Assert.assertEquals("/usr/local/bin/python2", pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("/opt/python/bin/python3.8");
        Assert.assertEquals("/opt/python/bin/python3.8", pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("/opt/soft/python");
        Assert.assertEquals("/opt/soft/python/bin/python", pythonCommand);
        pythonCommand = PythonCommandExecutor.getPythonCommand("/opt/soft/python-3.8");
        Assert.assertEquals("/opt/soft/python-3.8/bin/python", pythonCommand);
    }

}
