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

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;

import lombok.Getter;
import lombok.SneakyThrows;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public final class ProjectDetailPage extends NavBarPage {

    @FindBy(css = ".tab-vertical .n-submenu:nth-of-type(2) .n-menu-item:nth-of-type(2) > .n-menu-item-content")
    private WebElement menuProcessDefinition;

    @FindBy(css = ".tab-vertical .n-submenu:nth-of-type(2) .n-menu-item:nth-of-type(3) > .n-menu-item-content")
    private WebElement menuProcessInstances;

    @FindBy(xpath = "//div[contains(@class, 'n-menu-item-content')]//div[contains(., 'Task Instance')]")
    private WebElement menuTaskInstances;

    public ProjectDetailPage(RemoteWebDriver driver) {
        super(driver);
    }

    @SneakyThrows
    public <T extends Tab> T goToTab(Class<T> tab) {
        if (tab == WorkflowDefinitionTab.class) {
            menuProcessDefinition().click();
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.urlContains("/workflow-definition"));
            return tab.cast(new WorkflowDefinitionTab(driver));
        }
        if (tab == WorkflowInstanceTab.class) {
            menuProcessInstances().click();
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .until(ExpectedConditions.urlContains("/workflow/instances"));
            return tab.cast(new WorkflowInstanceTab(driver));
        }
        if (tab == TaskInstanceTab.class) {
            menuTaskInstances().click();
            WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.urlContains("/task/instances"));
            return tab.cast(new TaskInstanceTab(driver));
        }

        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
