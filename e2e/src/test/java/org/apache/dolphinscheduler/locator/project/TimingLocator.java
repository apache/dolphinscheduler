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
package org.apache.dolphinscheduler.locator.project;

import org.openqa.selenium.By;

public class TimingLocator {
    // create timing button
    public static final By CLICK_TIMING_BUTTON = By.xpath("//button[3]");

    public static final By CLICK_EXECUTION_TIMING_BUTTON = By.xpath("//div[3]/button/span");

    public static final By SELECT_FAILURE_STRATEGY_END = By.xpath("//label[2]/span/input");
    public static final By SELECT_FAILURE_STRATEGY_CONTINUE = By.xpath("//div[2]/div/label/span[2]");

    public static final By CLICK_NOTICE_STRATEGY = By.xpath("//div[6]/div[2]/div/div/div/input");
    public static final By SELECT_NOTICE_STRATEGY = By.xpath("//div[2]/div/div/div/ul/li[4]/span");

    public static final By CLICK_PROCESS_PRIORITY = By.xpath("//div[7]/div[2]/div/div/div/div/div/span[2]");
    public static final By SELECT_PROCESS_PRIORITY  = By.xpath("//li/li/span");

    public static final By CLICK_WORKER_GROUP = By.xpath("//div[8]/div[2]/div/div/div/input");
    public static final By SELECT_WORKER_GROUP  = By.xpath("//div[8]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By CLICK_NOTICE_GROUP = By.xpath("//div[9]/div[2]/div/div/div/input");
    public static final By SELECT_NOTICE_GROUP  = By.xpath("//div[9]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By INPUT_RECIPIENT  = By.xpath("//div[10]/div[2]/div/div/span/span/input");
    public static final By INPUT_Cc  = By.xpath("//div[11]/div[2]/div/div/span/span/input");

    public static final By CLICK_CREATE_BUTTON  = By.xpath("//div[12]/button[2]/span");

    //edit timing
    public static final By TIMING_STATE = By.xpath("//table/tr[2]/td[9]/span");

    public static final By CLICK_TIMING_MANAGEMENT_BUTTON = By.xpath("//tr[2]/td[10]/button[6]");

    public static final By WORKFLOW_NAME = By.xpath("//table/tr[2]/td[2]/span/a");

    public static final By CLICK_EDIT_TIMING_BUTTON = By.xpath("//tr[2]/td[10]/button[1]/i");

    //online timing
    public static final By TIMING_MANAGEMENT_TIMING_STATE = By.xpath("//table/tr[2]/td[7]/span");

    public static final By CLICK_ONLINE_TIMING_BUTTON = By.xpath("//table/tr[2]/td[10]/button[@title['data-original-title']='上线']");

    //offline timing
    public static final By CLICK_OFFLINE_TIMING_BUTTON = By.xpath("//table/tr[2]/td[10]/button[@title['data-original-title']='下线']");

    //delete timing
    public static final By CLICK_DELETE_TIMING_BUTTON = By.xpath("//table/tr[2]/td[10]/span/button");
    public static final By CLICK_CONFIRM_DELETE_TIMING_BUTTON = By.xpath("//div[2]/div/button[2]/span");
}
