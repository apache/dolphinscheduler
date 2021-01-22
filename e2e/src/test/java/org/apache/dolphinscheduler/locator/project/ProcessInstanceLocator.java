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

public class ProcessInstanceLocator {
    // jump Process Instance page
    // process instance state is success
    public static final By PROCESS_INSTANCE_SUCCESS_STATE = By.xpath("//table/tr[2]/td[4]/span/em[@title['data-original-title']='成功']");

    //click Process Instance name
    public static final By CLICK_PROCESS_INSTANCE_NAME = By.xpath("//div[4]/div/ul/li[2]");

    // click rerun button
    public static final By CLICK_RERUN_BUTTON = By.xpath("//tr[2]/td[14]/div[1]/button[2]");

    //assert rerun type
    public static final By RUNNING_TYPE  = By.xpath("//tr[2]/td[5]/span");
}
