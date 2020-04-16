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

import org.apache.dolphinscheduler.util.RedisUtil;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;


/**
 * Encapsulate the operation methods that can be used for each module page
 */
public class PageCommon extends BrowserCommon {
    /**
     * @param driver driver
     */
    public PageCommon(WebDriver driver) {
        super(driver);
    }

    /**
     * @param driver driver
     * @param jedis jedis
     */
    public PageCommon(WebDriver driver, Jedis jedis) {
        super(driver, jedis);
    }

    /**
     * @param driver driver
     * @param redisUtil redisUtil
     */
    public PageCommon(WebDriver driver, RedisUtil redisUtil) {
        super(driver, redisUtil);
    }
}
