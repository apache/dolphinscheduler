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
package org.apache.dolphinscheduler.testcase;

import org.apache.dolphinscheduler.page.LoginPage;
import org.testng.annotations.Test;

import static org.apache.dolphinscheduler.base.BaseTest.driver;

@Test(groups={"functionTests","login"})
public class LoginTest {
    private LoginPage loginPage;

    @Test(description = "LoginTest", priority = 1)
    public void testLogin() throws InterruptedException {
        loginPage = new LoginPage(driver);
        System.out.println("===================================");
        System.out.println("jump to Chinese login page");
        loginPage.jumpPageChinese();

        System.out.println("start login");
        assert  loginPage.login();
        System.out.println("end login");
        System.out.println("===================================");

    }
}
