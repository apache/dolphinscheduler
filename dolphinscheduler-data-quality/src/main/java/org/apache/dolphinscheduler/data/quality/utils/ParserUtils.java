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

package org.apache.dolphinscheduler.data.quality.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;
import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;

/**
 * ParserUtil
 */
@Slf4j
public class ParserUtils {

    private ParserUtils() {
        throw new UnsupportedOperationException("Construct ParserUtils");
    }

    public static String encode(String str) {
        String rs = str;
        try {
            rs = URLEncoder.encode(str, UTF_8.toString());
        } catch (Exception e) {
            log.error("encode str exception!", e);
        }

        return rs;
    }

    public static String decode(String str) {
        String rs = str;
        try {
            rs = URLDecoder.decode(str, UTF_8.toString());
        } catch (Exception e) {
            log.error("decode str exception!", e);
        }

        return rs;
    }
}
