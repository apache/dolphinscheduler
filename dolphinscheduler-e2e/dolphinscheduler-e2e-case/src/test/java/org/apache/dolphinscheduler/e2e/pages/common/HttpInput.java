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

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Getter
public class HttpInput {
    @FindBys({
            @FindBy(className = "input-url-name"),
            @FindBy(tagName = "input")
    })
    private WebElement urlInput;

    private WebDriver driver;



    public HttpInput(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public HttpInput content(String content) {
        new WebDriverWait(this.driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(urlInput));
        urlInput().sendKeys(content);
        return this;
    }

}
