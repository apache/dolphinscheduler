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

public class RunWorkflowLocator {
    /**
     * run workflow
     */
    // click run workflow button
    public static final By CLICK_RUN_WORKFLOW_BUTTON = By.xpath("//div[1]/div/table/tr[2]/td[10]/button[2]");

    //set running parameters
    public static final By SELECT_FAILURE_STRATEGY_END = By.xpath("//div[5]/div/div[2]/div/div[3]/div[2]/div/label[2]/span[1]/input");
    public static final By SELECT_FAILURE_STRATEGY_CONTINUE = By.xpath("//div[3]/div[2]/div/label[1]/span[1]/input");

    public static final By CLICK_NOTICE_STRATEGY = By.xpath("//div[4]/div[2]/div/div[1]/div/input");
    public static final By SELECT_NOTICE_STRATEGY = By.xpath("//div/ul/li[4]/span");

    public static final By CLICK_PROCESS_PRIORITY = By.xpath("//div/div/div/div/div/span[2]");
    public static final By SELECT_PROCESS_PRIORITY_HIGHEST  = By.xpath("//li[1]/li/span");

    public static final By CLICK_WORKER_GROUP = By.xpath("//div[6]/div[2]/div/div[1]/div/input");
    public static final By SELECT_WORKER_GROUP  = By.xpath("//div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By CLICK_NOTICE_GROUP = By.xpath("//div[7]/div[2]/div/div[1]/div/input");
    public static final By SELECT_NOTICE_GROUP  = By.xpath("//div[5]/div/div[2]/div/div[7]/div[2]/div/div[2]/div/div/div/ul/li/span");

    public static final By INPUT_RECIPIENT  = By.xpath("//div[8]/div[2]/div/div/span/span/input");
    public static final By INPUT_Cc  = By.xpath("//div[9]/div[2]/div/div/span/span/input");

    public static final By CLICK_RUNNING_BUTTON  = By.xpath("//div[11]/button[2]");
}
