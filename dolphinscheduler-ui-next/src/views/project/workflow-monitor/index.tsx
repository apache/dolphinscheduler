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

import { defineComponent, onMounted, ref } from 'vue'
import { NGrid, NGi } from 'naive-ui'
import { startOfToday, getTime } from 'date-fns'
import { useI18n } from 'vue-i18n'
import { useTaskState } from './use-task-state'
import { useProcessState } from './use-process-state'
import StateCard from './components/state-card'
import DefinitionCard from './components/definition-card'

const workflowMonitor = defineComponent({
  name: 'WorkflowMonitor',
  setup() {
    const { t } = useI18n()
    const dateRef = ref([getTime(startOfToday()), Date.now()])
    const { getTaskState } = useTaskState()
    const { getProcessState } = useProcessState()
    const taskStateRef = ref()
    const processStateRef = ref()

    onMounted(() => {
      taskStateRef.value = getTaskState(dateRef.value)
      processStateRef.value = getProcessState(dateRef.value)
    })

    const handleTaskDate = (val: any) => {
      taskStateRef.value = getTaskState(val)
    }

    const handleProcessDate = (val: any) => {
      processStateRef.value = getProcessState(val)
    }

    return {
      t,
      dateRef,
      handleTaskDate,
      handleProcessDate,
      taskStateRef,
      processStateRef
    }
  },
  render() {
    const { t, dateRef, handleTaskDate, handleProcessDate } = this

    return (
      <div>
        <NGrid x-gap={12} cols={2}>
          <NGi>
            <StateCard
              title={t('home.task_state_statistics')}
              date={dateRef}
              tableData={this.taskStateRef?.value.table}
              chartData={this.taskStateRef?.value.chart}
              onUpdateDatePickerValue={handleTaskDate}
            />
          </NGi>
          <NGi>
            <StateCard
              title={t('home.process_state_statistics')}
              date={dateRef}
              tableData={this.processStateRef?.value.table}
              chartData={this.processStateRef?.value.chart}
              onUpdateDatePickerValue={handleProcessDate}
            />
          </NGi>
        </NGrid>
        <NGrid cols={1} style='margin-top: 12px;'>
          <NGi>
            <DefinitionCard title={t('home.process_definition_statistics')} />
          </NGi>
        </NGrid>
      </div>
    )
  }
})

export default workflowMonitor
