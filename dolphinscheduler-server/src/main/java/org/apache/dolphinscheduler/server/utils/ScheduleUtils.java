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
package org.apache.dolphinscheduler.server.utils;

import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * ScheduleUtils
 */
public class ScheduleUtils {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleUtils.class);

    /**
     * Get the execution time of the time interval
     * @param cron
     * @param size
     * @param from
     * @param to
     * @return
     */
    public static List<Date> getRecentTriggerTime(String cron, int size, Date from, Date to) {
        List list = new LinkedList<Date>();
        if(to.before(from)){
            logger.error("from:{} to:{} error!", from, to);
            return list;
        }
        try {
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(cron);
            trigger.setStartTime(from);
            if(null != to){
                trigger.setEndTime(to);
            }
            trigger.computeFirstFireTime(null);
            for (int i = 0; i < size; i++) {
                Date d = trigger.getNextFireTime();
                if (d != null) {
                    if (null != to && d.after(to)) {
                        break;
                    }
                    list.add(d);
                    trigger.triggered(null);
                } else {
                    break;
                }
            }
        } catch (ParseException e) {
            logger.error("cron:{} error:{}", cron, e.getMessage());
        }
        return java.util.Collections.unmodifiableList(list);
    }

    /**
     * Get the execution time of the time interval
     * @param cron
     * @param from
     * @param to
     * @return
     */
    public static List<Date> getRecentTriggerTime(String cron, Date from, Date to) {
        List list = new LinkedList<Date>();
        if(to.before(from)){
            logger.error("from:{} to:{} error!", from, to);
            return list;
        }
        try {
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(cron);
            trigger.setStartTime(from);
            if(null != to){
                trigger.setEndTime(to);
            }
            trigger.computeFirstFireTime(null);
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                Date d = trigger.getNextFireTime();
                if (d != null) {
                    if (null != to && d.after(to)) {
                        break;
                    }
                    list.add(d);
                    trigger.triggered(null);
                } else {
                    break;
                }
            }
        } catch (ParseException e) {
            logger.error("cron:{} error:{}", cron, e.getMessage());
        }
        return java.util.Collections.unmodifiableList(list);
    }
}
