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
import org.apache.dolphinscheduler.data.project.CreateProjectData;
import org.apache.dolphinscheduler.locator.project.CreateProjectLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CreateProjectPage extends PageCommon {
    public CreateProjectPage(WebDriver driver) {
        super(driver);
    }

    /**
     * jump to ProjectManagePage
     */
    public void jumpProjectManagePage() throws InterruptedException {
        System.out.println("111111111");
        Thread.sleep(100000);
        clickElement(CreateProjectLocator.PROJECT_MANAGE);
        Thread.sleep(TestConstant.ONE_THOUSAND);
        System.out.println("2222222222222");
    }

    /**
     * create project
     *
     * @return Whether to enter the specified page after create project
     */
    public boolean createProject() throws InterruptedException {
        //click  create project
        clickElement(CreateProjectLocator.CREATE_PROJECT_BUTTON);

        // input create project data
        sendInput(CreateProjectLocator.PROJECT_NAME, CreateProjectData.PROJECT_NAME);
        sendInput(CreateProjectLocator.PROJECT_DESCRIPTION, CreateProjectData.DESCRIPTION);

        // click submit  button
        clickButton(CreateProjectLocator.SUBMIT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(CreateProjectData.PROJECT_TITLE);
    }

    /**
     * delete project
     *
     * @return Whether to enter the specified page after delete project
     */
    public boolean deleteProject() throws InterruptedException {
        //click  delete project
        clickElement(CreateProjectLocator.DELETE_PROJECT_BUTTON);

        //click confirm delete project
        clickElement(CreateProjectLocator.CONFIRM_DELETE_PROJECT_BUTTON);

        // Whether to enter the specified page after submit
        return ifTitleContains(CreateProjectData.PROJECT_TITLE);
    }
}
