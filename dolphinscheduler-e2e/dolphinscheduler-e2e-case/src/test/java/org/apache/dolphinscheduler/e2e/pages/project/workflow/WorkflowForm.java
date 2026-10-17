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

package org.apache.dolphinscheduler.e2e.pages.project.workflow;

import org.apache.dolphinscheduler.e2e.core.UiInteraction;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.FlinkStreamTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.HttpTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.JavaTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.PythonTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SeaTunnelTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.ShellTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SubWorkflowTaskForm;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.task.SwitchTaskForm;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.io.Resources;

@SuppressWarnings("UnstableApiUsage")
@Getter
@Slf4j
public final class WorkflowForm {

    private static final String MAIN_GRAPH = ".dag-container .x6-graph-scroller";

    private WebDriver driver;
    private final WorkflowSaveDialog saveForm;
    private final WorkflowFormatDialog formatDialog;

    @FindBy(className = "graph-format")
    private WebElement formatBtn;

    @FindBy(className = "btn-save")
    private WebElement buttonSave;

    public WorkflowForm(WebDriver driver) {
        this.driver = driver;
        this.saveForm = new WorkflowSaveDialog(this);
        this.formatDialog = new WorkflowFormatDialog(this);

        PageFactory.initElements(driver, this);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T addTask(TaskType type) {
        log.info("Waiting for DAG canvas and task menu: type={}, url={}", type, driver.getCurrentUrl());
        WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage("DAG graph has not mounted before adding task " + type)
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dag-container .x6-graph-svg")));
        // Task entries are populated by the asynchronous DAG menu request after navigation.
        final WebElement task = WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage("DAG task menu has not rendered a draggable entry for " + type)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".task-item-" + type.name() + "[draggable='true']")));
        final WebElement canvas = driver.findElement(By.className("dag-container"));
        log.info("Dragging DAG task: type={}, sourceLocation={}, sourceSize={}, canvasLocation={}, canvasSize={}",
                type, task.getLocation(), task.getSize(), canvas.getLocation(), canvas.getSize());

        final JavascriptExecutor js = (JavascriptExecutor) driver;
        final String dragAndDrop = String.join("\n",
                Resources.readLines(Resources.getResource("dragAndDrop.js"), StandardCharsets.UTF_8));
        js.executeScript(dragAndDrop, task, canvas);
        WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage("Task settings did not open after dragging " + type)
                .until(ExpectedConditions
                        .visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Current node settings')]")));
        log.info("DAG task settings opened: type={}", type);

        switch (type) {
            case SHELL:
                return (T) new ShellTaskForm(this);
            case SUB_WORKFLOW:
                return (T) new SubWorkflowTaskForm(this);
            case SWITCH:
                return (T) new SwitchTaskForm(this);
            case HTTP:
                return (T) new HttpTaskForm(this);
            case JAVA:
                return (T) new JavaTaskForm(this);
            case PYTHON:
                return (T) new PythonTaskForm(this);
            case SEATUNNEL:
                return (T) new SeaTunnelTaskForm(this);
            case FLINK_STREAM:
                return (T) new FlinkStreamTaskForm(this);
        }
        throw new UnsupportedOperationException("Unknown task type");
    }

    public WorkflowForm openTaskCategory(String category) {
        // The task palette is an accordion; wait until the previous task modal has closed.
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[data-modal-ready]")));
        UiInteraction.click(driver, ".task-cate-" + category + " .n-collapse-item__header");
        return this;
    }

    public WebElement getTask(String taskName) {
        WebElement task = findTask(taskName);

        Actions action = new Actions(driver);
        action.doubleClick(task).build().perform();

        return task;
    }

    public String copyTask(String taskName) {
        WebElement source = findTask(taskName);
        Set<String> existing = taskNames();
        new Actions(driver).contextClick(source).perform();
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'dag-context-menu')]//button[normalize-space(.)='Copy']")))
                .click();
        return WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage("Copied task did not appear: " + taskName)
                .until(ignored -> taskNames().stream().filter(name -> !existing.contains(name)).findFirst()
                        .orElse(null));
    }

    public List<String> edgeDashArrays() {
        return driver.findElements(By.cssSelector(MAIN_GRAPH + " g[data-shape='dag-edge']")).stream()
                .map(edge -> edge.findElements(By.cssSelector("path[stroke-dasharray]")).stream()
                        .map(path -> path.getAttribute("stroke-dasharray").replace(',', ' ').trim()
                                .replaceAll("\\s+", " "))
                        .distinct()
                        .collect(Collectors.joining(",")))
                .collect(Collectors.toList());
    }

    private WebElement findTask(String taskName) {
        // Match the full title inside the main graph; the minimap renders the same nodes.
        return WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage("No such task: " + taskName)
                .ignoring(StaleElementReferenceException.class)
                .until(ignored -> driver.findElements(By.cssSelector(MAIN_GRAPH + " g[data-shape='dag-task']"))
                        .stream()
                        .filter(task -> taskName.equals(title(task)))
                        .findFirst()
                        .orElse(null));
    }

    private Set<String> taskNames() {
        return driver.findElements(By.cssSelector(MAIN_GRAPH + " g[data-shape='dag-task']")).stream()
                .map(this::title)
                .collect(Collectors.toSet());
    }

    private String title(WebElement task) {
        return task.findElement(By.xpath("./*[local-name()='text']")).getText();
    }

    public WorkflowSaveDialog submit() {
        buttonSave().click();
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(.,'Basic Information')]")));
        return new WorkflowSaveDialog(this);
    }

    public WorkflowFormatDialog formatDAG() {
        formatBtn.click();

        return new WorkflowFormatDialog(this);
    }

    public enum TaskType {
        SHELL,
        SUB_WORKFLOW,
        SWITCH,
        HTTP,
        JAVA,
        PYTHON,
        SEATUNNEL,
        FLINK_STREAM
    }
}
