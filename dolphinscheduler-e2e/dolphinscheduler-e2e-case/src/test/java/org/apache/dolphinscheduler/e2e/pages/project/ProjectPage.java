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

package org.apache.dolphinscheduler.e2e.pages.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage.NavBarItem;

import java.util.List;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

@Getter
public final class ProjectPage extends NavBarPage implements NavBarItem {

    @FindBy(className = "btn-create-project")
    private WebElement buttonCreateProject;

    @FindBy(className = "items")
    private List<WebElement> projectList;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final CreateProjectForm createProjectForm;

    private final AssignWorkerGroupForm assignWorkerGroupForm;

    public ProjectPage(RemoteWebDriver driver) {
        super(driver);

        this.createProjectForm = new CreateProjectForm();
        this.assignWorkerGroupForm = new AssignWorkerGroupForm();

        PageFactory.initElements(driver, this);
    }

    public ProjectPage create(String project) {
        buttonCreateProject().click();
        createProjectForm().inputProjectName().sendKeys(project);
        createProjectForm().buttonSubmit().click();
        return this;
    }

    public ProjectPage createProjectUntilSuccess(String project) {
        create(project);
        assignWorkerGroup(project, "default");
        await().untilAsserted(() -> assertThat(projectList())
                .as("project list should contain newly-created project")
                .anyMatch(it -> it.getText().contains(project)));
        return this;
    }

    public ProjectPage delete(String project) {
        projectList()
                .stream()
                .filter(it -> it.getText().contains(project))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find project: " + project))
                .findElement(By.className("delete")).click();

        driver.executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    public ProjectPage assignWorkerGroup(String project, String workerGroup) {
        projectList()
                .stream()
                .filter(it -> it.getText().contains(project))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find project: " + project))
                .findElement(By.className("assign-worker-group-btn")).click();

        assignWorkerGroupForm.sourceWorkerGroups()
                .stream()
                .filter(it -> it.getText().contains(workerGroup))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find source worker group: " + workerGroup))
                .click();

        assignWorkerGroupForm.buttonSubmit().click();

        return this;
    }

    public ProjectPage verifyAssignedWorkerGroup(String project, String workerGroup) {
        projectList()
                .stream()
                .filter(it -> it.getText().contains(project))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find project: " + project))
                .findElement(By.className("assign-worker-group-btn")).click();

        assignWorkerGroupForm.targetWorkerGroups()
                .stream()
                .filter(it -> it.getText().contains(workerGroup))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find target worker group: " + workerGroup))
                .click();

        assignWorkerGroupForm.buttonCancel().click();

        return this;
    }

    public ProjectDetailPage goTo(String project) {
        projectList().stream()
                .filter(it -> it.getText().contains(project))
                .map(it -> it.findElement(By.className("project-name")).findElement(new By.ByTagName("button")))
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

        @FindBys({
                @FindBy(className = "input-project-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputProjectName;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;
    }

    @Getter
    public class AssignWorkerGroupForm {

        AssignWorkerGroupForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
                @FindBy(className = "assign-worker-group-modal"),
                @FindBy(className = "n-transfer-list--source"),
                @FindBy(className = "n-transfer-list-item__label")
        })
        private List<WebElement> sourceWorkerGroups;

        @FindBys({
                @FindBy(className = "assign-worker-group-modal"),
                @FindBy(className = "n-transfer-list--target"),
                @FindBy(className = "n-transfer-list-item__label")
        })
        private List<WebElement> targetWorkerGroups;

        @FindBys({
                @FindBy(className = "assign-worker-group-modal"),
                @FindBy(className = "btn-submit"),
        })
        private WebElement buttonSubmit;

        @FindBys({
                @FindBy(className = "assign-worker-group-modal"),
                @FindBy(className = "btn-cancel"),
        })
        private WebElement buttonCancel;
    }
}
