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

public class IpUtilsTest {

    @Test
    public void ipToLong() {

        String ip = "192.168.110.1";
        String ip2 = "0.0.0.0";
        long longNumber = IpUtils.ipToLong(ip);
        long longNumber2 = IpUtils.ipToLong(ip2);
        System.out.println(longNumber);
        Assert.assertEquals(3232263681L, longNumber);
        Assert.assertEquals(0L, longNumber2);

        String ip3 = "255.255.255.255";
        long longNumber3 = IpUtils.ipToLong(ip3);
        System.out.println(longNumber3);
        Assert.assertEquals(4294967295L, longNumber3);

    }

    @Test
    public void longToIp() {

        String ip = "192.168.110.1";
        String ip2 = "0.0.0.0";
        long longNum = 3232263681L;
        String i1 = IpUtils.longToIp(longNum);

        String i2 = IpUtils.longToIp(0);

        Assert.assertEquals(ip, i1);
        Assert.assertEquals(ip2, i2);
    }
}