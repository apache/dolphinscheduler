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

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
@Slf4j
public final class MultipleCodeEditor {

    private List<WebElement> editors = new ArrayList<>();

    private List<List<WebElement>> editorLines = new ArrayList<>();

    private WebDriver driver;

    public MultipleCodeEditor(WebDriver driver) {
        this.driver = driver;
        locateEditors();
        locateLines();
    }
    public MultipleCodeEditor locateEditors() {
        editors.clear();
        editors = driver.findElements(By.className("monaco-editor"));
        return this;
    }

    public MultipleCodeEditor locateLines() {
        editorLines.clear();
        for (WebElement element : editors) {
            List<WebElement> lines = element.findElements(By.className("view-line"));
            editorLines.add(lines);
        }
        return this;
    }

    @SneakyThrows
    public MultipleCodeEditor content(int editorIndex, String content) {
        locateEditors();
        if (editorIndex >= editors.size()) {
            throw new IllegalArgumentException("editorIndex out of range");
        }

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editors.get(editorIndex));
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            log.warn("scroll bar not found, skipping...");
        }
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(editors.get(editorIndex)));

        Actions actions = new Actions(this.driver);
        actions.moveToElement(editors.get(editorIndex))
                .click()
                .sendKeys(Constants.LINE_SEPARATOR)
                .perform();

        Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);

        locateLines();

        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(editorLines.get(editorIndex).get(0)));

        String[] contentList = content.split(Constants.LINE_SEPARATOR);
        for (int i = 0; i < contentList.length; i++) {
            contentList[i] = contentList[i].trim();
        }
        String rebuiltContent = String.join(Constants.LINE_SEPARATOR, contentList) + Constants.LINE_SEPARATOR;
        actions.moveToElement(lineElement(editorIndex, 0))
                .click()
                .sendKeys(rebuiltContent)
                .perform();
        clearTail(actions, content.length() + 1);
        return this;
    }

    private void clearLine(Actions actions, WebElement element) throws InterruptedException {
        actions.moveToElement(element)
                .click()
                .sendKeys(Keys.BACK_SPACE)
                .perform();
    }

    private void clearTail(Actions actions, int length) throws InterruptedException {
        for (int i = 0; i < length; i++) {
            actions.sendKeys(Keys.DELETE);
        }
        actions.perform();
    }

    private WebElement lineElement(int editorIndex, int lineNumber) {
        locateLines();
        return editorLines
                .get(editorIndex)
                .get(lineNumber);
    }
}
