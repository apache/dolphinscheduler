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

package org.apache.dolphinscheduler.api.aspect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Hua Jiang
 */

public class AccessLogAspectTest {

    private AccessLogAspect accessLogAspect = new AccessLogAspect();

    @Test
    public void testHandleSensitiveData() {
        String data =
                "userPassword='7ad2410b2f4c074479a8937a28a22b8f', email='xxx@qq.com', database='null', userName='root', password='root', other='null'";
        String expected =
                "userPassword='********************************', email='xxx@qq.com', database='null', userName='root', password='****', other='null'";

        String actual = accessLogAspect.handleSensitiveData(data);

        Assertions.assertEquals(expected, actual);
    }

}
