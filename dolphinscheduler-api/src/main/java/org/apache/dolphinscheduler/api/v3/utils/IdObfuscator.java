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

package org.apache.dolphinscheduler.api.v3.utils;

import org.hashids.Hashids;

public class IdObfuscator {

    private static final Hashids obfuscator;
    private static final String SALT = "DOLPHINSCHEDULER_ID_OBFUSCATOR_SALT!@#";

    static {
        obfuscator = new Hashids(SALT, 10);
    }

    public static String encode(int id) {
        return obfuscator.encode(id);
    }

    public static int decode(String encodedId) {
        return (int) obfuscator.decode(encodedId)[0];
    }
}
