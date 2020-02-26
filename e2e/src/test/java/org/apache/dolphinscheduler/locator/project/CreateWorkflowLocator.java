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

public class CreateWorkflowLocator {
    // click project manage
    public static final By CLICK_PROJECT_MANAGE = By.xpath("//div[2]/div/a/span");

    // click project name
    public static final By CLICK_PROJECT_NAME = By.xpath("//span/a");

    // click workflow define
    public static final By CLICK_WORKFLOW_DEFINE = By.xpath("//li/span");

    // click create workflow button
    public static final By CLICK_CREATE_WORKFLOW_BUTTON = By.xpath("//button/span");

    //mouse down at shell
    public static final By MOUSE_DOWN_AT_SHELL = By.xpath("//*[@id='SHELL']/div/div");

    //mouse down at spark
    public static final By MOUSE_DOWN_AT_SPARK = By.xpath("//div[5]/div/div");

    //mouse move at DAG
    public static final By MOUSE_MOVE_SHELL_AT_DAG = By.xpath("//div[2]/div/div[2]/div[2]/div/div");

//    //click shell task
//    public static final By CLICK_SHELL_TASK = By.xpath("//div[2]/div/div[2]/div[2]/div/div");

    //input shell task _name
    public static final By INPUT_SHELL_TASK_NAME = By.xpath("//input");

    //click stop run type
    public static final By CLICK_STOP_RUN_TYPE = By.xpath("//label[2]/span/input");

    //click normal run type
    public static final By CLICK_NORMAL_RUN_TYPE = By.xpath("//span/input");

    //input shell task description
    public static final By INPUT_SHELL_TASK_DESCRIPTION = By.xpath("//label/div/textarea");

    //click task priority
    public static final By CLICK_TASK_PRIORITY = By.xpath("//span/div/div/div/div/div");

    //select task priority
    public static final By SELECT_TASK_PRIORITY = By.xpath("//li[2]/li/span");

    //click work group
    public static final By CLICK_WORK_GROUP = By.xpath("//div/div/input");

    //select work group
    public static final By SELECT_WORK_GROUP = By.xpath("//div[4]/div[2]/div/div[1]/div/input");

    //select number of failed retries
    public static final By SELECT_FAIL_RETRIES_NUMBER = By.xpath("//div[5]/div[2]/div[1]/div[1]/div/input");

    //select failed retry interval
    public static final By SELECT_FAIL_RETRIES_INTERVAL = By.xpath("//div[5]/div[2]/div[2]/div[1]/div/input");

    //click timeout alarm
    public static final By CLICK_TIMEOUT_ALARM = By.xpath("//label/div/span/span");

    //select timeout fail
    public static final By SELECT_TIMEOUT_FAIL = By.xpath("//div/div/label[2]/span/input");

    //cancel timeout alarm
    public static final By CANCEL_TIMEOUT_ALARM = By.xpath("//div/div/label/span/input");

    //select timeout alarm
    public static final By SELECT_TIMEOUT_ALARM = By.xpath("//div/div/label/span/input");

    //input timeout
    public static final By SELECT_TIMEOUT = By.xpath("//div[3]/div[2]/label/div/input");

    //input script
    public static final By INPUT_SCRIPT = By.xpath("//div[2]/div/div/div/div/div/textarea");



}
