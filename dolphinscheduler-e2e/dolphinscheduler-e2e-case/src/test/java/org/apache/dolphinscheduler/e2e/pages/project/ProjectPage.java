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

import org.apache.dolphinscheduler.e2e.core.UiInteraction;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
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

    public ProjectPage(RemoteWebDriver driver) {
        super(driver);

        this.createProjectForm = new CreateProjectForm();

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
        await().untilAsserted(() -> assertThat(projectList())
                .as("project list should contain newly-created project")
                .anyMatch(it -> it.getText().contains(project)));
        assignWorkerGroup(project, "default");
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
        return assignResource(project, "Worker Group", workerGroup);
    }

    public ProjectPage assignResource(String project, String type, String resource) {
        openResourceAuthorization(project, type);
        selectAuthorizationOption(".select-project-resource:not(.n-select--disabled)", resource);
        UiInteraction.click(driver, ".project-resource-authorization-modal .grant-project-resource");
        await().untilAsserted(() -> assertThat(grantedResources())
                .anyMatch(row -> row.findElement(By.tagName("td")).getText().equals(resource)));
        closeResourceAuthorization();
        return this;
    }

    public ProjectPage verifyAssignedWorkerGroup(String project, String workerGroup) {
        openResourceAuthorization(project, "Worker Group");
        await().untilAsserted(() -> assertThat(grantedResources())
                .anyMatch(row -> row.findElement(By.tagName("td")).getText().equals(workerGroup)));
        closeResourceAuthorization();
        return this;
    }

    public ProjectPage removeWorkerGroup(String project, String workerGroup) {
        openResourceAuthorization(project, "Worker Group");
        WebElement row = WebDriverWaitFactory.createWebDriverWait(driver).until(ignored -> grantedResources()
                .stream()
                .filter(it -> it.findElement(By.tagName("td")).getText().equals(workerGroup))
                .findFirst().orElse(null));
        row.findElement(By.className("revoke-project-resource")).click();
        UiInteraction.click(driver, ".n-popconfirm__action .n-button--primary-type");
        await().untilAsserted(() -> assertThat(grantedResources())
                .noneMatch(it -> it.findElement(By.tagName("td")).getText().equals(workerGroup)));
        closeResourceAuthorization();
        return this;
    }

    public ProjectPage verifyRemovedWorkerGroup(String project, String workerGroup) {
        openResourceAuthorization(project, "Worker Group");
        WebDriverWaitFactory.createWebDriverWait(driver).until(ignored -> driver
                .findElements(By.cssSelector(
                        ".project-resource-authorization-modal .select-project-resource.n-select--disabled"))
                .isEmpty());
        assertThat(grantedResources())
                .noneMatch(it -> it.findElement(By.tagName("td")).getText().equals(workerGroup));
        closeResourceAuthorization();
        return this;
    }

    private void openResourceAuthorization(String project, String type) {
        projectList().stream().filter(it -> it.getText().contains(project)).findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find project: " + project))
                .findElement(By.className("assign-project-resource-btn")).click();
        selectAuthorizationOption(".select-resource-type:not(.n-select--disabled)", type);
    }

    private void selectAuthorizationOption(String selector, String label) {
        UiInteraction.click(driver, ".project-resource-authorization-modal " + selector);
        WebElement option = WebDriverWaitFactory.createWebDriverWait(driver).until(ignored -> driver
                .findElements(By.cssSelector(".n-base-select-option")).stream()
                .filter(WebElement::isDisplayed).filter(it -> it.getText().equals(label))
                .findFirst().orElse(null));
        option.click();
    }

    private List<WebElement> grantedResources() {
        return driver.findElements(
                By.cssSelector(".project-resource-authorization-modal .project-resource-grants tbody tr"));
    }

    private void closeResourceAuthorization() {
        UiInteraction.click(driver, ".project-resource-authorization-modal .btn-cancel");
        WebDriverWaitFactory.createWebDriverWait(driver).until(ignored -> driver
                .findElements(By.className("project-resource-authorization-modal")).stream()
                .noneMatch(WebElement::isDisplayed));
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

}
