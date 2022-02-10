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
import org.apache.dolphinscheduler.e2e.pages.project.workflow.TaskInstanceTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowDefinitionTab;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowInstanceTab;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import lombok.Getter;

@Getter
public final class ProjectDetailPage extends NavBarPage {
    @FindBy(className = "tab-process-definition")
    private WebElement menuProcessDefinition;
    @FindBy(className = "tab-process-instance")
    private WebElement menuProcessInstances;
    @FindBy(className = "tab-task-instance")
    private WebElement menuTaskInstances;

    public ProjectDetailPage(RemoteWebDriver driver) {
        super(driver);
    }

    public <T extends Tab> T goToTab(Class<T> tab) {
        if (tab == WorkflowDefinitionTab.class) {
            menuProcessDefinition().click();
            return tab.cast(new WorkflowDefinitionTab(driver));
        }
        if (tab == WorkflowInstanceTab.class) {
            menuProcessInstances().click();
            return tab.cast(new WorkflowInstanceTab(driver));
        }
        if (tab == TaskInstanceTab.class) {
            menuTaskInstances().click();
            return tab.cast(new TaskInstanceTab(driver));
        }

        throw new UnsupportedOperationException("Unknown tab: " + tab.getName());
    }

    public interface Tab {
    }
}
