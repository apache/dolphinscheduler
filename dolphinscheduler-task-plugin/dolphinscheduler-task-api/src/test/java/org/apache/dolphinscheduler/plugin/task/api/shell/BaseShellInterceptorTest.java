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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseShellInterceptorTest {

    @Test
    public void testMaskSensitiveOptionsMasksTheValueOfACredential() {
        String command = "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                + "-Dsecurity.ssl.rest.truststore-password='pa'\\''ss' -Dsecurity.ssl.protocol=TLSv1.3";

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                        + "-Dsecurity.ssl.rest.truststore-password=****** -Dsecurity.ssl.protocol=TLSv1.3",
                BaseShellInterceptor.maskSensitiveOptions(command));
    }

    @Test
    public void testMaskSensitiveOptionsKeepsTheOtherOptions() {
        String command = "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                + "-Dkubernetes.config.file=/opt/flink configs/kube.yaml";

        Assertions.assertEquals(command, BaseShellInterceptor.maskSensitiveOptions(command));
    }

    @Test
    public void testMaskSensitiveOptionsMasksAValueWithSpaces() {
        String command = "${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef "
                + "-Dsecurity.ssl.rest.truststore-password='my secret'";

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink savepoint 1234567890abcdef1234567890abcdef "
                        + "-Dsecurity.ssl.rest.truststore-password=******",
                BaseShellInterceptor.maskSensitiveOptions(command));
    }

    @Test
    public void testMaskSensitiveOptionsMasksTheSeparatedOption() {
        String command = "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                + "-D security.ssl.rest.truststore-password=secret -D security.ssl.protocol=TLSv1.3";

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink cancel 1234567890abcdef1234567890abcdef "
                        + "-D security.ssl.rest.truststore-password=****** -D security.ssl.protocol=TLSv1.3",
                BaseShellInterceptor.maskSensitiveOptions(command));
    }
}
