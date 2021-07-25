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
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;
import org.apache.dolphinscheduler.common.enums.CycleEnum;

/**
 * Crontab Cycle Tool Factory
 */
public class CycleFactory {
    private CycleFactory() {
        throw new IllegalStateException("CycleFactory class");
    }
    /**
     * min
     * @param cron cron
     * @return AbstractCycle
     */
    public static AbstractCycle min(Cron cron) {
      return new MinCycle(cron);
    }

    /**
     * hour
     * @param cron cron
     * @return AbstractCycle
     */
    public static AbstractCycle hour(Cron cron) {
      return new HourCycle(cron);
    }

    /**
     * day
     * @param cron cron
     * @return AbstractCycle
     */
    public static AbstractCycle day(Cron cron) {
      return new DayCycle(cron);
    }

    /**
     * week
     * @param cron cron
     * @return AbstractCycle
     */
    public static AbstractCycle week(Cron cron) {
      return new WeekCycle(cron);
    }

    /**
     * month
     * @param cron cron
     * @return AbstractCycle
     */
    public static AbstractCycle month(Cron cron) {
      return new MonthCycle(cron);
    }

  /**
   * day cycle
   */
  public static class DayCycle extends AbstractCycle {

    public DayCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {

          if (minFiledIsSetAll()
              && hourFiledIsSetAll()
              && dayOfMonthFieldIsEvery()
              && dayOfWeekField.getExpression() instanceof QuestionMark
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.DAY;
          }

          return null;
        }

      /**
       * get min cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (dayOfMonthFieldIsEvery()) {
            return CycleEnum.DAY;
          }

          return null;
        }
  }

  /**
   * hour cycle
   */
  public static class HourCycle extends AbstractCycle {

    public HourCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          if (minFiledIsSetAll()
              && hourFiledIsEvery()
              && dayOfMonthField.getExpression() instanceof Always
              && dayOfWeekField.getExpression() instanceof QuestionMark
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.HOUR;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if(hourFiledIsEvery()){
            return CycleEnum.HOUR;
          }
          return null;
        }
  }

  /**
   * minute cycle
   */
  public static class MinCycle extends AbstractCycle {

      public MinCycle(Cron cron) {
          super(cron);
      }

      /**
       * get cycle
       * @return CycleEnum
       */
      @Override
      protected CycleEnum getCycle() {
          if (minFiledIsEvery()
                  && hourField.getExpression() instanceof Always
                  && dayOfMonthField.getExpression() instanceof Always
                  && monthField.getExpression() instanceof Always) {
              return CycleEnum.MINUTE;
          }

          return null;
      }

      /**
       * get min cycle
       * @return CycleEnum
       */
      @Override
      protected CycleEnum getMiniCycle() {
          if(minFiledIsEvery()){
              return CycleEnum.MINUTE;
          }
          return null;
      }
  }

  /**
   * month cycle
   */
  public static class MonthCycle extends AbstractCycle {

    public MonthCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          boolean flag = (minFiledIsSetAll()
                  && hourFiledIsSetAll()
                  && dayOfMonthFieldIsSetAll()
                  && dayOfWeekField.getExpression() instanceof QuestionMark
                  && monthFieldIsEvery()) ||
                  (minFiledIsSetAll()
                          && hourFiledIsSetAll()
                          && dayOfMonthField.getExpression() instanceof QuestionMark
                          && dayofWeekFieldIsSetAll()
                          && monthFieldIsEvery());
          if (flag) {
            return CycleEnum.MONTH;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (monthFieldIsEvery()) {
            return CycleEnum.MONTH;
          }

          return null;
        }
  }

  /**
   * week cycle
   */
  public static class WeekCycle extends AbstractCycle {
    public WeekCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          if (minFiledIsSetAll()
              && hourFiledIsSetAll()
              && dayOfMonthField.getExpression() instanceof QuestionMark
              && dayofWeekFieldIsEvery()
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.WEEK;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (dayofWeekFieldIsEvery()) {
            return CycleEnum.WEEK;
          }

          return null;
        }
  }
}
