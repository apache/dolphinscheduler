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
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.io.Resources;

@SuppressWarnings("UnstableApiUsage")
@Getter
@Slf4j
public final class WorkflowForm {

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
        return addTask(type, null, null);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T addTask(TaskType type, Integer x, Integer y) {
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
        js.executeScript(dragAndDrop, task, canvas, x, y);
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

    public WebElement getTask(String taskName) {
        List<WebElement> tasks = WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector("svg > g > g[class^='x6-graph-svg-stage'] > g[data-shape^='dag-task']")));

        WebElement task = tasks.stream()
                .filter(t -> t.getText().contains(taskName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such task: " + taskName));

        Actions action = new Actions(driver);
        action.doubleClick(task).build().perform();

        return task;
    }

    public String taskCode(String taskName) {
        return WebDriverWaitFactory.createWebDriverWait(driver).until(unused -> driver
                .findElements(By.cssSelector(".dag-container .x6-graph-scroller g[data-shape='dag-task']")).stream()
                .filter(node -> node.findElement(By.xpath("./*[local-name()='text']")).getText()
                        .equals(taskName))
                .map(node -> node.getAttribute("data-cell-id"))
                .findFirst().orElse(null));
    }

    private WebElement taskLabelInView(String code) {
        By label = By.cssSelector(".dag-container .x6-graph-scroller g[data-shape='dag-task'][data-cell-id='"
                + code + "'] > text > tspan");
        return WebDriverWaitFactory.createWebDriverWait(driver)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .withMessage("Task label is hidden or its center is covered: " + code).until(unused -> {
                    WebElement node = ExpectedConditions.elementToBeClickable(label).apply(driver);
                    if (node == null) {
                        return null;
                    }
                    WebElement scroller = driver.findElement(By.cssSelector(".dag-container .x6-graph-scroller"));
                    Rectangle view = scroller.getRect();
                    Rectangle text = node.getRect();
                    int deltaX = text.x + text.width / 2 - view.x - view.width / 2;
                    int deltaY = text.y + text.height / 2 - view.y - view.height / 2;
                    // Center inside the nested scroller, away from its fixed minimap.
                    if (Math.abs(deltaX) > 2 || Math.abs(deltaY) > 2) {
                        new Actions(driver)
                                .scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(scroller), deltaX, deltaY)
                                .perform();
                        return null;
                    }
                    // Target the rendered text span: the body is covered by this task label.
                    // Native click scrolls the label into view and rejects an intercepted pointer.
                    // Reuse this span for Actions so SVG text-container bounds cannot shift the pointer.
                    node.click();
                    return node;
                });
    }

    public void openTask(String code) {
        new Actions(driver).doubleClick(taskLabelInView(code)).perform();
        // Visible controls still move while their modal is entering or leaving.
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".n-modal:not(.fade-in-scale-up-transition-enter-active)"
                        + ":not(.fade-in-scale-up-transition-leave-active) .input-node-name input")));
    }

    public String copyTask(String code) {
        Set<String> existing = driver
                .findElements(By.cssSelector(".dag-container .x6-graph-scroller g[data-shape='dag-task']")).stream()
                .map(node -> node.getAttribute("data-cell-id")).collect(Collectors.toSet());
        new Actions(driver).contextClick(taskLabelInView(code)).perform();
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class, 'dag-context-menu')]//button[normalize-space(.)='Copy']"))).click();
        return WebDriverWaitFactory.createWebDriverWait(driver).until(unused -> {
            List<String> added = driver
                    .findElements(By.cssSelector(".dag-container .x6-graph-scroller g[data-shape='dag-task']")).stream()
                    .map(node -> node.getAttribute("data-cell-id"))
                    .filter(id -> !existing.contains(id)).collect(Collectors.toList());
            return added.size() == 1 ? added.get(0) : null;
        });
    }

    public void waitForEdgeStyle(int count, boolean stream) {
        WebDriverWaitFactory.createWebDriverWait(driver).until(unused -> {
            List<WebElement> edges =
                    driver.findElements(By.cssSelector(".dag-container .x6-graph-scroller g[data-shape='dag-edge']"));
            return edges.size() == count && edges.stream().allMatch(edge -> {
                List<WebElement> lines = edge.findElements(By.cssSelector("path[stroke-dasharray]"));
                return !lines.isEmpty() && lines.stream()
                        .allMatch(line -> line.getAttribute("stroke-dasharray").replace(',', ' ').trim()
                                .replaceAll("\\s+", " ")
                                .equals(stream ? "5 5" : "none"));
            });
        });
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
