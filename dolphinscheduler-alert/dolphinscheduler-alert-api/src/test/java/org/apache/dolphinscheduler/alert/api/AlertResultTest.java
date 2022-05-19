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

package org.apache.dolphinscheduler.alert.api;

import org.junit.Assert;
import org.junit.Test;

public class AlertResultTest {

    //Testing that two AlertResult objects are equal when they have same messages and status
    @Test
    public void testAlertResultEqualsTrue(){
        AlertResult ar1 = new AlertResult("Status", "Message");
        AlertResult ar2 = new AlertResult("Status", "Message");
        Assert.assertTrue(ar1.equals(ar2));
    }

    //Testing that two AlertResult objects are not equal when they have different messages and statuses
    @Test
    public void testAlertResultEqualsFalse(){
        AlertResult ar1 = new AlertResult("Status1","Message1");
        AlertResult ar2 = new AlertResult("Status2","Message2");
        Assert.assertFalse(ar1.equals(ar2));
    }

    //Testing that equals() method will be false when comparing an AlertResult to a different type object
    @Test
    public void testAlertResultEqualsDifferentInstanceFalse(){
        AlertResult ar1 = new AlertResult("Message1", "Status1");
        String s2 = "Message1Status1";
        Assert.assertFalse(ar1.equals(s2));
    }

}
