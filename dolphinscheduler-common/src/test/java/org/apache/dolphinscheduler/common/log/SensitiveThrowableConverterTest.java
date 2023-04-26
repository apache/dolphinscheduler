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

package org.apache.dolphinscheduler.common.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensitiveThrowableConverterTest {

    private final Logger logger = LoggerFactory.getLogger(SensitiveThrowableConverterTest.class);

    private final static String ipPattern = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";

    private final String ipLogMsg = "UnknownHostException :172.168.31.1";

    private final String ipMaskLogMsg = "UnknownHostException :************";

    /**
     * mask sensitive logMsg - sql task datasource password
     */
    @Test
    public void testPwdLogMsgConverter() {
        SensitiveThrowableConverter.addMaskPattern(ipPattern);
        RuntimeException runtimeException = new RuntimeException(ipLogMsg);
        logger.error("error log", runtimeException);
        String ipMaskedLog = SensitiveThrowableConverter.maskSensitiveData(runtimeException.getMessage());
        Assertions.assertEquals(ipMaskedLog, ipMaskLogMsg);

    }

}
