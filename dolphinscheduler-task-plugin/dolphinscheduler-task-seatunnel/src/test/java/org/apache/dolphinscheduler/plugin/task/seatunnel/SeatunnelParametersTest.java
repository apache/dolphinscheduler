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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeatunnelParametersTest {

    @Test
    public void testInvalidStartupScript() {
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(true);
        seatunnelParameters.setRawScript("env { execution.parallelism = 1 }");

        seatunnelParameters.setStartupScript("../../../etc/passwd");
        Assertions.assertFalse(seatunnelParameters.checkParameters());

        seatunnelParameters.setStartupScript("script.sh; rm -rf /");
        Assertions.assertFalse(seatunnelParameters.checkParameters());

        seatunnelParameters.setStartupScript("script");
        Assertions.assertFalse(seatunnelParameters.checkParameters());

        seatunnelParameters.setStartupScript(".sh");
        Assertions.assertFalse(seatunnelParameters.checkParameters());
    }
}
