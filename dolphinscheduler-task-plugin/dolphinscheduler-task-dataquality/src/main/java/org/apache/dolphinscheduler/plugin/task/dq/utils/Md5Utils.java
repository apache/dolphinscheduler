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

package org.apache.dolphinscheduler.plugin.task.dq.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

/**
 * Md5Utils
 */
@Slf4j
public class Md5Utils {

    private Md5Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getMd5(String src, boolean isUpper) {
        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            Base64.Encoder encoder = Base64.getEncoder();
            md5 = encoder.encodeToString(md.digest(src.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("get md5 error: {}", e.getMessage());
        }

        if (isUpper) {
            md5 = md5.toUpperCase();
        }

        return md5;
    }
}
