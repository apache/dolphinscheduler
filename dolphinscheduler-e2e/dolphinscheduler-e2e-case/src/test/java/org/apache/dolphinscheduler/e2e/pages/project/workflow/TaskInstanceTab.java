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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;
import org.apache.dolphinscheduler.e2e.pages.project.ProjectDetailPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public final class TaskInstanceTab extends NavBarPage implements ProjectDetailPage.Tab {
    @FindBy(className = "items-task-instances")
    private List<WebElement> instanceList;

    public TaskInstanceTab(RemoteWebDriver driver) {
        super(driver);
    }

    public List<Row> instances() {
        return instanceList()
            .stream()
            .filter(WebElement::isDisplayed)
            .map(Row::new)
            .filter(row -> !row.name().isEmpty())
            .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    public static class Row {
        private final WebElement row;

        public String state() {
            return row.findElement(By.className("task-instance-state")).getText();
        }

        public String name() {
            return row.findElement(By.className("task-instance-name")).getText();
        }
    }
}
