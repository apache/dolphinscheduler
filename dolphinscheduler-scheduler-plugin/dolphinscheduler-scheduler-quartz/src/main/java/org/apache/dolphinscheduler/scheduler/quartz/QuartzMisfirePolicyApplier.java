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

import org.apache.dolphinscheduler.common.enums.MisfirePolicy;

import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

final class QuartzMisfirePolicyApplier {

    private QuartzMisfirePolicyApplier() {
        throw new IllegalStateException("Utility class");
    }

    static void apply(SimpleScheduleBuilder scheduleBuilder, MisfirePolicy misfirePolicy) {
        switch (effectivePolicy(misfirePolicy)) {
            case DO_NOTHING:
                scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
                return;
            case FIRE_AND_PROCEED:
                scheduleBuilder.withMisfireHandlingInstructionFireNow();
                return;
            case IGNORE_MISFIRES:
                scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                return;
            default:
                throw new IllegalStateException("Unsupported misfire policy: " + misfirePolicy);
        }
    }

    static void apply(CronScheduleBuilder scheduleBuilder, MisfirePolicy misfirePolicy) {
        switch (effectivePolicy(misfirePolicy)) {
            case DO_NOTHING:
                scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                return;
            case FIRE_AND_PROCEED:
                scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                return;
            case IGNORE_MISFIRES:
                scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                return;
            default:
                throw new IllegalStateException("Unsupported misfire policy: " + misfirePolicy);
        }
    }

    private static MisfirePolicy effectivePolicy(MisfirePolicy misfirePolicy) {
        return misfirePolicy == null ? MisfirePolicy.IGNORE_MISFIRES : misfirePolicy;
    }
}
