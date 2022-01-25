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
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.util.List;
import java.util.stream.Stream;

@Getter
public final class SwitchTaskForm extends TaskNodeForm {

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "dep-opt"),
            @FindBy(className = "add-dep")
    })
    private WebElement addBranchButton;

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "switch-list"),
            @FindBy(className = "el-input")
    })
    private List<WebElement> ifBranches;

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "switch-list"),
            @FindBy(className = "el-input__inner")
    })
    private List<CodeEditor> ifScriptList;

    @FindBys({
            @FindBy(className = "switch-task"),
            @FindBy(className = "clearfix list"),
            @FindBy(className = "el-input__inner")
    })
    private WebElement elseBranch;

    public SwitchTaskForm(WorkflowForm parent) {
        super(parent);
    }

    public SwitchTaskForm elseBranch(String elseBranchName) {
        elseBranch().sendKeys(elseBranchName);

        return this;
    }

    public SwitchTaskForm addIfBranch(String switchScript, String ifBranchName) {
        final int len = ifBranches().size();

        addBranchButton.click();

        ifScriptList.add(new CodeEditor(this.parent().driver()).content(switchScript));
        ifBranches().get(len).sendKeys(ifBranchName);

        return this;
    }


}
