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
package org.apache.dolphinscheduler.dao.utils.cron;

import org.apache.dolphinscheduler.common.enums.CycleEnum;
import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.*;

/**
 * Cycle
 */
public abstract class AbstractCycle {

  protected Cron cron;

  protected CronField minField;
  protected CronField hourField;
  protected CronField dayOfMonthField;
  protected CronField dayOfWeekField;
  protected CronField monthField;
  protected CronField yearField;

  public CycleLinks addCycle(AbstractCycle cycle) {
    return new CycleLinks(this.cron).addCycle(this).addCycle(cycle);
  }

  public AbstractCycle(Cron cron) {
    if (cron == null) {
      throw new IllegalArgumentException("cron must not be null!");
    }

    this.cron = cron;
    this.minField = cron.retrieve(CronFieldName.MINUTE);
    this.hourField = cron.retrieve(CronFieldName.HOUR);
    this.dayOfMonthField = cron.retrieve(CronFieldName.DAY_OF_MONTH);
    this.dayOfWeekField = cron.retrieve(CronFieldName.DAY_OF_WEEK);
    this.monthField = cron.retrieve(CronFieldName.MONTH);
    this.yearField = cron.retrieve(CronFieldName.YEAR);
  }

  /**
   * Whether the minute field has a value
   * @return
   */
  protected boolean minFiledIsSetAll(){
    FieldExpression minFieldExpression = minField.getExpression();
    return (minFieldExpression instanceof Every || minFieldExpression instanceof Always
            || minFieldExpression instanceof Between || minFieldExpression instanceof And
            || minFieldExpression instanceof On);
  }


  /**
   * Whether the minute field has a value of every or always
   * @return
   */
  protected boolean minFiledIsEvery(){
    FieldExpression minFieldExpression = minField.getExpression();
    return (minFieldExpression instanceof Every || minFieldExpression instanceof Always);
  }

  /**
   * Whether the hour field has a value
   * @return
   */
  protected boolean hourFiledIsSetAll(){
    FieldExpression hourFieldExpression = hourField.getExpression();
    return (hourFieldExpression instanceof Every || hourFieldExpression instanceof Always
            || hourFieldExpression instanceof Between || hourFieldExpression instanceof And
            || hourFieldExpression instanceof On);
  }

  /**
   * Whether the hour field has a value of every or always
   * @return
   */
  protected boolean hourFiledIsEvery(){
    FieldExpression hourFieldExpression = hourField.getExpression();
    return (hourFieldExpression instanceof Every || hourFieldExpression instanceof Always);
  }

  /**
   * Whether the day Of month field has a value
   * @return
   */
  protected boolean dayOfMonthFieldIsSetAll(){
    return (dayOfMonthField.getExpression() instanceof Every || dayOfMonthField.getExpression() instanceof Always
            || dayOfMonthField.getExpression() instanceof Between || dayOfMonthField.getExpression() instanceof And
            || dayOfMonthField.getExpression() instanceof On);
  }


  /**
   * Whether the day Of Month field has a value of every or always
   * @return
   */
  protected boolean dayOfMonthFieldIsEvery(){
    return (dayOfMonthField.getExpression() instanceof Every || dayOfMonthField.getExpression() instanceof Always);
  }

  /**
   * Whether month field has a value
   * @return
   */
  protected boolean monthFieldIsSetAll(){
    FieldExpression monthFieldExpression = monthField.getExpression();
    return (monthFieldExpression instanceof Every || monthFieldExpression instanceof Always
            || monthFieldExpression instanceof Between || monthFieldExpression instanceof And
            || monthFieldExpression instanceof On);
  }

  /**
   * Whether the month field has a value of every or always
   * @return
   */
  protected boolean monthFieldIsEvery(){
    FieldExpression monthFieldExpression = monthField.getExpression();
    return (monthFieldExpression instanceof Every || monthFieldExpression instanceof Always);
  }

  /**
   * Whether the day Of week field has a value
   * @return
   */
  protected boolean dayofWeekFieldIsSetAll(){
    FieldExpression dayOfWeekFieldExpression = dayOfWeekField.getExpression();
    return (dayOfWeekFieldExpression instanceof Every || dayOfWeekFieldExpression instanceof Always
            || dayOfWeekFieldExpression instanceof Between || dayOfWeekFieldExpression instanceof And
            || dayOfWeekFieldExpression instanceof On);
  }

  /**
   * Whether the day Of week field has a value of every or always
   * @return
   */
  protected boolean dayofWeekFieldIsEvery(){
    FieldExpression dayOfWeekFieldExpression = dayOfWeekField.getExpression();
    return (dayOfWeekFieldExpression instanceof Every || dayOfWeekFieldExpression instanceof Always);
  }
  /**
   * get cycle enum
   *
   * @return
   */
  protected abstract CycleEnum getCycle();

  /**
   * get mini level cycle enum
   *
   * @return
   */
  protected abstract CycleEnum getMiniCycle();
}
