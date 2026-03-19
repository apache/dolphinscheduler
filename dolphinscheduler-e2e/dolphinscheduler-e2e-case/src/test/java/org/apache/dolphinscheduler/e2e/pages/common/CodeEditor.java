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

import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.junit.platform.commons.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Getter
@Slf4j
public final class CodeEditor {

    private static final By EDITOR_LINE_LOCATOR = By.cssSelector(".monaco-editor .view-line");

    @FindBy(className = "pre-tasks-model")
    private WebElement scrollBar;

    private WebDriver driver;

    public CodeEditor(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public CodeEditor content(String content) {
        waitForLineCountAtLeast(1);
        Actions actions = new Actions(this.driver);

        List<String> contentList = List.of(content.split(Constants.LINE_SEPARATOR, -1));

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", scrollBar);
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            log.warn("scroll bar not found, skipping...");
        }

        for (int i = 0; i < contentList.size(); i++) {
            waitForLineCountAtLeast(i + 1);
            WebElement editorLine = editorLine(i);
            String editorLineText;
            String inputContent = contentList.get(i);
            boolean hasNextLine = i < contentList.size() - 1;
            if (i == 0) {
                typeLine(actions, editorLine, inputContent, hasNextLine);
                waitForLineContent(i, inputContent);
                if (hasNextLine) {
                    waitForLineCountAtLeast(i + 2);
                }
                continue;
            } else {
                editorLineText = editorLine.getText();
            }

            if (StringUtils.isNotBlank(inputContent)) {
                if (editorLineText.isEmpty()) {
                    typeLine(actions, editorLine, inputContent, hasNextLine);
                } else {
                    for (int p = 0; p < editorLineText.strip().length(); p++) {
                        clearLine(actions, editorLine);
                    }
                    if (!editorLineText.isEmpty()) {
                        clearLine(actions, editorLine);
                    }
                    typeLine(actions, editorLine, inputContent, hasNextLine);
                }
                waitForLineContent(i, inputContent);
            } else {
                typeLine(actions, editorLine, inputContent, hasNextLine);
            }

            if (hasNextLine) {
                waitForLineCountAtLeast(i + 2);
            }
        }

        return this;
    }

    private void typeLine(Actions actions, WebElement element, String content, boolean appendNewLine) {
        actions.moveToElement(element)
                .click()
                .sendKeys(content)
                .perform();

        if (appendNewLine) {
            actions.sendKeys(Constants.LINE_SEPARATOR)
                    .perform();
        }
    }

    private void clearLine(Actions actions, WebElement element) {
        actions.moveToElement(element)
                .click()
                .sendKeys(Keys.BACK_SPACE)
                .perform();
    }

    private void waitForLineCountAtLeast(int expectedLineCount) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(it -> editorLines().size() >= expectedLineCount);
    }

    private void waitForLineContent(int lineIndex, String expectedContent) {
        if (StringUtils.isBlank(expectedContent)) {
            return;
        }
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(it -> editorLines().size() > lineIndex
                        && editorLine(lineIndex).getText().contains(expectedContent));
    }

    private WebElement editorLine(int index) {
        return editorLines().get(index);
    }

    private List<WebElement> editorLines() {
        return driver.findElements(EDITOR_LINE_LOCATOR);
    }
}
