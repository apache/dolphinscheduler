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
import java.util.Date;
import org.apache.dolphinscheduler.common.model.Server;

public class ResInfoTest {
    @Test
    public void testGetHeartBeatInfo() {
        String info = ResInfo.getHeartBeatInfo(new Date());
        Assert.assertEquals(7, info.split(",").length);
    }

    @Test
    public void testParseHeartbeatForZKInfo() {
        //normal info
        String info = ResInfo.getHeartBeatInfo(new Date());
        Server s = ResInfo.parseHeartbeatForZKInfo(info);
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getResInfo());

        //null param
        s = ResInfo.parseHeartbeatForZKInfo(null);
        Assert.assertNull(s);
    }
}
