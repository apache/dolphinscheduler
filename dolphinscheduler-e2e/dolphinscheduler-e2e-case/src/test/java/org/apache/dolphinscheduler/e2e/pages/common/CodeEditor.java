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
package org.apache.dolphinscheduler.e2e.pages.common;

import org.apache.dolphinscheduler.e2e.core.Constants;
import org.apache.dolphinscheduler.e2e.core.WebDriverWaitFactory;

import org.junit.platform.commons.util.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

import java.util.List;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class CodeEditor {
    private static final Logger log = LoggerFactory.getLogger(CodeEditor.class);
    @FindBys({
        @FindBy(className = "monaco-editor"),
        @FindBy(className = "view-line"),
    })
    private List<WebElement> editor;

    @FindBy(className = "pre-tasks-model")
    private WebElement scrollBar;

    private WebDriver driver;

    public CodeEditor(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public CodeEditor content(String content) {
        WebDriverWaitFactory.createWebDriverWait(driver).until(ExpectedConditions.elementToBeClickable(editor.get(0)));

        Actions actions = new Actions(this.driver);

        List<String> contantList = List.of(content.split(Constants.LINE_SEPARATOR));

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", scrollBar);
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            log.warn("scroll bar not found, skipping...");
        }

        for (int i = 0; i < contantList.size(); i++) {
            String editorLineText;
            String inputContent = contantList.get(i);
            if (i == 0) {
                actions.moveToElement(editor.get(i))
                    .click()
                    .sendKeys(inputContent)
                    .sendKeys(Constants.LINE_SEPARATOR)
                    .pause(Constants.DEFAULT_SLEEP_SECONDS)
                    .perform();
                continue;
            } else {
                editorLineText = editor.get(i).getText();
            }

            if (StringUtils.isNotBlank(inputContent)) {
                if (editorLineText.isEmpty()) {
                    actions.moveToElement(editor.get(i))
                        .click()
                        .sendKeys(inputContent)
                        .sendKeys(Constants.LINE_SEPARATOR)
                        .pause(Constants.DEFAULT_SLEEP_SECONDS)
                        .perform();
                } else {
                    for (int p = 0 ; p < editorLineText.strip().length(); p++) {
                        clearLine(actions, editor.get(i));
                    }
                    if (!editorLineText.isEmpty()) {
                        clearLine(actions, editor.get(i));
                    }
                    actions.moveToElement(editor.get(i))
                        .click()
                        .sendKeys(inputContent)
                        .sendKeys(Constants.LINE_SEPARATOR)
                        .pause(Constants.DEFAULT_SLEEP_SECONDS)
                        .perform();
                }
            } else {
                actions.moveToElement(editor.get(i))
                    .click()
                    .sendKeys(Constants.LINE_SEPARATOR)
                    .pause(Constants.DEFAULT_SLEEP_SECONDS)
                    .perform();
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
