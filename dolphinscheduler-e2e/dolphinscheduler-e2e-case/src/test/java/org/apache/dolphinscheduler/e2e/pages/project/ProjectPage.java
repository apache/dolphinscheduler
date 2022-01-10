/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.dolphinscheduler.e2e.pages.project;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage.NavBarItem;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;

@Getter
public final class ProjectPage extends NavBarPage implements NavBarItem {
    @FindBy(id = "btnCreateProject")
    private WebElement buttonCreateProject;

    @FindBy(className = "items-project")
    private List<WebElement> projectList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final CreateProjectForm createProjectForm;

    public ProjectPage(RemoteWebDriver driver) {
        super(driver);

        this.createProjectForm = new CreateProjectForm();

        PageFactory.initElements(driver, this);
    }

    public ProjectPage create(String project) {
        buttonCreateProject().click();
        createProjectForm().inputProjectName().sendKeys(project);
        createProjectForm().buttonSubmit().click();

        new WebDriverWait(driver(), 10)
            .until(ExpectedConditions.textToBePresentInElementLocated(By.className("project-name"), project));

        return this;
    }

    public ProjectPage delete(String project) {
        projectList()
            .stream()
            .filter(it -> it.getText().contains(project))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find project: " + project))
            .findElement(By.className("delete")).click();

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button is displayed"))
            .click();

        return this;
    }

    public ProjectDetailPage goTo(String project) {
        projectList().stream()
                     .filter(it -> it.getText().contains(project))
                     .map(it -> it.findElement(By.className("project-name")))
                     .findFirst()
                     .orElseThrow(() -> new RuntimeException("Cannot click the project item"))
                     .click();

        return new ProjectDetailPage(driver);
    }

    @Getter
    public class CreateProjectForm {
        CreateProjectForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(id = "inputProjectName")
        private WebElement inputProjectName;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;
    }
}
