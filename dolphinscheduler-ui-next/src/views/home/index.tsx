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

import { defineComponent } from 'vue'
import { NGrid, NGi, NDataTable } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import { useProcessDefinition } from './use-process-definition'
import Card from '@/components/card'
import PieChart from '@/components/chart/modules/Pie'
import BarChart from '@/components/chart/modules/Bar'
import styles from './index.module.scss'
import type { ProcessDefinitionRes } from '@/service/modules/projects-analysis/types'

export default defineComponent({
  name: 'home',
  setup() {
    const { t } = useI18n()
    const { getProcessDefinition, formatProcessDefinition } =
      useProcessDefinition()
    const processDefinition = getProcessDefinition()

    return { t, processDefinition, formatProcessDefinition }
  },
  render() {
    const { columnsRef } = useTable()
    const { t, processDefinition, formatProcessDefinition } = this
    const chartData =
      Object.keys(processDefinition).length > 0 &&
      formatProcessDefinition(processDefinition as ProcessDefinitionRes)

    return (
      <div>
        <NGrid x-gap={12} cols={2}>
          <NGi>
            <Card title={t('home.task_state_statistics')}>
              {{
                default: () => (
                  <div class={styles['card-table']}>
                    <PieChart />
                    <NDataTable columns={columnsRef} />
                  </div>
                ),
              }}
            </Card>
          </NGi>
          <NGi class={styles['card-table']}>
            <Card title={t('home.process_state_statistics')}>
              {{
                default: () => (
                  <div class={styles['card-table']}>
                    <PieChart />
                    <NDataTable columns={columnsRef} />
                  </div>
                ),
              }}
            </Card>
          </NGi>
        </NGrid>
        <NGrid cols={1} style='margin-top: 12px;'>
          <NGi>
            <Card title={t('home.process_definition_statistics')}>
              {{
                default: () =>
                  chartData && (
                    <BarChart
                      xAxisData={chartData.xAxisData}
                      seriesData={chartData.seriesData}
                    />
                  ),
              }}
            </Card>
          </NGi>
        </NGrid>
      </div>
    )
  },
})
