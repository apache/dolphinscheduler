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
package org.apache.dolphinscheduler.e2e.pages.project.workflow.task;

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.common.CodeEditor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

@Getter
public final class SwitchTaskIfBranch {
    private final WebDriver driver;
    private final SwitchTaskForm parent;

    private final CodeEditor codeEditor;

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "switch-list"),
            @FindBy(className = "el-input")
    })
    private WebElement inputIfBranch;

    public SwitchTaskIfBranch(SwitchTaskForm parent) {
        this.parent = parent;
        this.driver = parent.parent().driver();

        this.codeEditor = new CodeEditor(driver);

        PageFactory.initElements(driver, this);
    }
}
