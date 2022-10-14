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
package org.apache.dolphinscheduler.service.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.dolphinscheduler.common.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MasterLogFilterTest {

    @Test
    public void decide() {
        MasterLogFilter masterLogFilter = new MasterLogFilter();

        FilterReply filterReply = masterLogFilter.decide(new LoggingEvent() {

            @Override
            public String getThreadName() {
                return Constants.THREAD_NAME_MASTER_SERVER;
            }

            @Override
            public Level getLevel() {
                return Level.INFO;
            }

            @Override
            public String getMessage() {
                return "master insert into queue success, task : shell2";
            }

        });

        Assertions.assertEquals(FilterReply.ACCEPT, filterReply);

    }
}
