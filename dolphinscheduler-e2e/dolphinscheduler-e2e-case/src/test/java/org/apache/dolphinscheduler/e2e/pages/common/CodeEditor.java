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

package org.apache.dolphinscheduler.e2e.pages.common;

import org.apache.dolphinscheduler.e2e.core.UiInteraction;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;

import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Slf4j
public final class CodeEditor {

    private final WebDriver driver;

    public CodeEditor(WebDriver driver) {
        this.driver = driver;
    }

    public CodeEditor content(String content) {
        UiInteraction.click(driver, ".monaco-editor .overflow-guard");
        WebElement focusedInput = driver.switchTo().activeElement();
        if (!"textarea".equals(focusedInput.getTagName())
                || !focusedInput.getDomAttribute("class").contains("inputarea")
                || focusedInput.getDomAttribute("readonly") != null) {
            throw new IllegalStateException("Expected focus in an editable Monaco input: " + focusedInput.getTagName());
        }
        WebElement editor = focusedInput.findElement(By.xpath("ancestor::*[@data-editor-model-version][1]"));
        String initialVersion = editor.getDomAttribute("data-editor-model-version");
        int initialLength = Integer.parseInt(editor.getDomAttribute("data-editor-content-length"));
        String normalized = content.replace("\r\n", "\n");
        log.info("Replacing Monaco content: characters={}, initialVersion={}, initialLength={}",
                normalized.length(), initialVersion, initialLength);
        if (!normalized.isEmpty() || initialLength != 0) {
            Actions input = new Actions(driver).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL);
            if (normalized.isEmpty()) {
                input.sendKeys(Keys.BACK_SPACE).perform();
            } else {
                clipboard(normalized);
                input.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();
            }
            WebDriverWaitFactory.createWebDriverWait(driver)
                    .withMessage("Monaco paste did not update the model to " + normalized.length() + " characters")
                    .until(ignored -> !initialVersion.equals(editor.getDomAttribute("data-editor-model-version"))
                            && Integer.parseInt(editor.getDomAttribute("data-editor-content-length")) == normalized
                                    .length());
        }

        if (!normalized.isEmpty()) {
            // Clear the clipboard so an unsuccessful copy cannot validate the paste payload.
            String sentinel = UUID.randomUUID().toString();
            clipboard(sentinel);
            new Actions(driver)
                    .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                    .keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL)
                    .perform();
            String actual = WebDriverWaitFactory.createWebDriverWait(driver)
                    .withMessage("Monaco copy did not populate the clipboard")
                    .until(ignored -> {
                        String copied = clipboard(null);
                        return sentinel.equals(copied) ? null : copied;
                    }).replace("\r\n", "\n");
            if (!normalized.equals(actual)) {
                throw new IllegalStateException("Monaco content mismatch: expected characters=" + normalized.length()
                        + ", copied characters=" + actual.length());
            }
        }
        new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();
        log.info("Monaco content verified: characters={}", normalized.length());
        return this;
    }

    private String clipboard(String value) {
        Map<?, ?> result = (Map<?, ?>) ((JavascriptExecutor) driver).executeAsyncScript(
                "const value = arguments[0], done = arguments[arguments.length - 1];"
                        + "if (!navigator.clipboard) { done({error: 'Clipboard API unavailable; check secure origin configuration'}); return; }"
                        + "const operation = value === null ? navigator.clipboard.readText() : navigator.clipboard.writeText(value);"
                        + "operation.then(text => done({text: value === null ? text : value}),"
                        + "error => done({error: error.name + ': ' + error.message}));",
                value);
        if (result.containsKey("error")) {
            throw new IllegalStateException("Monaco clipboard operation failed: " + result.get("error"));
        }
        return (String) result.get("text");
    }
}
