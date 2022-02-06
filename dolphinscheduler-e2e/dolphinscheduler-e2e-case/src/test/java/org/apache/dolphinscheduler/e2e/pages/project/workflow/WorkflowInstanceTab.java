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
package org.apache.dolphinscheduler.e2e.pages.project.workflow;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class WorkflowInstanceTab extends NavBarPage implements ProjectDetailPage.Tab {
    @FindBy(className = "items-workflow-instances")
    private List<WebElement> instanceList;
    @FindBy(className = "select-all")
    private WebElement checkBoxSelectAll;
    @FindBy(className = "btn-delete-all")
    private WebElement buttonDeleteAll;
    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    public WorkflowInstanceTab(RemoteWebDriver driver) {
        super(driver);
    }

    public List<Row> instances() {
        return instanceList()
            .stream()
            .filter(WebElement::isDisplayed)
            .map(Row::new)
            .collect(Collectors.toList());
    }

    public WorkflowInstanceTab deleteAll() {
        if (instanceList().isEmpty()) {
            return this;
        }

        checkBoxSelectAll().click();
        buttonDeleteAll().click();
        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button is displayed"))
            .click();

        return this;
    }

    @RequiredArgsConstructor
    public static class Row {
        private final WebElement row;

        public WebElement rerunButton() {
            return row.findElement(By.className("btn-rerun"));
        }

        public boolean isSuccess() {
            return !row.findElements(By.className("success")).isEmpty();
        }

        public int executionTime() {
            return Integer.parseInt(row.findElement(By.className("execution-time")).getText());
        }

        public Row rerun() {
            row.findElements(By.className("btn-rerun"))
               .stream()
               .filter(WebElement::isDisplayed)
               .findFirst()
               .orElseThrow(() -> new RuntimeException("Cannot find rerun button"))
               .click();

            return this;
        }
    }
}
