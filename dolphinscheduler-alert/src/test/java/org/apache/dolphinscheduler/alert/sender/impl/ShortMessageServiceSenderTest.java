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
package org.apache.dolphinscheduler.alert.sender.impl;

import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ShortMessageServiceSenderTest {

    private ShortMessageServiceSender sender;

    @Before
    public void setUp() {
        sender = new ShortMessageServiceSender();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSend() {
        assertFalse(sender.send(new Alert()));
    }

    @Test
    public void testSendGroup() {
        assertFalse(sender.send(new Alert()));
    }

    @Test
    public void testGetName() {
        assertEquals(sender.getName(), "SMS");
    }
}
