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
      scheduleTime: null,
      failureStrategy: 'CONTINUE',
      warningType: 'NONE',
      warningGroupId: null,
      execType: '',
      startNodeList: '',
      taskDependType: 'TASK_POST',
      runMode: 'RUN_MODE_SERIAL',
      processInstancePriority: 'MEDIUM',
      workerGroup: 'default',
      environmentCode: null,
      startParams: null,
      expectedParallelismNumber: '',
      dryRun: 0
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
      warningGroupId: '',
      workerGroup: 'default',
      environmentCode: null
    }
  })
  return {
    importState,
    startState,
    timingState
  }
}
