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

package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * hive conf utils test
 */
public class HiveConfUtilsTest {

    /**
     * test is hive conf var
     */
    @Test
    public void testIsHiveConfVar() {

        String conf = "hive.exec.script.wrapper=123";
        boolean hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertTrue(hiveConfVar);

        conf = "hive.test.v1=v1";
        hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertFalse(hiveConfVar);

        conf = "tez.queue.name=tezQueue";
        hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertTrue(hiveConfVar);

    }
}
