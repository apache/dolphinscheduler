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
package org.apache.dolphinscheduler.testcase.testProject;

import org.apache.dolphinscheduler.base.BaseTest;
import org.apache.dolphinscheduler.page.project.ProjectPage;
import org.apache.dolphinscheduler.page.project.WorkflowDefinePage;
import org.apache.dolphinscheduler.page.project.RunWorkflowPage;
import org.testng.annotations.Test;

public class TestRunWorkflow extends BaseTest {
    private WorkflowDefinePage createWorkflowPage;
    private ProjectPage projectPage;
    private RunWorkflowPage runWorkflowPage;


    @Test(groups={"functionTests","runWorkflow"},dependsOnGroups = { "login","workflow" },description = "TestRunWorkflow")
    public void testRunWorkflow() throws InterruptedException {
        runWorkflowPage = new RunWorkflowPage(driver);

        projectPage = new ProjectPage(driver);
        System.out.println("start run workflow");
        assert runWorkflowPage.runWorkflow();
        System.out.println("end run workflow");
        System.out.println("===================================");
    }
}
