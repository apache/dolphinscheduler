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
package org.apache.dolphinscheduler.common;

import org.apache.dolphinscheduler.util.PropertiesReader;
import org.apache.dolphinscheduler.util.RedisUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Encapsulate the most basic operations on the interface in the browser
 */
public class BrowserCommon {
    /**
     * driver
     */
    protected WebDriver driver;

    /**
     * actions
     */
    protected Actions actions;

    /**
     * Javascript
     */
    protected JavascriptExecutor je;

    /**
     * Show wait
     */
    protected WebDriverWait wait;

    /**
     * Jedis
     */
    protected Jedis jedis;

    /**
     * redis util
     */
    protected RedisUtil redisUtil;

    /**
     * @param driver driver
     */
    public BrowserCommon(WebDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver);
        this.je = ((JavascriptExecutor) driver);
        // show wait timeout
        long timeout = Long.valueOf(PropertiesReader.getKey("driver.timeouts.webDriverWait"));
        wait = new WebDriverWait(driver, timeout);
    }

    /**
     * @param driver driver
     * @param jedis jedis
     */
    public BrowserCommon(WebDriver driver, Jedis jedis) {
        this.driver = driver;
        this.actions = new Actions(driver);
        this.je = ((JavascriptExecutor) driver);
        // show wait timeout
        long timeout = Long.valueOf(PropertiesReader.getKey("driver.timeouts.webDriverWait"));
        wait = new WebDriverWait(driver, timeout);
        this.jedis = jedis;
    }

    /**
     * @param driver driver
     * @param redisUtil redisUtil
     */
    public BrowserCommon(WebDriver driver, RedisUtil redisUtil) {
        this.driver = driver;
        this.actions = new Actions(driver);
        this.je = ((JavascriptExecutor) driver);
        // show wait timeout
        long timeout = Long.valueOf(PropertiesReader.getKey("driver.timeouts.webDriverWait"));
        wait = new WebDriverWait(driver, timeout);
    }


    /**
     * Get WebElement element object through element positioning
     *
     * @param locator By
     * @return WebElement
     */

    public WebElement locateElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Click button element
     * @param locator By
     * @return clickButton
     */
    public WebElement clickButton(By locator) {
        WebElement buttonElement = locateElement(locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        ExpectedConditions.elementToBeClickable(locator);
        buttonElement.click();
        return buttonElement;
    }

    /**
     * Click Navigation Bar element
     * @param locator By
     * @return clickButton
     */
    public void clickTopElement(By locator) {
        WebElement element = driver.findElement(locator);
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", element);
    }


    /**
     * Click element
     *
     * @param locator By
     * @return inputElement
     */
    public WebElement clickElement(By locator) {
        WebElement clickElement = locateElement(locator);
        clickElement.click();
        return clickElement;
    }


    /**
     * input element
     *
     * @param locator By
     * @param content Input content
     * @return inputElement
     */
    public WebElement sendInput(By locator, String content) {
        WebElement inputElement = locateElement(locator);
        inputElement.clear();
        inputElement.sendKeys(content);
        return inputElement;
    }
    /**
     * clear element
     *
     * @param locator By
     */
    public WebElement clearInput(By locator) {
        WebElement clearElement = locateElement(locator);
        clearElement.click();
        clearElement.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        clearElement.sendKeys(Keys.BACK_SPACE);
        return clearElement;
    }

    /**
     * input codeMirror
     *
     * @param codeMirrorLocator By codeMirror
     * @param codeMirrorLineLocator By codeMirrorLine

     */
    public void inputCodeMirror(By codeMirrorLocator,By codeMirrorLineLocator,String content) {
        WebElement codeMirrorElement = locateElement(codeMirrorLocator);
        WebElement codeMirrorLineElement = locateElement(codeMirrorLineLocator);
        codeMirrorElement.click();
        codeMirrorLineElement.sendKeys(content);
    }

    /**
     * move to element
     * @param locator BY
     * @return actions
     */
    public Actions moveToElement(By locator){
        return actions.moveToElement(locateElement(locator));
    }

    /**
     * mouse drag  element
     *
     * @param source_locator BY
     * @param target_locator BY
     */
    public void dragAndDrop(By source_locator, By target_locator){
        WebElement sourceElement = locateElement(source_locator);
        WebElement targetElement = locateElement(target_locator);
        actions.dragAndDrop(sourceElement, targetElement).perform();
        actions.release();
    }

    public void moveToDragElement(By target_locator, int X, int Y){
        WebElement targetElement = locateElement(target_locator);
        actions.dragAndDropBy(targetElement, X, Y).perform();
        actions.release();
    }


    /**
     * jump page
     *
     * @param url url
     */
    public void jumpPage(String url) {
        driver.get(url);
    }


    /**
     * Find the next handle, recommended for two windows
     *
     * @return driver
     */
    public WebDriver switchNextHandle() {
        // Current window handle
        String currentHandle = driver.getWindowHandle();
        // All window handle
        Set<String> allHandles = driver.getWindowHandles();
        // Finding the next handle
        for (String handle : allHandles) {
            if (!handle.equals(currentHandle)) {
                return driver.switchTo().window(handle);
            }
        }
        return driver;
    }





        /**
         * Multi-window switch handle, according to the handle number passed in
         *
         * @param num Number starts from 1
         * @return driver
         */
    public WebDriver switchHandle(int num) {
        // current handle
        String currentHandle = driver.getWindowHandle();
        // all handle
        Set<String> allHandlesSet = driver.getWindowHandles();
        List<String> allHandlesList = new ArrayList<>(allHandlesSet);
        // switch handle
        return driver.switchTo().window(allHandlesList.get(num - 1));
    }

    /**
     * Switch frame structure
     *
     * @param locator frame
     * @return driver
     */
    public WebDriver switchFrame(By locator) {
        return driver.switchTo().frame(locateElement(locator));
    }

    /**
     * Switch parent frame structure
     *
     * @return driver
     */
    public WebDriver switchParentFrame() {
        return driver.switchTo().parentFrame();
    }

    /**
     * Switch out of frame structure
     *
     * @return driver
     */
    public WebDriver switchOutOfFrame() {
        return driver.switchTo().defaultContent();
    }


    /**
     * execute JS Script
     *
     * @param script JS script
     */
    public void executeScript(String script) {
        je.executeScript(script);
    }

    /**
     * execute JS Script
     *
     * @param script JS script
     * @param args   Object element array
     */
    public void executeScript(String script, Object... args) {
        je.executeScript(script, args);
    }

    /**
     * Page slide to top
     */
    public void scrollToTop() {
        executeScript("window.scrollTo(0, 0)");
    }

    /**
     * Page slides to the bottom
     */
    public void scrollToBottom() {
        executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public void scrollToElementBottom(By locator) {
        WebElement webElement = locateElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
    }


    /**
     * Page swipe makes the top of the element align with the top of the page
     *
     * @param by Elements that need to be aligned with the top of the page
     */
    public void scrollElementTopToTop(By by) {
        executeScript("arguments[0].scrollIntoView(true);", driver.findElement(by));
    }

    /**
     * Page sliding makes the bottom of the element aligned with the bottom of the page
     *
     * @param by Elements that need to be aligned with the bottom of the page
     */
    public void scrollElementBottomToBottom(By by) {
        executeScript("arguments[0].scrollIntoView(false);", driver.findElement(by));
    }


    /**
     * Determine if the current page title is the specified title
     *
     * @param title  title
     * @return boolean
     */

    public boolean ifTitleIs(String title) {
        return wait.until(ExpectedConditions.titleIs(title));
    }

    /**
     * Determines whether the current page title contains the specified text
     *
     * @param text text
     * @return boolean
     */
    public boolean ifTitleContains(String text) {
        return wait.until(ExpectedConditions.titleContains(text));
    }

    /**
     * Determines whether the text value of an element on the current page is the specified text
     *
     * @param locator By
     * @param text text
     * @return boolean
     */
    public boolean ifTextExists(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }
}
