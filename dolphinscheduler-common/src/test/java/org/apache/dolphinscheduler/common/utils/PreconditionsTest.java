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
        String testReference = "test reference";
        //test  reference is not null
        Assert.assertEquals(testReference, Preconditions.checkNotNull(testReference));
        Assert.assertEquals(testReference,Preconditions.checkNotNull(testReference,"reference is null"));
        Assert.assertEquals(testReference,Preconditions.checkNotNull(testReference,"%s is null",testReference));

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
        //test  reference is  null ,expect contains errorMessage
        try {
            Preconditions.checkNotNull(null,"reference is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString("reference is null"));
        }

        try {
            Preconditions.checkNotNull("","reference is null");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString("reference is null"));
        }

        //test  reference is  null ,expect contains errorMessageTemplate and errorMessageArgs
        try {
            Preconditions.checkNotNull(null,"%s is null",testReference);
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString(testReference + " is null"));
        }

        try {
            Preconditions.checkNotNull("","%s is null",testReference);
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), containsString(testReference + " is null"));
        }
    }

    /**
     * Test checkArgument
     */
    @Test
    public void testCheckArgument() throws Exception {

        int argument = 100;
        //boolean condition is true
        Preconditions.checkArgument(argument > 0 && argument < 200);

        //boolean condition is false
        try {
            Preconditions.checkArgument(argument > 0 && argument < 50);
        } catch (IllegalArgumentException ex) {
            assertNull(ex.getMessage());
        }

        //boolean condition is false ,expect contains errorMessage
        try {
            Preconditions.checkArgument(argument > 300, "argument is error");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), containsString("argument is error"));
        }

        //boolean condition is false,expect contains errorMessageTemplate and errorMessageArgs
        try {
            Preconditions.checkArgument(argument > 0 && argument < 99, "argument %s is error",argument);
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), containsString( "argument " + argument +  " is error"));
        }
    }

    /**
     * Test checkState
     */
    @Test
    public void testCheckState() throws Exception {
        int state = 1;
        //boolean condition is true
        Preconditions.checkState(state == 1);
        Preconditions.checkState(state > -1);

        //boolean condition is false
        try {
            Preconditions.checkState(state > 2);
        } catch (IllegalStateException ex) {
            assertNull(ex.getMessage());
        }

        //boolean condition is false ,expect contains errorMessage
        try {
            Preconditions.checkState(state < 1, "state is error");
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage(), containsString("state is error"));
        }

        //boolean condition is false,expect contains errorMessageTemplate and errorMessageArgs
        try {
            Preconditions.checkState(state < -1 , "state %s is error",state);
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage(), containsString( "state " + state +  " is error"));
        }
    }

    /**
     * Test checkElementIndex
     */
    @Test
    public void testCheckElementIndex() throws Exception {
        int index = 2;
        int size = 30;

        //boolean condition is true
        Preconditions.checkElementIndex(index, size);

        //boolean condition is false
        try {
            Preconditions.checkElementIndex(-1, 10);
        } catch (IndexOutOfBoundsException ex) {
            assertThat(ex.getMessage(), containsString("Index: -1, Size: 10"));
        }

        //boolean condition is false ,expect contains errorMessage
        try {
            Preconditions.checkElementIndex(100, 50, "index is greater than size");
        } catch (IndexOutOfBoundsException ex) {
            assertThat(ex.getMessage(), containsString("index is greater than size Index: 100, Size: 50"));
        }
    }
}
