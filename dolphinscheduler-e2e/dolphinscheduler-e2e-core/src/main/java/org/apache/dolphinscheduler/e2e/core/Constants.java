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

package org.apache.dolphinscheduler.e2e.core;

import java.nio.file.Paths;

/**
 * Constants
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Construct Constants");
    }

    /**
     * tmp directory path
     */
    public static final String HOST_TMP_PATH = System.getProperty("java.io.tmpdir");

    /**
     * chrome download path in host
     */
    public static final String HOST_CHROME_DOWNLOAD_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "download").toFile().getAbsolutePath();

    /**
     * chrome download path in selenium/standalone-chrome-debug container
     */
    public static final String SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH = "/home/seluser/Downloads";

    /**
     * host os name
     */
    public static final String OS_NAME = System.getProperties().getProperty("os.name");

}
