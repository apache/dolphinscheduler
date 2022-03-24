/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.e2e.pages.security;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import java.security.Key;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import lombok.Getter;

@Getter
public final class QueuePage extends NavBarPage implements SecurityPage.Tab {
    @FindBy(className = "btn-create-queue")
    private WebElement buttonCreateQueue;

    @FindBy(className = "items")
    private List<WebElement> queueList;

    private final QueueForm createQueueForm;
    private final QueueForm editQueueForm;

    public QueuePage(RemoteWebDriver driver) {
        super(driver);
        createQueueForm = new QueueForm();
        editQueueForm = new QueueForm();
    }

    public QueuePage create(String queueName, String queueValue) {
        buttonCreateQueue().click();
        createQueueForm().inputQueueName().sendKeys(queueName);
        createQueueForm().inputQueueValue().sendKeys(queueValue);
        createQueueForm().buttonSubmit().click();
        return this;
    }

    public QueuePage update(String queueName, String editQueueName, String editQueueValue) {
        queueList()
                .stream()
                .filter(it -> it.findElement(By.className("queue-name")).getAttribute("innerHTML").contains(queueName))
                .flatMap(it -> it.findElements(By.className("edit")).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No edit button in queue list"))
                .click();

        editQueueForm().inputQueueName().sendKeys(Keys.CONTROL + "a");
        editQueueForm().inputQueueName().sendKeys(Keys.BACK_SPACE);
        editQueueForm().inputQueueName().sendKeys(editQueueName);

        editQueueForm().inputQueueValue().sendKeys(Keys.CONTROL + "a");
        editQueueForm().inputQueueValue().sendKeys(Keys.BACK_SPACE);
        editQueueForm().inputQueueValue().sendKeys(editQueueValue);

        editQueueForm().buttonSubmit().click();

        return this;
    }

    @Getter
    public class QueueForm {
        QueueForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBys({
            @FindBy(className = "input-queue-name"),
            @FindBy(tagName = "input"),
        })
        private WebElement inputQueueName;

        @FindBys({
                @FindBy(className = "input-queue-value"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputQueueValue;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;
    }
}
