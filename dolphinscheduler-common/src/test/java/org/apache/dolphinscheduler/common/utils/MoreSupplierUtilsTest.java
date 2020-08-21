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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.apache.dolphinscheduler.common.utils.MoreSupplierUtils.LazyCloseableSupplier;
import org.apache.dolphinscheduler.common.utils.MoreSupplierUtils.LazyCloseableThrowableSupplier;

import org.junit.Assert;
import org.junit.Test;

/**
 * More supplier utils
 */
public class MoreSupplierUtilsTest {

    @Test(expected = InvocationTargetException.class)
    public void testConstructMoreSupplierUtils()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<MoreSupplierUtils> clazz = MoreSupplierUtils.class;
        Constructor<MoreSupplierUtils> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testLazy() {
        final String expectedResult = "5";

        Supplier<String> stringInitSupplier = () -> String.valueOf(5);

        LazyCloseableSupplier<String> lazyCloseableThrowableSupplier =
                MoreSupplierUtils.lazy(stringInitSupplier);

        String result = lazyCloseableThrowableSupplier.get();

        Assert.assertEquals(expectedResult, result);
        Assert.assertTrue(lazyCloseableThrowableSupplier.isInitialized());

        String resultGetTwice = lazyCloseableThrowableSupplier.get();
        Assert.assertEquals(expectedResult, resultGetTwice);
    }

    @Test
    public void testLazyCloseable() {
        final String expectedResult = "6";

        Supplier<CloseableTester> stringInitSupplier = () -> new CloseableTester("6");

        LazyCloseableSupplier<CloseableTester> lazyCloseableThrowableSupplier =
                MoreSupplierUtils.lazyCloseable(stringInitSupplier, CloseableTester::close);

        CloseableTester result = lazyCloseableThrowableSupplier.get();

        Assert.assertEquals(expectedResult, result.getS());
        Assert.assertTrue(lazyCloseableThrowableSupplier.isInitialized());

        lazyCloseableThrowableSupplier.close();

        Assert.assertEquals("close", result.getS());
        Assert.assertFalse(lazyCloseableThrowableSupplier.isInitialized());
    }

    @Test
    public void testLazyThrowable() throws IOException {
        final String expectedResult = "5";

        ThrowableSupplier<String, IOException> stringInitSupplier = () -> String.valueOf(5);

        LazyCloseableThrowableSupplier<String, IOException, RuntimeException> lazyCloseableThrowableSupplier =
                MoreSupplierUtils.lazyThrowable(stringInitSupplier);

        String result = lazyCloseableThrowableSupplier.get();

        Assert.assertEquals(expectedResult, result);
        Assert.assertTrue(lazyCloseableThrowableSupplier.isInitialized());

        String resultGetTwice = lazyCloseableThrowableSupplier.get();
        Assert.assertEquals(expectedResult, resultGetTwice);
    }

    @Test
    public void testLazyCloseableThrowable() throws IOException {
        final String expectedResult = "6";

        ThrowableSupplier<CloseableTester, IOException> stringInitSupplier =
                () -> new CloseableTester("6");

        LazyCloseableThrowableSupplier<CloseableTester, IOException, RuntimeException> lazyCloseableThrowableSupplier =
                MoreSupplierUtils.lazyCloseableThrowable(stringInitSupplier, CloseableTester::close);

        CloseableTester result = lazyCloseableThrowableSupplier.get();

        Assert.assertEquals(expectedResult, result.getS());
        Assert.assertTrue(lazyCloseableThrowableSupplier.isInitialized());

        lazyCloseableThrowableSupplier.close();

        Assert.assertEquals("close", result.getS());
        Assert.assertFalse(lazyCloseableThrowableSupplier.isInitialized());
    }

    private static class CloseableTester implements Closeable {

        private String s;

        public CloseableTester(String s) {
            this.s = s;
        }

        public String getS() {
            return s;
        }

        @Override
        public void close() {
            this.s = "close";
        }
    }
}
