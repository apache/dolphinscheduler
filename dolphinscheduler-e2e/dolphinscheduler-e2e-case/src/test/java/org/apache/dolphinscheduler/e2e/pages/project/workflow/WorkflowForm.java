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

import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SubWorkflowTaskForm;

import java.nio.charset.StandardCharsets;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.google.common.io.Resources;

import lombok.Getter;
import lombok.SneakyThrows;

@SuppressWarnings("UnstableApiUsage")
@Getter
public final class WorkflowForm {
    private final WebDriver driver;
    private final WorkflowSaveDialog saveForm;

    @FindBy(id = "btnSave")
    private WebElement buttonSave;

    public WorkflowForm(WebDriver driver) {
        this.driver = driver;
        this.saveForm = new WorkflowSaveDialog(this);

        PageFactory.initElements(driver, this);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T addTask(TaskType type) {
        final WebElement task = driver.findElement(By.className("task-item-" + type.name()));
        final WebElement canvas = driver.findElement(By.className("dag-container"));

        final JavascriptExecutor js = (JavascriptExecutor) driver;
        final String dragAndDrop = String.join("\n",
            Resources.readLines(Resources.getResource("dragAndDrop.js"), StandardCharsets.UTF_8));
        js.executeScript(dragAndDrop, task, canvas);

        switch (type) {
            case SHELL:
                return (T) new ShellTaskForm(this);
            case SUB_PROCESS:
                return (T) new SubWorkflowTaskForm(this);
        }
        throw new UnsupportedOperationException("Unknown task type");
    }

    public WorkflowSaveDialog submit() {
        buttonSave().click();

        return new WorkflowSaveDialog(this);
    }

    public enum TaskType {
        SHELL,
        SUB_PROCESS,
    }
}
