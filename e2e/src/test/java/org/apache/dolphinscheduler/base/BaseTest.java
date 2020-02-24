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


import org.apache.dolphinscheduler.page.LoginPage;
import org.apache.dolphinscheduler.testcase.LoginTest;
import org.apache.dolphinscheduler.util.PropertiesReader;
import org.apache.dolphinscheduler.util.RedisUtil;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Properties;

/**
 *  base test class
 */
public class BaseTest {
    /**
     * properties
     */
    private static Properties properties;

    /**
     * redis
     */
    private static JedisPool jedisPool;

    /**
     * redis util
     */
    public RedisUtil redisUtil;

    /**
     * jedis
     */
    public Jedis jedis;

    /**
     * baseDriver
     */
    private BaseDriver baseDriver;

    /**
     * driver
     */
    public WebDriver driver;

    /**
     * Executed before executing a test suite 
     * Read the test configuration file
     *
     * @param propertiesPath properties path
     * @throws IOException IOException
     */
    @BeforeSuite(alwaysRun = true)
    @Parameters({"propertiesPath"})
    public void beforeSuite(@Optional("src/test/resources/config/config.properties") String propertiesPath) throws IOException {
        // read properties
        properties = PropertiesReader.readProperties(propertiesPath);
        // redis init
        jedisPool = RedisUtil.getJedisPool();
    }

    /**
     * Executed before executing a testcase
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest() throws Exception {
        redisUtil = new RedisUtil();
        // set jedis expire time
        redisUtil.setJedisAndExpire(redisUtil.getNewJedis());
        jedis = redisUtil.getJedis();
        //base driver
        baseDriver = new BaseDriver();
        baseDriver.startBrowser();
        driver = baseDriver.getDriver();
    }

    /**
     * Executed before executing a class method in a test case
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() throws IOException, InterruptedException {

        LoginPage loginPage = new LoginPage(driver, redisUtil);
        loginPage.jumpPage();
        loginPage.login();
    }


    /**
     * Execute after executing a class method in a test case
     */
    @AfterClass(alwaysRun = true)
    public void afterClass() {
        // logout
    }

    /**
     * Execute after executing a testcase
//     */
    @AfterTest(alwaysRun = true)
    public void afterTest() throws InterruptedException {
        // close browser
        baseDriver.closeBrowser();
        // redis Connection recycling
        redisUtil.returnJedis();
    }

    /**
     * Execute after executing a testsuite
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
    }
}