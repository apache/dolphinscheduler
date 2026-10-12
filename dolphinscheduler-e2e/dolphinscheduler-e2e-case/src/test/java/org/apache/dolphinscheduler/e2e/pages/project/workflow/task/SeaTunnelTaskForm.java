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

package org.apache.dolphinscheduler.e2e.pages.project.workflow.task;

import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SeaTunnelTaskForm extends TaskNodeForm {

    public SeaTunnelTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public SeaTunnelTaskForm script(String script) {
        WebElement input = editorInput();
        input.sendKeys(Keys.chord(selectAllKey(), "a"), Keys.BACK_SPACE);
        String[] lines = script.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            // Replace automatic indentation, then remove any auto-closed suffix.
            input.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), Keys.DELETE, lines[i],
                    Keys.chord(Keys.SHIFT, Keys.END), Keys.DELETE);
            if (i + 1 < lines.length) {
                input.sendKeys(Keys.ENTER);
            }
        }
        return this;
    }

    public String script() {
        WebElement input = editorInput();
        input.sendKeys(Keys.chord(selectAllKey(), "a"), Keys.ARROW_LEFT);
        StringBuilder script = new StringBuilder();
        // Monaco abbreviates a selection spanning several screen-reader pages.
        // Read native single-line selections to preserve the complete script.
        while (true) {
            input.sendKeys(Keys.chord(Keys.SHIFT, Keys.END));
            String value = input.getDomProperty("value");
            int start = Integer.parseInt(input.getDomProperty("selectionStart"));
            int end = Integer.parseInt(input.getDomProperty("selectionEnd"));
            script.append(value, start, end);
            input.sendKeys(Keys.END);
            String before = input.getDomProperty("value");
            String position = input.getDomProperty("selectionStart");
            input.sendKeys(Keys.ARROW_RIGHT);
            if (before.equals(input.getDomProperty("value"))
                    && position.equals(input.getDomProperty("selectionStart"))) {
                return script.toString();
            }
            script.append('\n');
        }
    }

    private Keys selectAllKey() {
        return ((HasCapabilities) parent().driver()).getCapabilities().getPlatformName().is(Platform.MAC) ? Keys.COMMAND
                : Keys.CONTROL;
    }

    private WebElement editorInput() {
        WebElement editor = WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".n-modal .monaco-editor")));
        editor.click();
        return editor.findElement(By.cssSelector("textarea.inputarea"));
    }

    public SeaTunnelTaskForm deployMode(String deployMode) {
        WebElement input = parent().driver().findElement(By.cssSelector(".n-modal input[value='" + deployMode + "']"));
        input.findElement(By.xpath("..")).click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeSelected(input));
        return this;
    }

    public String deployMode() {
        return parent().driver().findElements(By.cssSelector(
                ".n-modal input[type=radio][value=local], .n-modal input[type=radio][value=cluster], "
                        + ".n-modal input[type=radio][value=client]"))
                .stream().filter(WebElement::isSelected).map(input -> input.getAttribute("value")).findFirst()
                .orElse("");
    }

    public WorkflowForm cancel() {
        parent().driver().findElement(By.cssSelector(".n-modal .btn-cancel")).click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.invisibilityOf(inputNodeName()));
        return parent();
    }

    public SeaTunnelTaskForm executionType(String executionType) {
        WebElement input = executionTypeInput(executionType);
        WebElement label = input.findElement(By.xpath(".."));
        ((JavascriptExecutor) parent().driver()).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                label);
        label.click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeSelected(input));
        return this;
    }

    public boolean isExecutionType(String executionType) {
        return executionTypeInput(executionType).isSelected();
    }

    private WebElement executionTypeInput(String executionType) {
        return WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                        ".seatunnel-execute-type input[value='" + executionType + "']")));
    }
}
