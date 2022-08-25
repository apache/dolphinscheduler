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

import java.security.Key;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


@Getter
public class DataSourcePage extends NavBarPage implements NavBarPage.NavBarItem {

    @FindBy(className = "btn-create-data-source")
    private WebElement buttonCreateDataSource;

    @FindBy(className = "data-source-items")
    private List<WebElement> dataSourceItemsList;

    @FindBys({
            @FindBy(className = "n-popconfirm__action"),
            @FindBy(className = "n-button--primary-type"),
    })
    private WebElement buttonConfirm;

    private final CreateDataSourceForm createDataSourceForm;

    public DataSourcePage(RemoteWebDriver driver) {
        super(driver);

        createDataSourceForm = new CreateDataSourceForm();
    }

    public DataSourcePage createDataSource(String dataSourceType, String dataSourceName, String dataSourceDescription, String ip, String port, String userName, String password, String database,
                                           String jdbcParams, int testFlag) {
        buttonCreateDataSource().click();

        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(
            new By.ByClassName("dialog-create-data-source")));

        createDataSourceForm().btnDataSourceTypeDropdown().click();

        new WebDriverWait(driver, 10).until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.className("dialog-create-data-source")), dataSourceType.toUpperCase()));

        createDataSourceForm().selectDataSourceType()
            .stream()
            .filter(it -> it.getText().contains(dataSourceType.toUpperCase()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No %s in data source type list", dataSourceType.toUpperCase())))
            .click();

        createDataSourceForm().inputDataSourceName().sendKeys(dataSourceName);
        createDataSourceForm().inputDataSourceDescription().sendKeys(dataSourceDescription);
        createDataSourceForm().inputIP().sendKeys(ip);
        createDataSourceForm().inputPort().sendKeys(Keys.CONTROL + "a");
        createDataSourceForm().inputPort().sendKeys(Keys.BACK_SPACE);
        createDataSourceForm().inputPort().sendKeys(port);
        createDataSourceForm().inputUserName().sendKeys(userName);
        createDataSourceForm().inputPassword().sendKeys(password);
        createDataSourceForm().inputDataBase().sendKeys(database);
        createDataSourceForm().radioTestDatasource().click();


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
            .flatMap(it -> it.findElements(By.className("btn-delete")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No delete button in data source list"))
            .click();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonConfirm());

        return this;
    }

    @Getter
    public class CreateDataSourceForm {
        CreateDataSourceForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(className = "n-base-select-option__content")
        private List<WebElement> selectDataSourceType;

        @FindBys({
                @FindBy(className = "btn-data-source-type-drop-down"),
                @FindBy(className = "n-base-selection"),
        })
        private WebElement btnDataSourceTypeDropdown;

        @FindBys({
                @FindBy(className = "input-data-source-name"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputDataSourceName;

        @FindBys({
                @FindBy(className = "input-data-source-description"),
                @FindBy(tagName = "textarea"),
        })
        private WebElement inputDataSourceDescription;

        @FindBys({
                @FindBy(className = "input-ip"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputIP;

        @FindBys({
                @FindBy(className = "input-port"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputPort;

        @FindBys({
                @FindBy(className = "input-username"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputUserName;

        @FindBys({
                @FindBy(className = "input-password"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputPassword;

        @FindBys({
                @FindBy(className = "input-data-base"),
                @FindBy(tagName = "input"),
        })
        private WebElement inputDataBase;

        @FindBys({
                @FindBy(className = "input-jdbc-params"),
                @FindBy(tagName = "textarea"),
        })
        private WebElement inputJdbcParams;

        @FindBy(className = "radio-test-datasource")
        private WebElement radioTestDatasource;

        @FindBy(className = "radio-online-datasource")
        private WebElement radioOnlineDatasource;

        @FindBy(className = "select-bind-test-data-source-type-drop-down")
        private WebElement selectBindTestDataSourceId;

        @FindBy(className = "btn-submit")
        private WebElement buttonSubmit;

        @FindBy(className = "btn-cancel")
        private WebElement buttonCancel;

        @FindBy(className = "btn-test-connection")
        private WebElement radioTestConnection;

    }
}
