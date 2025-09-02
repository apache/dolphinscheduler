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

import org.apache.dolphinscheduler.e2e.pages.common.HttpInput;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;

import org.openqa.selenium.WebDriver;

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


public class GrpcTaskForm extends TaskNodeForm {

    private WebDriver driver;

    private String url;

    @FindBys({

            @FindBy(className = "n-tree-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement sevicesDefinition;

    @FindBys({

            @FindBy(className = "n-tree-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement methodName;

    @FindBys({

            @FindBy(className = "n-tree-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement message;

    @FindBys({

            @FindBy(className = "n-tree-select"),
            @FindBy(className = "n-base-selection"),
    })
    private WebElement checkCondition;


    public GrpcTaskForm(WorkflowForm parent) {
        super(parent);

        this.driver = parent.driver();

        PageFactory.initElements(driver, this);
    }

    public GrpcTaskForm inputUrl(String script) {
        this.url=script;
        return this;
    }

    public GrpcTaskForm inputServiceDefinition(String serviceDefinition) {
        return this;
    }

    public GrpcTaskForm inputMethodName(String methodName) {
        return this;
    }

    public GrpcTaskForm inputMessage(String message) {
        return this;
    }

    public GrpcTaskForm inputCheckCondition(String checkCondition) {
        return this;
    }
}
