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

package org.apache.dolphinscheduler.plugin.datasource.api.utils;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DATASOURCE_ENCRYPTION_ENABLE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DATASOURCE_ENCRYPTION_SALT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordUtils {

    private static final Base64 BASE64 = new Base64();

    private PasswordUtils() {
        throw new UnsupportedOperationException("Construct PasswordUtils");
    }

    /**
     * encode password
     */
    public static String encodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }
        // if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(DATASOURCE_ENCRYPTION_SALT, DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = salt + new String(BASE64.encode(password.getBytes(
                StandardCharsets.UTF_8)));
        return new String(BASE64.encode(passwordWithSalt.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * decode password
     */
    public static String decodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }

        // if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(DATASOURCE_ENCRYPTION_SALT, DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = new String(BASE64.decode(password), StandardCharsets.UTF_8);
        if (!passwordWithSalt.startsWith(salt)) {
            log.warn("There is a password and salt mismatch: {} ", password);
            return password;
        }
        return new String(BASE64.decode(passwordWithSalt.substring(salt.length())), StandardCharsets.UTF_8);
    }

}
