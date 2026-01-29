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

import { defineComponent, onMounted, ref, toRefs, watch } from 'vue'
import { NGrid, NGi } from 'naive-ui'
import { startOfToday, getTime } from 'date-fns'
import { useI18n } from 'vue-i18n'
import { useTaskState } from './use-task-state'
import { useWorkflowState } from './use-workflow-state'
import StateCard from './components/state-card'
import DefinitionCard from './components/definition-card'

export default defineComponent({
  name: 'home',
  setup() {
    const { t, locale } = useI18n()
    const dateRef = ref([getTime(startOfToday()), Date.now()])
    const taskStateRef = ref()
    const workflowStateRef = ref()
    const { getTaskState, taskVariables } = useTaskState()
    const { getWorkflowState, workflowVariables: workflowVariables } =
      useWorkflowState()

    const initData = () => {
      taskStateRef.value = getTaskState(dateRef.value) || taskStateRef.value
      workflowStateRef.value =
        getWorkflowState(dateRef.value) || workflowStateRef.value
    }

    const handleTaskDate = (val: any) => {
      taskStateRef.value = getTaskState(val)
    }

    const handleWorkflowDate = (val: any) => {
      workflowStateRef.value = getWorkflowState(val)
    }

    onMounted(() => {
      initData()
    })

    watch(
      () => locale.value,
      () => initData()
    )

    return {
      t,
      dateRef,
      handleTaskDate,
      handleWorkflowDate: handleWorkflowDate,
      taskStateRef,
      workflowStateRef: workflowStateRef,
      ...toRefs(taskVariables),
      ...toRefs(workflowVariables)
    }
  },
  render() {
    const {
      t,
      dateRef,
      handleTaskDate,
      handleWorkflowDate,
      taskLoadingRef,
      workflowLoadingRef
    } = this

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
              loadingRef={taskLoadingRef}
            />
          </NGi>
          <NGi>
            <StateCard
              title={t('home.workflow_state_statistics')}
              date={dateRef}
              tableData={this.workflowStateRef?.value.table}
              chartData={this.workflowStateRef?.value.chart}
              onUpdateDatePickerValue={handleWorkflowDate}
              loadingRef={workflowLoadingRef}
            />
          </NGi>
        </NGrid>
        <NGrid cols={1} style='margin-top: 12px;'>
          <NGi>
            <DefinitionCard title={t('home.workflow_definition_statistics')} />
          </NGi>
        </NGrid>
      </div>
    )
  }
})
