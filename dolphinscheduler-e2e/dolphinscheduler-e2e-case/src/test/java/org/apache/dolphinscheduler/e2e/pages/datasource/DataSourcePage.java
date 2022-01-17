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

package org.apache.dolphinscheduler.e2e.pages.datasource;

import lombok.Getter;

import org.apache.dolphinscheduler.e2e.pages.common.NavBarPage;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;


@Getter
public class DataSourcePage extends NavBarPage implements NavBarPage.NavBarItem {

    @FindBy(id = "btnCreateDataSource")
    private WebElement buttonCreateDataSource;

    @FindBy(className = "dataSourceItems")
    private List<WebElement> dataSourceItemsList;

    @FindBys({
        @FindBy(className = "el-popconfirm"),
        @FindBy(className = "el-button--primary"),
    })
    private List<WebElement> buttonConfirm;

    private final CreateDataSourceForm createDataSourceForm;

    public DataSourcePage(RemoteWebDriver driver) {
        super(driver);

        createDataSourceForm = new CreateDataSourceForm();
    }

    public DataSourcePage createDataSource(String dataSourceType, String dataSourceName, String dataSourceDescription, String ip, String port, String userName, String password, String database,
                                           String jdbcParams) {
        buttonCreateDataSource().click();

        createDataSourceForm().selectDataSourceType().selectByVisibleText(dataSourceType.toUpperCase());
        createDataSourceForm().inputDataSourceName().sendKeys(dataSourceName);
        createDataSourceForm().inputDataSourceDescription().sendKeys(dataSourceDescription);
        createDataSourceForm().inputIP().sendKeys(ip);
        createDataSourceForm().inputPort().sendKeys(port);
        createDataSourceForm().inputUserName().sendKeys(userName);
        createDataSourceForm().inputPassword().sendKeys(password);
        createDataSourceForm().inputDataBase().sendKeys(database);

        if (!"".equals(jdbcParams)) {
            createDataSourceForm().inputJdbcParams().sendKeys(jdbcParams);
        }

        createDataSourceForm().buttonSubmit().click();

        return this;
    }

    public DataSourcePage delete(String name) {
        dataSourceItemsList()
            .stream()
            .filter(it -> it.getText().contains(name))
            .flatMap(it -> it.findElements(By.id("btnDelete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in data source list"))
            .click();

        buttonConfirm()
            .stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No confirm button when deleting"))
            .click();

        return this;
    }

    @Getter
    public class CreateDataSourceForm {
        CreateDataSourceForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(className = "el-select")
        private Select selectDataSourceType;

        @FindBy(id = "inputDataSourceName")
        private WebElement inputDataSourceName;

        @FindBy(id = "inputDataSourceDescription")
        private WebElement inputDataSourceDescription;

        @FindBy(id = "inputIP")
        private WebElement inputIP;

        @FindBy(id = "inputPort")
        private WebElement inputPort;

        @FindBy(id = "inputUserName")
        private WebElement inputUserName;

        @FindBy(id = "inputPassword")
        private WebElement inputPassword;

        @FindBy(id = "inputDataBase")
        private WebElement inputDataBase;

        @FindBy(id = "inputJdbcParams")
        private WebElement inputJdbcParams;

        @FindBy(id = "btnSubmit")
        private WebElement buttonSubmit;

        @FindBy(id = "btnCancel")
        private WebElement buttonCancel;

        @FindBy(id = "btnTestConnection")
        private WebElement btnTestConnection;
    }
}
