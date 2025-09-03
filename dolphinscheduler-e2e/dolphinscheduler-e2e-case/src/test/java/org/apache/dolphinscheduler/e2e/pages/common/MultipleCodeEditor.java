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

import org.junit.platform.commons.util.StringUtils;
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

    @FindBy(className = "pre-tasks-model")
    private List<WebElement> scrollBars;

    private WebDriver driver;

    public MultipleCodeEditor(WebDriver driver) {
        PageFactory.initElements(driver, this);

        for (WebElement element : editors) {
            List<WebElement> lines = element.findElements(By.className("view-line"));
            editorLines.add(lines);
        }

        this.driver = driver;
    }

    @SneakyThrows
    public MultipleCodeEditor content(int index, String content) {
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(editorLines.get(index).get(0)));

        Actions actions = new Actions(this.driver);

        List<String> contentList = List.of(content.split(Constants.LINE_SEPARATOR));

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", scrollBars.get(index));
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            log.warn("scroll bar not found, skipping...");
        }

        for (int i = 0; i < contentList.size(); i++) {
            String editorLineText;
            String inputContent = contentList.get(i);
            if (i == 0) {
                actions.moveToElement(editorLines.get(index).get(i))
                        .click()
                        .sendKeys(inputContent)
                        .sendKeys(Constants.LINE_SEPARATOR)
                        .perform();
                continue;
            } else {
                editorLineText = editorLines.get(index).get(i).getText();
            }

            if (StringUtils.isNotBlank(inputContent)) {
                if (editorLineText.isEmpty()) {
                    actions.moveToElement(editorLines.get(index).get(i))
                            .click()
                            .sendKeys(inputContent)
                            .sendKeys(Constants.LINE_SEPARATOR)
                            .perform();
                    Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);
                } else {
                    for (int p = 0; p < editorLineText.strip().length(); p++) {
                        clearLine(actions, editorLines.get(index).get(i));
                    }
                    if (!editorLineText.isEmpty()) {
                        clearLine(actions, editorLines.get(index).get(i));
                    }
                    actions.moveToElement(editorLines.get(index).get(i))
                            .click()
                            .sendKeys(inputContent)
                            .sendKeys(Constants.LINE_SEPARATOR)
                            .perform();
                    Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);
                }
            } else {
                actions.moveToElement(editorLines.get(index).get(i))
                        .click()
                        .sendKeys(Constants.LINE_SEPARATOR)
                        .perform();
                Thread.sleep(Constants.DEFAULT_SLEEP_MILLISECONDS);
            }
        }

        return this;
    }

    private void clearLine(Actions actions, WebElement element) {
        actions.moveToElement(element)
                .click()
                .sendKeys(Keys.BACK_SPACE)
                .perform();
    }
}
