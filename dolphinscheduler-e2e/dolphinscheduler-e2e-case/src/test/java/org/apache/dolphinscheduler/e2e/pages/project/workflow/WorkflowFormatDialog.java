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

import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Getter
public final class WorkflowFormatDialog {

    static final String MODAL = "//div[contains(concat(' ', normalize-space(@class), ' '), ' n-modal ')"
            + " and not(contains(@class, 'fade-in-scale-up-transition-enter-active'))"
            + " and not(contains(@class, 'fade-in-scale-up-transition-leave-active'))]"
            + "[.//*[contains(@class, 'n-card-header__main') and normalize-space(.)='Format']]";

    private final WebDriver driver;
    private final WorkflowForm parent;

    @FindBy(xpath = MODAL + "//button[contains(concat(' ', normalize-space(@class), ' '), ' btn-submit ')]")
    private WebElement buttonConfirm;

    public WorkflowFormatDialog(WorkflowForm parent) {
        this.parent = parent;
        this.driver = parent.driver();
        PageFactory.initElements(driver, this);
    }

    public WorkflowForm confirm() {
        final WebElement modal = driver.findElement(By.xpath(MODAL));
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.elementToBeClickable(buttonConfirm)).click();
        WebDriverWaitFactory.createWebDriverWait(driver)
                .until(ExpectedConditions.invisibilityOf(modal));
        return parent;
    }
}
