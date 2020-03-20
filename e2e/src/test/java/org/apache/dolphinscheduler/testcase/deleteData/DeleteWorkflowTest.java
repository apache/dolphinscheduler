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
package org.apache.dolphinscheduler.testcase.deleteData;

import org.apache.dolphinscheduler.base.BaseTest;
import org.apache.dolphinscheduler.page.project.CreateProjectPage;
import org.apache.dolphinscheduler.page.project.CreateWorkflowPage;
import org.testng.annotations.Test;

public class DeleteWorkflowTest extends BaseTest {
    private CreateWorkflowPage createWorkflowPage;
    private CreateProjectPage createProjectPage;

    @Test(groups={"functionTests"},dependsOnGroups = { "login","workflow"},description = "DeleteWorkflowTest",priority=6)
    public void testDeleteWorkflow() throws InterruptedException {
        createProjectPage = new CreateProjectPage(driver);
        //jump to project manage page
        System.out.println("jump to the project manage page to delete workflow");
        createProjectPage.jumpProjectManagePage();

        createWorkflowPage = new CreateWorkflowPage(driver);
        createWorkflowPage.jumpWorkflowPage();
        //assert tenant manage page
        System.out.println("start delete workflow");
        assert createWorkflowPage.deleteWorkflow();
        System.out.println("end delete workflow");
        System.out.println("===================================");
    }
}
