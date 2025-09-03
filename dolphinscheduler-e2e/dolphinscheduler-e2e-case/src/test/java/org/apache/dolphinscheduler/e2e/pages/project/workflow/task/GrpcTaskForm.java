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
import org.apache.dolphinscheduler.e2e.pages.common.MultipleCodeEditor;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

public class GrpcTaskForm extends TaskNodeForm {

    private WebDriver driver;

    private HttpInput url;

    private MultipleCodeEditor editors;

    @FindBys({

            @FindBy(className = "input-method-name"),
            @FindBy(tagName = "input")
    })
    private WebElement methodName;

    public GrpcTaskForm(WorkflowForm parent) {
        super(parent);

        this.url = new HttpInput(parent.driver());
        this.editors = new MultipleCodeEditor(parent.driver());

        this.driver = parent.driver();

        PageFactory.initElements(driver, this);
    }

    public GrpcTaskForm inputUrl(String url) {
        this.url.content(url);
        return this;
    }

    public GrpcTaskForm inputServiceDefinition(String serviceDefinition) {
        this.editors.content(0, serviceDefinition);
        return this;
    }

    public GrpcTaskForm inputMethodName(String methodName) {
        this.methodName.sendKeys(methodName);
        return this;
    }

    public GrpcTaskForm inputMessage(String message) {
        this.editors.content(1, message);
        return this;
    }
}
