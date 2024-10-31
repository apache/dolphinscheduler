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

import java.util.List;

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Getter
public class JavaTaskForm extends TaskNodeForm {

    private WebDriver driver;

    @FindBys({
            @FindBy(className = "resource-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement selectResource;

    @FindBys({

            @FindBy(className = "n-tree-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement selectMainPackage;

    @FindBys({
            @FindBy(xpath = "//div[contains(@class,'n-form-item') and .//span[text()='Run Type']]"),
            @FindBy(className = "n-select"),
            @FindBy(className = "n-base-selection")

    })
    private WebElement selectRunType;

    public JavaTaskForm(WorkflowForm parent) {
        super(parent);

        this.driver = parent.driver();

        PageFactory.initElements(driver, this);
    }

    public JavaTaskForm selectJavaResource(String resourceName) {
        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(driver());
        wait.until(ExpectedConditions.elementToBeClickable(selectResource));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();",
                selectResource);
        By optionsLocator = By.className("n-tree-node-content__text");
        wait.until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        List<WebElement> options = driver.findElements(optionsLocator);
        boolean found = false;
        for (WebElement option : options) {
            if (option.getText().trim().startsWith(resourceName)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();",
                        option);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("Cannot Found: " + resourceName);
        }

        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        return this;
    }

    public JavaTaskForm selectRunType(String runType) {
        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(driver());

        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(selectRunType));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();",
                dropdown);

        By dropdownMenuLocator = By.xpath("//div[contains(@class, 'n-select-menu')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownMenuLocator));

        String optionXPath = String.format(
                "//div[contains(@class, 'n-select-menu')]//div[contains(@class, 'n-base-select-option') and normalize-space(text())='%s']",
                runType);
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(optionXPath)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", option);

        return this;
    }

    public JavaTaskForm selectMainPackage(String packageName) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectMainPackage);

        final By optionsLocator = By.className("n-tree-node-content__text");

        WebDriverWait wait = WebDriverWaitFactory.createWebDriverWait(driver());
        wait.until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

        List<WebElement> elements = driver.findElements(optionsLocator);

        WebElement targetElement = elements.stream()
                .filter(it -> it.getText().trim().equals(packageName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such package: " + packageName));

        targetElement.click();

        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);

        return this;
    }

}
