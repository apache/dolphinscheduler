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
package org.apache.dolphinscheduler.base;


import org.apache.dolphinscheduler.constant.TestConstant;
import org.apache.dolphinscheduler.util.PropertiesReader;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * base driver class
 */
public class BaseDriver {
    /**
     * driver
     */
    private WebDriver driver;

    /**
     * chrome driver path
     */
    private String chromeDriverPath;


    /**
     * implicitly wait time（s）
     */
    private long implicitlyWait;

    /**
     * page load timeout（s）
     */
    private long pageLoadTimeout;

    /**
     * script Timeout（s）
     */
    private long setScriptTimeout;


    /**
     * Local thread storage is used to store the driver
     */
    public static ThreadLocal<WebDriver> threadLocal = new ThreadLocal<>();

    /**
     *Initialization parameters
     */
    public BaseDriver() throws IOException {
        /* driver test class path */
        chromeDriverPath = PropertiesReader.getKey("driver.chromeDriver");
        /* wait timeout */
        implicitlyWait = Long.valueOf(PropertiesReader.getKey("driver.timeouts.implicitlyWait"));
        pageLoadTimeout = Long.valueOf(PropertiesReader.getKey("driver.timeouts.pageLoadTimeout"));
        setScriptTimeout = Long.valueOf(PropertiesReader.getKey("driver.timeouts.setScriptTimeout"));
    }


    /**
     * start chrome browser
     */
    public void startBrowser() throws Exception {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(implicitlyWait, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(setScriptTimeout, TimeUnit.SECONDS);
    }

    /**
     * get webdriver
     *
     * @return driver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * set webdriver
     *
     * @param driver driver
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
        // Thread local storage
        threadLocal.set(driver);
    }

    /**
     * close browser
     */
    public void closeBrowser() throws InterruptedException {
        // JS Show a pop-up box to indicate the end of the test
        Thread.sleep(TestConstant.THREE_THOUSANG);
        ((JavascriptExecutor) driver).executeScript("alert('Test completed, browser closes after 3s')");
        Thread.sleep(TestConstant.THREE_THOUSANG);
        if (driver != null) {
            driver.quit();
        }
    }
}
