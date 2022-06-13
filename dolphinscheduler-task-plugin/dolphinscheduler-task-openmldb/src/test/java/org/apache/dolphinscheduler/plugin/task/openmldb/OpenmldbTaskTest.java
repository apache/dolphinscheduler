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

package org.apache.dolphinscheduler.plugin.task.openmldb;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class OpenmldbTaskTest {
    static class MockOpenmldbTask extends OpenmldbTask {
        /**
         * constructor
         *
         * @param taskRequest taskRequest
         */
        public MockOpenmldbTask(TaskExecutionContext taskRequest) {
            super(taskRequest);
        }

        @Override
        protected Map<String, Property> mergeParamsWithContext(AbstractParameters parameters) {
            return new HashMap<>();
        }
    }

    private OpenmldbTask createOpenmldbTask() {
        return new MockOpenmldbTask(null);
    }

    @Test
    public void buildPythonExecuteCommand() throws Exception {
        OpenmldbTask openmldbTask = createOpenmldbTask();
        String pythonFile = "test.py";
        String result1 = openmldbTask.buildPythonExecuteCommand(pythonFile);
        Assert.assertEquals("python3 test.py", result1);
    }

    @Test
    public void buildSQLWithComment() throws Exception {
        OpenmldbTask openmldbTask = createOpenmldbTask();
        OpenmldbParameters openmldbParameters = new OpenmldbParameters();
        openmldbParameters.setExecuteMode("offline");
        String rawSQLScript = "select * from users\r\n"
                + "-- some comment\n"
                + "inner join order on users.order_id = order.id; \n\n;"
                + "select * from users;";
        openmldbParameters.setSql(rawSQLScript);
        Whitebox.setInternalState(openmldbTask, "openmldbParameters", openmldbParameters);
        OpenmldbParameters internal = (OpenmldbParameters) openmldbTask.getParameters();
        Assert.assertNotNull(internal);
        Assert.assertEquals(internal.getExecuteMode(), "offline");

        String result1 = openmldbTask.buildPythonScriptContent();
        Assert.assertEquals("import openmldb\n"
                        + "import sqlalchemy as db\n"
                        + "engine = db.create_engine('openmldb:///?zk=null&zkPath=null')\n"
                        + "con = engine.connect()\n"
                        + "con.execute(\"set @@execute_mode='offline';\")\n"
                        + "con.execute(\"set @@sync_job=true\")\n"
                        + "con.execute(\"set @@job_timeout=1800000\")\n"
                        + "con.execute(\"select * from users\\n-- some comment\\ninner join order on users.order_id = "
                        + "order.id\")\n"
                        + "con.execute(\"select * from users\")\n"
                , result1);
    }

}