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

package org.apache.dolphinscheduler.scheduler.quartz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.dolphinscheduler.common.enums.ScheduleMissedFirePolicy;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

class CronScheduleBuilderFactoryTest {

    private static final String CRON_EXPRESSION = "0 0 * * * ?";

    @Test
    void shouldCreateSkipMissedCronScheduleBuilder() {
        assertFactoryAndMisfireInstruction(
                ScheduleMissedFirePolicy.SKIP_MISSED,
                SkipMissedCronScheduleBuilderFactory.class,
                CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }

    @Test
    void shouldCreateFireOnceNowCronScheduleBuilder() {
        assertFactoryAndMisfireInstruction(
                ScheduleMissedFirePolicy.FIRE_ONCE_NOW,
                FireOnceNowCronScheduleBuilderFactory.class,
                CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
    }

    @Test
    void shouldCreateFireAllMissedCronScheduleBuilder() {
        assertFactoryAndMisfireInstruction(
                ScheduleMissedFirePolicy.FIRE_ALL_MISSED,
                FireAllMissedCronScheduleBuilderFactory.class,
                Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY);
    }

    @Test
    void shouldCreateFireAllMissedCronScheduleBuilderByDefault() {
        assertFactoryAndMisfireInstruction(
                null,
                FireAllMissedCronScheduleBuilderFactory.class,
                Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY);
    }

    private void assertFactoryAndMisfireInstruction(
                                                    ScheduleMissedFirePolicy policy,
                                                    Class<? extends CronScheduleBuilderFactory> expectedFactoryClass,
                                                    int expectedMisfireInstruction) {
        CronScheduleBuilderFactory factory = CronScheduleBuilderFactory.getFactory(policy);
        assertInstanceOf(expectedFactoryClass, factory);
        assertEquals(
                expectedMisfireInstruction,
                factory.createCronScheduleBuilder(CRON_EXPRESSION).build().getMisfireInstruction());
    }
}
