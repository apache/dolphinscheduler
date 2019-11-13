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
package org.apache.dolphinscheduler.dao.cron;

import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.dao.utils.cron.CronUtils;
import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

/**
 */
public class CronUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

    /**
     * cron as string test
     */
    @Test
    public void testCronAsString() {
        Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(always())
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(always())
                .withHour(always())
                .withMinute(every(5))
                .withSecond(on(0))
                .instance();
        // Obtain the string expression
        String cronAsString = cron.asString(); // 0 */5 * * * ? *  Every five minutes(once every 5 minutes)

        Assert.assertEquals(cronAsString, "0 */5 * * * ? *");
    }


    /**
     * cron parse test
     * @throws ParseException if error throws ParseException
     */
    @Test
    public void testCronParse() throws ParseException {
        String strCrontab = "0 1 2 3 * ? *";
        strCrontab = "0/50 0/59 * * * ? *";
        strCrontab = "3/5 * 0/5 * * ? *";
        strCrontab = "1/5 3/5 1/5 3/30 * ? *";

        Cron depCron = CronUtils.parse2Cron(strCrontab);
        Assert.assertEquals(depCron.retrieve(CronFieldName.SECOND).getExpression().asString(), "0");
        Assert.assertEquals(depCron.retrieve(CronFieldName.MINUTE).getExpression().asString(), "1");
        Assert.assertEquals(depCron.retrieve(CronFieldName.HOUR).getExpression().asString(), "2");
        Assert.assertEquals(depCron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression().asString(), "3");
        Assert.assertEquals(depCron.retrieve(CronFieldName.MONTH).getExpression().asString(), "*");
        Assert.assertEquals(depCron.retrieve(CronFieldName.YEAR).getExpression().asString(), "*");
    }

    /**
     * schedule type test
     * @throws ParseException if error throws ParseException
     */
    @Test
    public void testScheduleType() throws ParseException {

        CycleEnum cycleEnum = CronUtils.getMaxCycle("0 */1 * * * ? *");
        Assert.assertEquals(cycleEnum.name(), "MINUTE");

        CycleEnum cycleEnum2 = CronUtils.getMaxCycle("0 * * * * ? *");
        Assert.assertEquals(cycleEnum2.name(), "MINUTE");
    }

    /**
     * test
     */
    @Test
    public void test2(){
        Cron cron1 = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(always())
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(always())
                .withHour(always())
                .withMinute(every(5))
                .withSecond(on(0))
                .instance();

        String cronAsString = cron1.asString(); // 0 */5 * * * ? * once every 5 minutes
        //logger.info(cronAsString);
        // Obtain the string expression
        //String minCrontab = "0 0 * * * ? *";
        //String minCrontab = "0 0 10,14,16 * * ?";
        //String minCrontab = "0 0-5 14 * * ? *";
        //String minCrontab = "0 0 2 ? * SUN *";
        //String minCrontab = "* 0,3 2 SUN * 1#1 *";
        //String minCrontab = "* 0,3 * 1W * ? *";
        //cron = CronUtils.parse2Cron("0 * * * * ? *");
        // month cycle
        /*String[] cronArayy = new String[]{"* 0,3 * 1W * ? *","* 0 0 1W * ? *",
                "0 0 0 L 3/5 ? *","0 0 0 ? 3/5 2/2 *"};*/
        // minute cycle
        String[] cronArayy = new String[]{"* * * * * ? *","* 0 * * * ? *",
                "* 5 * * 3/5 ? *","0 0 * * * ? *"};
        // week cycle
        /*String[] cronArayy = new String[]{"* * * ? * 2/1 *","0 *//*5 * ? * 2/1 *",
                "* * *//*5 ? * 2/1 *"};*/
        for(String minCrontab:cronArayy){
            if (!org.quartz.CronExpression.isValidExpression(minCrontab)) {
                throw new RuntimeException(minCrontab+" verify failure, cron expression not valid");
            }
            Cron cron = CronUtils.parse2Cron(minCrontab);
            CronField minField = cron.retrieve(CronFieldName.MINUTE);
            logger.info("minField instanceof Between:"+(minField.getExpression() instanceof Between));
            logger.info("minField instanceof Every:"+(minField.getExpression() instanceof Every));
            logger.info("minField instanceof Always:" + (minField.getExpression() instanceof Always));
            logger.info("minField instanceof On:"+(minField.getExpression() instanceof On));
            logger.info("minField instanceof And:"+(minField.getExpression() instanceof And));
            CronField hourField = cron.retrieve(CronFieldName.HOUR);
            logger.info("hourField instanceof Between:"+(hourField.getExpression() instanceof Between));
            logger.info("hourField instanceof Always:"+(hourField.getExpression() instanceof Always));
            logger.info("hourField instanceof Every:"+(hourField.getExpression() instanceof Every));
            logger.info("hourField instanceof On:"+(hourField.getExpression() instanceof On));
            logger.info("hourField instanceof And:"+(hourField.getExpression() instanceof And));

            CronField dayOfMonthField = cron.retrieve(CronFieldName.DAY_OF_MONTH);
            logger.info("dayOfMonthField instanceof Between:"+(dayOfMonthField.getExpression() instanceof Between));
            logger.info("dayOfMonthField instanceof Always:"+(dayOfMonthField.getExpression() instanceof Always));
            logger.info("dayOfMonthField instanceof Every:"+(dayOfMonthField.getExpression() instanceof Every));
            logger.info("dayOfMonthField instanceof On:"+(dayOfMonthField.getExpression() instanceof On));
            logger.info("dayOfMonthField instanceof And:"+(dayOfMonthField.getExpression() instanceof And));
            logger.info("dayOfMonthField instanceof QuestionMark:"+(dayOfMonthField.getExpression() instanceof QuestionMark));

            CronField monthField = cron.retrieve(CronFieldName.MONTH);
            logger.info("monthField instanceof Between:"+(monthField.getExpression() instanceof Between));
            logger.info("monthField instanceof Always:"+(monthField.getExpression() instanceof Always));
            logger.info("monthField instanceof Every:"+(monthField.getExpression() instanceof Every));
            logger.info("monthField instanceof On:"+(monthField.getExpression() instanceof On));
            logger.info("monthField instanceof And:"+(monthField.getExpression() instanceof And));
            logger.info("monthField instanceof QuestionMark:"+(monthField.getExpression() instanceof QuestionMark));

            CronField dayOfWeekField = cron.retrieve(CronFieldName.DAY_OF_WEEK);
            logger.info("dayOfWeekField instanceof Between:"+(dayOfWeekField.getExpression() instanceof Between));
            logger.info("dayOfWeekField instanceof Always:"+(dayOfWeekField.getExpression() instanceof Always));
            logger.info("dayOfWeekField instanceof Every:"+(dayOfWeekField.getExpression() instanceof Every));
            logger.info("dayOfWeekField instanceof On:"+(dayOfWeekField.getExpression() instanceof On));
            logger.info("dayOfWeekField instanceof And:"+(dayOfWeekField.getExpression() instanceof And));
            logger.info("dayOfWeekField instanceof QuestionMark:"+(dayOfWeekField.getExpression() instanceof QuestionMark));


            CycleEnum cycleEnum = CronUtils.getMaxCycle(minCrontab);
            if(cycleEnum !=null){
                logger.info(cycleEnum.name());
            }else{
                logger.info("can't get scheduleType");
            }
        }
    }
}
