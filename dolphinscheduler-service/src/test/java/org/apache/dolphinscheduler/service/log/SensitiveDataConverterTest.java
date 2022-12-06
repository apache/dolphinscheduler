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

<<<<<<<< HEAD:dolphinscheduler-service/src/test/java/org/apache/dolphinscheduler/service/log/SensitiveDataConverterTest.java
package org.apache.dolphinscheduler.service.log;

import static org.apache.dolphinscheduler.service.log.SensitiveDataConverter.passwordHandler;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
========
package org.apache.dolphinscheduler.common.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
>>>>>>>> refs/remotes/origin/3.1.1-release:dolphinscheduler-common/src/test/java/org/apache/dolphinscheduler/common/log/SensitiveDataConverterTest.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensitiveDataConverterTest {

    private final Logger logger = LoggerFactory.getLogger(SensitiveDataConverterTest.class);

<<<<<<<< HEAD:dolphinscheduler-service/src/test/java/org/apache/dolphinscheduler/service/log/SensitiveDataConverterTest.java
    /**
     * password pattern
     */
    private final Pattern pwdPattern = Pattern.compile(DataSourceConstants.DATASOURCE_PASSWORD_REGEX);

========
>>>>>>>> refs/remotes/origin/3.1.1-release:dolphinscheduler-common/src/test/java/org/apache/dolphinscheduler/common/log/SensitiveDataConverterTest.java
    private final String logMsg = "{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\","
            + "\"database\":\"carbond\","
            + "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\","
            + "\"user\":\"view\","
            + "\"password\":\"view1\"}";

    private final String maskLogMsg = "{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\","
            + "\"database\":\"carbond\","
            + "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\","
            + "\"user\":\"view\","
            + "\"password\":\"*****\"}";
<<<<<<<< HEAD:dolphinscheduler-service/src/test/java/org/apache/dolphinscheduler/service/log/SensitiveDataConverterTest.java

    @Test
    public void convert() {
        Assert.assertEquals(maskLogMsg, passwordHandler(pwdPattern, logMsg));
    }
========
>>>>>>>> refs/remotes/origin/3.1.1-release:dolphinscheduler-common/src/test/java/org/apache/dolphinscheduler/common/log/SensitiveDataConverterTest.java

    /**
     * mask sensitive logMsg - sql task datasource password
     */
    @Test
    public void testPwdLogMsgConverter() {
        final String maskedLog = SensitiveDataConverter.maskSensitiveData(logMsg);

<<<<<<<< HEAD:dolphinscheduler-service/src/test/java/org/apache/dolphinscheduler/service/log/SensitiveDataConverterTest.java
        Assert.assertNotEquals(logMsg, passwordHandler(pwdPattern, logMsg));
        Assert.assertEquals(maskLogMsg, passwordHandler(pwdPattern, logMsg));
========
        logger.info("original parameter : {}", logMsg);
        logger.info("masked parameter : {}", maskedLog);

        Assertions.assertEquals(maskLogMsg, maskedLog);
>>>>>>>> refs/remotes/origin/3.1.1-release:dolphinscheduler-common/src/test/java/org/apache/dolphinscheduler/common/log/SensitiveDataConverterTest.java

    }

}
