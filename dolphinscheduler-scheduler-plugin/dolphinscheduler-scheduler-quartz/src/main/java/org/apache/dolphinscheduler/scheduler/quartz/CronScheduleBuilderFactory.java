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

import org.apache.dolphinscheduler.common.enums.ScheduleMissedFirePolicy;

import org.quartz.CronScheduleBuilder;

interface CronScheduleBuilderFactory {

    CronScheduleBuilder createCronScheduleBuilder(String cronExpression);

    static CronScheduleBuilderFactory getFactory(ScheduleMissedFirePolicy missedFirePolicy) {
        ScheduleMissedFirePolicy effectivePolicy = missedFirePolicy == null
                ? ScheduleMissedFirePolicy.FIRE_ALL_MISSED
                : missedFirePolicy;
        switch (effectivePolicy) {
            case SKIP_MISSED:
                return new SkipMissedCronScheduleBuilderFactory();
            case FIRE_ONCE_NOW:
                return new FireOnceNowCronScheduleBuilderFactory();
            case FIRE_ALL_MISSED:
                return new FireAllMissedCronScheduleBuilderFactory();
            default:
                throw new IllegalArgumentException("Unsupported schedule missed fire policy: " + missedFirePolicy);
        }
    }
}
