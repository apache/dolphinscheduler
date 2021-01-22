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
package org.apache.dolphinscheduler.plugin.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlertDataTest {

    private AlertData alertData;

    @Before
    public void before() {
        alertData = new AlertData();
        alertData.setId(1)
                .setContent("content")
                .setShowType("email")
                .setTitle("title")
                .setReceivers("receivers")
                .setReceiversCc("cc")
                .setLog("log")
                .setAlertGroupId(1);
    }

    @Test
    public void getId() {
        assertEquals(1, alertData.getId());
    }

    @Test
    public void getTitle() {
        assertEquals("title", alertData.getTitle());
    }

    @Test
    public void getContent() {
        assertEquals("content", alertData.getContent());
    }

    @Test
    public void getLog() {
        assertEquals("log", alertData.getLog());
    }

    @Test
    public void getAlertGroupId() {
        assertEquals(1, alertData.getAlertGroupId());
    }

    @Test
    public void getReceivers() {
        assertEquals("receivers", alertData.getReceivers());
    }

    @Test
    public void getReceiversCc() {
        assertEquals("cc", alertData.getReceiversCc());
    }

    @Test
    public void getShowType() {
        assertEquals("email", alertData.getShowType());
    }
}