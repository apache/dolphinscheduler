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

import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormRules } from 'naive-ui'

export const useForm = () => {
  const { t } = useI18n()
  const date = new Date()
  const year = date.getFullYear()
  const month = date.getMonth()
  const day = date.getDate()

  const importState = reactive({
    importFormRef: ref(),
    importForm: {
      name: '',
      file: ''
    },
    saving: false,
    importRules: {
      file: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (importState.importForm.name === '') {
            return new Error(t('project.workflow.enter_name_tips'))
          }
        }
      }
    } as FormRules
  })

  const startState = reactive({
    startFormRef: ref(),
    startForm: {
      processDefinitionCode: -1,
      startEndTime: [new Date(year, month, day), new Date(year, month, day)],
      scheduleTime: '',
      dataDateType: 1,
      failureStrategy: 'CONTINUE',
      warningType: 'NONE',
      warningGroupId: null,
      execType: 'START_PROCESS',
      startNodeList: '',
      taskDependType: 'TASK_POST',
      complementDependentMode: 'OFF_MODE',
      runMode: 'RUN_MODE_SERIAL',
      processInstancePriority: 'MEDIUM',
      workerGroup: 'default',
      environmentCode: null,
      startParams: null,
      expectedParallelismNumber: '',
      dryRun: 0,
      testFlag: 0
    },
    saving: false,
    rules: {
      scheduleTime: {
        trigger: ['input', 'blur'],
        validator(unuse: any, value: string) {
          if (!value) return
          if (
            !/(((19|20)[0-9]{2})-((0[1-9])|(1[0-2]))-((0[1-9])|((1|2)[0-9])|(3[0-1]))([ ])([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))(,(((19|20)[0-9]{2})-((0[1-9])|(1[0-2]))-((0[1-9])|((1|2)[0-9])|(3[0-1]))([ ])([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])))*$/.test(
              value
            )
          ) {
            return new Error(t('project.workflow.schedule_date_tips'))
          }
          const dates = value.split(',')
          if (dates.length > 100) {
            return new Error(t('project.workflow.schedule_date_limit'))
          }
        }
      }
    }
  })

  const timingState = reactive({
    timingFormRef: ref(),
    timingForm: {
      startEndTime: [
        new Date(year, month, day),
        new Date(year + 100, month, day)
      ],
      crontab: '0 0 * * * ? *',
      timezoneId: Intl.DateTimeFormat().resolvedOptions().timeZone,
      failureStrategy: 'CONTINUE',
      warningType: 'NONE',
      processInstancePriority: 'MEDIUM',
      warningGroupId: null as null | number,
      workerGroup: 'default',
      environmentCode: null as null | string
    },
    saving: false
  })

  const copyState = reactive({
    copyFormRef: ref(),
    copyForm: {
      projectCode: null
    },
    saving: false,
    copyRules: {
      projectCode: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (copyState.copyForm.projectCode === '') {
            return new Error(t('project.workflow.project_name_required'))
          }
        }
      }
    } as FormRules
  })

  return {
    importState,
    startState,
    timingState,
    copyState
  }
}
