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
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
@Slf4j
public final class MultipleCodeEditor {

    @FindBys({
            @FindBy(className = "monaco-editor")
    })
    private List<WebElement> editors;

    private List<List<WebElement>> editorLines = new ArrayList<>();

    @FindBys({
            @FindBy(className = "pre-tasks-model")
    })
    private WebElement scrollBar;

    private WebDriver driver;

    public MultipleCodeEditor(WebDriver driver) {
        PageFactory.initElements(driver, this);

        relocateLines();

        this.driver = driver;
    }

    public MultipleCodeEditor relocateLines() {
        editorLines.clear();
        for (WebElement element : editors) {
            List<WebElement> lines = element.findElements(By.className("view-line"));
            editorLines.add(lines);
        }
        return this;
    }

    @SneakyThrows
    public MultipleCodeEditor content(int editorIndex, String content) {
        if (editorIndex >= editors.size()) {
            throw new IllegalArgumentException("editorIndex out of range");
        }

        content += Constants.LINE_SEPARATOR;

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editors.get(editorIndex));
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            log.warn("scroll bar not found, skipping...");
        }
        Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);
        Actions actions = new Actions(this.driver);
        actions.moveToElement(editors.get(editorIndex))
                .click()
                .sendKeys(Constants.LINE_SEPARATOR)
                .perform();

        Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);
        relocateLines();

        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(editorLines.get(editorIndex).get(0)));

        List<String> contentList = List.of(content.split(Constants.LINE_SEPARATOR));

        actions.moveToElement(lineElement(editorIndex, 0))
                .click()
                .sendKeys(content)
                .perform();
        clearTail(actions, contentList.size() + 1);

        Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);

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
        relocateLines();
        return editorLines
                .get(editorIndex)
                .get(lineNumber);
    }
}
