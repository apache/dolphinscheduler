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
import org.apache.dolphinscheduler.common.enums.CycleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * DAG Cycle judge
 */
public class CycleLinks extends AbstractCycle {
  private final List<AbstractCycle> cycleList = new ArrayList<>();

  public CycleLinks(Cron cron) {
    super(cron);
  }

  /**
   * add cycle
   * @param cycle cycle
   * @return CycleLinks
   */
  @Override
  public CycleLinks addCycle(AbstractCycle cycle) {
    cycleList.add(cycle);
    return this;
  }

  /**
   * get cycle
   * @return CycleEnum
   */
  @Override
  protected CycleEnum getCycle() {
    for (AbstractCycle abstractCycle : cycleList) {
      CycleEnum cycle = abstractCycle.getCycle();
      if (cycle != null) {
        return cycle;
      }
    }

    return null;
  }

  /**
   * get mini cycle
   * @return CycleEnum
   */
  @Override
  protected CycleEnum getMiniCycle() {
    for (AbstractCycle cycleHelper : cycleList) {
      CycleEnum cycle = cycleHelper.getMiniCycle();
      if (cycle != null) {
        return cycle;
      }
    }

    return null;
  }
}