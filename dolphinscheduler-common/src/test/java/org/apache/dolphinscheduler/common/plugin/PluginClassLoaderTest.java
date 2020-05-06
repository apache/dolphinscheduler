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
package org.apache.dolphinscheduler.common.plugin;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class PluginClassLoaderTest {

    private PluginClassLoader pluginClassLoader;
    private ClassLoader parent;

    @Before
    public void setUp() {
        parent = Thread.currentThread().getContextClassLoader();
        pluginClassLoader = new PluginClassLoader(
                new URL[]{}, parent,
                null, null);
    }

    @Test
    public void loadClassNull() {
        Class clazz = null;
        try {
            clazz = pluginClassLoader.loadClass("java.lang.Object");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assertEquals(null, clazz.getClassLoader());
    }

    @Test
    public void loadClassApp() {
        Class clazz = null;
        try {
            clazz = pluginClassLoader.loadClass("org.apache.dolphinscheduler.common.Constants");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assertEquals(parent, clazz.getClassLoader());
    }

}