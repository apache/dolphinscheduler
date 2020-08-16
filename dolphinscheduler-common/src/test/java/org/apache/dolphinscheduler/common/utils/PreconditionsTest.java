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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class PreconditionsTest {
    public static final Logger logger = LoggerFactory.getLogger(PreconditionsTest.class);

    /**
     * Test checkNotNull
     */
    @Test
    public void testCheckNotNull() throws Exception {
        String testReference = "test object";
        Assert.assertEquals(testReference, Preconditions.checkNotNull(testReference));
        Assert.assertEquals(testReference,Preconditions.checkNotNull(testReference,"object is null"));

        //test  reference is  null
        try {
            Preconditions.checkNotNull(null);
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage());
        }

        try {
            Preconditions.checkNotNull("");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage());
        }

        try {
            Preconditions.checkNotNull(null,"object is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString("object is null"));
        }

        try {
            Preconditions.checkNotNull("","object is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString("object is null"));
        }

    }

}
