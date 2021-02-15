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
package org.apache.dolphinscheduler.service.quartz.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.*;
import org.apache.dolphinscheduler.common.enums.CycleEnum;

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

  /**
   * cycle constructor
   * @param cron cron
   */
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
   * whether the minute field has a value
   * @return if minute field has a value return true，else return false
   */
  protected boolean minFiledIsSetAll(){
    FieldExpression minFieldExpression = minField.getExpression();
    return (minFieldExpression instanceof Every || minFieldExpression instanceof Always
            || minFieldExpression instanceof Between || minFieldExpression instanceof And
            || minFieldExpression instanceof On);
  }


  /**
   * whether the minute field has a value of every or always
   * @return if minute field has a value of every or always return true，else return false
   */
  protected boolean minFiledIsEvery(){
    FieldExpression minFieldExpression = minField.getExpression();
    return (minFieldExpression instanceof Every || minFieldExpression instanceof Always);
  }

  /**
   * whether the hour field has a value
   * @return if hour field has a value return true，else return false
   */
  protected boolean hourFiledIsSetAll(){
    FieldExpression hourFieldExpression = hourField.getExpression();
    return (hourFieldExpression instanceof Every || hourFieldExpression instanceof Always
            || hourFieldExpression instanceof Between || hourFieldExpression instanceof And
            || hourFieldExpression instanceof On);
  }

  /**
   * whether the hour field has a value of every or always
   * @return if hour field has a value of every or always return true，else return false
   */
  protected boolean hourFiledIsEvery(){
    FieldExpression hourFieldExpression = hourField.getExpression();
    return (hourFieldExpression instanceof Every || hourFieldExpression instanceof Always);
  }

  /**
   * whether the day Of month field has a value
   * @return if day Of month field has a value return true，else return false
   */
  protected boolean dayOfMonthFieldIsSetAll(){
    return (dayOfMonthField.getExpression() instanceof Every || dayOfMonthField.getExpression() instanceof Always
            || dayOfMonthField.getExpression() instanceof Between || dayOfMonthField.getExpression() instanceof And
            || dayOfMonthField.getExpression() instanceof On);
  }


  /**
   * whether the day Of Month field has a value of every or always
   * @return if day Of Month field has a value of every or always return true，else return false
   */
  protected boolean dayOfMonthFieldIsEvery(){
    return (dayOfMonthField.getExpression() instanceof Every || dayOfMonthField.getExpression() instanceof Always);
  }

  /**
   * whether month field has a value
   * @return if month field has a value return true，else return false
   */
  protected boolean monthFieldIsSetAll(){
    FieldExpression monthFieldExpression = monthField.getExpression();
    return (monthFieldExpression instanceof Every || monthFieldExpression instanceof Always
            || monthFieldExpression instanceof Between || monthFieldExpression instanceof And
            || monthFieldExpression instanceof On);
  }

  /**
   * whether the month field has a value of every or always
   * @return if  month field has a value of every or always return true，else return false
   */
  protected boolean monthFieldIsEvery(){
    FieldExpression monthFieldExpression = monthField.getExpression();
    return (monthFieldExpression instanceof Every || monthFieldExpression instanceof Always);
  }

  /**
   * whether the day Of week field has a value
   * @return if day Of week field has a value return true，else return false
   */
  protected boolean dayofWeekFieldIsSetAll(){
    FieldExpression dayOfWeekFieldExpression = dayOfWeekField.getExpression();
    return (dayOfWeekFieldExpression instanceof Every || dayOfWeekFieldExpression instanceof Always
            || dayOfWeekFieldExpression instanceof Between || dayOfWeekFieldExpression instanceof And
            || dayOfWeekFieldExpression instanceof On);
  }

  /**
   * whether the day Of week field has a value of every or always
   * @return if day Of week field has a value of every or always return true，else return false
   */
  protected boolean dayofWeekFieldIsEvery(){
    FieldExpression dayOfWeekFieldExpression = dayOfWeekField.getExpression();
    return (dayOfWeekFieldExpression instanceof Every || dayOfWeekFieldExpression instanceof Always);
  }

  /**
   * get cycle enum
   * @return CycleEnum
   */
  protected abstract CycleEnum getCycle();

  /**
   * get mini level cycle enum
   * @return CycleEnum
   */
  protected abstract CycleEnum getMiniCycle();
}
