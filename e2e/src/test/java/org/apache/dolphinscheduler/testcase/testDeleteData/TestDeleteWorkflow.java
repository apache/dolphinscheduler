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
package org.apache.dolphinscheduler.testcase.testDeleteData;

import org.apache.dolphinscheduler.base.BaseTest;
import org.apache.dolphinscheduler.page.project.ProjectPage;
import org.apache.dolphinscheduler.page.project.WorkflowDefinePage;
import org.testng.annotations.Test;

public class TestDeleteWorkflow extends BaseTest {
    private WorkflowDefinePage createWorkflowPage;
    private ProjectPage createProjectPage;

    /**
     * offline workflow
     * @throws InterruptedException
     */
    @Test(groups={"functionTests"},dependsOnGroups = { "login","workflow"},description = "TestDeleteWorkflow")
    public void testOfflineWorkflow() throws InterruptedException {
        createWorkflowPage = new WorkflowDefinePage(driver);
        System.out.println("start offline workflow");
        assert createWorkflowPage.offlineWorkflow();
        System.out.println("end offline workflow");
        System.out.println("===================================");
    }

    @Test(groups={"functionTests"},dependsOnGroups = { "login","workflow"},description = "TestDeleteWorkflow")
    public void testDeleteWorkflow() throws InterruptedException {
        System.out.println("start delete workflow");
        assert createWorkflowPage.deleteWorkflow();
        System.out.println("end delete workflow");
        System.out.println("===================================");
    }
}
