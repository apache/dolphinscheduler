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

import org.apache.dolphinscheduler.common.constants.Constants;
<<<<<<< HEAD

import org.apache.commons.lang3.SystemUtils;
=======
>>>>>>> refs/remotes/origin/3.1.1-release

import org.apache.commons.lang3.SystemUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Constants Test
 */
public class ConstantsTest {

    /**
     * Test PID via env
     */
    @Test
    public void testPID() {
        if (SystemUtils.IS_OS_WINDOWS) {
            Assertions.assertEquals(Constants.PID, "handle");
        } else {
            Assertions.assertEquals(Constants.PID, "pid");
        }
    }

}
