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

import lombok.Getter;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

@Getter
public final class FlinkMaterializedTableTaskForm extends TaskNodeForm {


    @FindBys({
            @FindBy(className = "input-identifier"),
            @FindBy(tagName = "input"),
    })
    private WebElement inputIdentifier;

        @FindBys({
            @FindBy(className = "input-gateway-endpoint"),
            @FindBy(tagName = "input"),
    })
    private WebElement inputGatewayEndpoint;

    @FindBys({
            @FindBy(className = "input-dynamic-options"),
            @FindBy(tagName = "input"),
    })
    private WebElement dynamicOptions;

    @FindBys({
            @FindBy(className = "input-static-partitions"),
            @FindBy(tagName = "input"),
    })
    private WebElement staticPartitions;

    @FindBys({
            @FindBy(className = "input-init-config"),
            @FindBy(tagName = "input"),
    })
    private WebElement initConfig;

    @FindBys({
            @FindBy(className = "input-execution-config"),
            @FindBy(tagName = "input"),
    })
    private WebElement executionConfig;

    private final WebDriver driver;

    public FlinkMaterializedTableTaskForm(WorkflowForm parent) {
        super(parent);
        this.driver = parent.driver();
        PageFactory.initElements(driver, this);
    }

    public FlinkMaterializedTableTaskForm gatewayEndpoint(String endpoint) {
        inputGatewayEndpoint().sendKeys(endpoint);
        return this;
    }

    public FlinkMaterializedTableTaskForm identifier(String identifier) {
        inputIdentifier().sendKeys(identifier);
        return this;
    }

    public FlinkMaterializedTableTaskForm dynamicOptions(String staticPartitions) {
        dynamicOptions().sendKeys(staticPartitions);
        return this;
    }

    public FlinkMaterializedTableTaskForm staticPartitions(String staticPartitions) {
        staticPartitions().sendKeys(staticPartitions);
        return this;
    }

    public FlinkMaterializedTableTaskForm initConfig(String initConfig) {
        initConfig().sendKeys(initConfig);
        return this;
    }

    public FlinkMaterializedTableTaskForm executionConfig(String executionConfig) {
        executionConfig().sendKeys(executionConfig);
        return this;
    }
} 