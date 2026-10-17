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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FlinkStreamTaskForm extends TaskNodeForm {

    public FlinkStreamTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public FlinkStreamTaskForm programType(String programType) {
        WebDriverWaitFactory.createWebDriverWait(parent().driver()).until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector("[data-modal-ready='true'] .input-node-name input")));
        WebElement selection = WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeClickable(formItem("Program Type", "n-base-selection")));
        scrollToControl(selection);
        selection.click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver()).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'n-base-select-menu')]"
                        + "//div[contains(@class, 'n-base-select-option') and normalize-space(.)='" + programType
                        + "']")))
                .click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.textToBePresentInElementLocated(
                        formItem("Program Type", "n-base-selection"), programType));
        return this;
    }

    public FlinkStreamTaskForm deployMode(String deployMode) {
        WebElement input = WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".n-modal input[type=radio][value='" + deployMode + "']")));
        WebElement label = input.findElement(By.xpath(".."));
        scrollToControl(label);
        label.click();
        WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeSelected(input));
        return this;
    }

    // CodeEditor needs exactly one visible editor, but SQL mode also shows the initialization script editor.
    public FlinkStreamTaskForm script(String script) {
        WebElement editor = WebDriverWaitFactory.createWebDriverWait(parent().driver())
                .until(ExpectedConditions.elementToBeClickable(formItem("Script", "monaco-editor")));
        scrollToControl(editor);
        editor.click();
        WebElement input = editor.findElement(By.cssSelector("textarea.inputarea"));
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        String[] lines = script.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            // Replace automatic indentation, then remove any auto-closed suffix.
            input.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), Keys.DELETE, lines[i],
                    Keys.chord(Keys.SHIFT, Keys.END), Keys.DELETE);
            if (i + 1 < lines.length) {
                // Insert a line without accepting Monaco's word suggestions.
                input.sendKeys(Keys.chord(Keys.CONTROL, Keys.ENTER));
            }
        }
        return this;
    }

    private By formItem(String label, String control) {
        return By.xpath("//div[contains(@class, 'n-modal')]"
                + "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-form-item ')"
                + " and .//*[normalize-space(text())='" + label + "']]//div[contains(@class, '" + control + "')]");
    }

    private void scrollToControl(WebElement element) {
        // Scroll past the bottom edge so the modal footer does not cover the control.
        new Actions(parent().driver()).scrollToElement(element)
                .scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(element), 0, 200).perform();
    }
}
