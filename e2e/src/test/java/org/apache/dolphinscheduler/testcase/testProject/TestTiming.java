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
package org.apache.dolphinscheduler.testcase.testProject;

import org.apache.dolphinscheduler.base.BaseTest;
import org.apache.dolphinscheduler.page.project.TimingPage;
import org.testng.annotations.Test;

public class TestTiming extends BaseTest {
    private TimingPage timingPage;

    @Test(groups={"functionTests","createTiming"},dependsOnGroups = { "login","workflow"},description = "TestCreateTiming")
    public void testCreateTiming() throws InterruptedException {
        timingPage = new TimingPage(driver);

        System.out.println("start create timing");
        assert timingPage.createTiming();
        System.out.println("end create timing");
        System.out.println("===================================");

    }
    @Test(groups={"functionTests","timing"},dependsOnGroups = { "login","workflow"},description = "TestEditTiming")
    public void testEditTiming() throws InterruptedException {
        timingPage = new TimingPage(driver);

        System.out.println("start edit timing");
        assert timingPage.editTiming();
        System.out.println("end edit timing");
        System.out.println("===================================");
    }

    @Test(groups={"functionTests","timing"},dependsOnGroups = { "login","workflow" },description = "TestOnlineTiming")
    public void testOnlineTiming() throws InterruptedException {
        timingPage = new TimingPage(driver);

        System.out.println("start online timing");
        assert timingPage.onlineTiming();
        System.out.println("end online timing");
        System.out.println("===================================");
    }

    @Test(groups={"functionTests","timing"},dependsOnGroups = { "login","workflow"},description = "TestOfflineTiming")
    public void testOfflineTiming() throws InterruptedException {
        timingPage = new TimingPage(driver);

        System.out.println("start offline timing");
        assert timingPage.offlineTiming();
        System.out.println("end offline timing");
        System.out.println("===================================");
    }

    @Test(groups={"functionTests","timing"},dependsOnGroups = { "login","workflow"},description = "TestDeleteTiming")
    public void testDeleteTiming() throws InterruptedException {
        timingPage = new TimingPage(driver);

        System.out.println("start delete timing");
        assert timingPage.deleteTiming();
        System.out.println("end delete timing");
        System.out.println("===================================");
    }
}
