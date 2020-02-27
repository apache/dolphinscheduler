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
package org.apache.dolphinscheduler.page.project;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.constant.TestConstant;
import org.apache.dolphinscheduler.data.project.CreatProjectData;
import org.apache.dolphinscheduler.locator.project.CreateProjectLocator;
import org.openqa.selenium.WebDriver;

public class CreateProjectPage extends PageCommon {
    public CreateProjectPage(WebDriver driver) {
        super(driver);
    }
    /**
     * jump page
     */
    public void jumpProjectManagePage() throws InterruptedException {
        Thread.sleep(TestConstant.ONE_THOUSANG);
        clickElement(CreateProjectLocator.PROJECT_MANAGE);
        Thread.sleep(TestConstant.ONE_THOUSANG);
    }

    /**
     * creatTenant
     *
     * @return Whether to enter the specified page after creat tenant
     */
    public boolean createProject() throws InterruptedException {
        //click  create project
        clickElement(CreateProjectLocator.CREATE_PROJECT_BUTTON);
        Thread.sleep(TestConstant.ONE_THOUSANG);

        // input create project data
        sendInput(CreateProjectLocator.PROJECT_NAME, CreatProjectData.PROJECT_NAME);
        sendInput(CreateProjectLocator.PROJECT_DESCRIPTION, CreatProjectData.DESCRIPTION);

        // click submit  button
        clickButton(CreateProjectLocator.SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(CreatProjectData.PROJECT_TITLE);
    }
}
