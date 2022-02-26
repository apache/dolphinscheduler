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

import { ref, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData, ITaskData } from '../types'

export function useConditions({
  projectCode,
  from = 0,
  readonly,
  data
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
}) {
  const { t } = useI18n()
  const taskCodeOptions = ref([] as { label: string; value: number }[])
  const model = reactive({
    taskType: 'CONDITIONS',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    delayTime: 0,
    timeout: 30,
    relation: 'AND',
    dependTaskList: [],
    preTasks: []
  } as INodeData)

  let extra: IJsonItem[] = []
  if (from === 1) {
    extra = [
      Fields.useTaskType(model, readonly),
      Fields.useProcessName({
        model,
        projectCode,
        isCreate: !data?.id,
        from,
        processName: data?.processName,
        code: data?.code
      })
    ]
  }

  watch(
    () => model.preTasks,
    () => {
      taskCodeOptions.value =
        model.preTaskOptions
          ?.filter((task: { code: number }) =>
            model.preTasks?.includes(task.code)
          )
          .map((task: { code: number; name: string }) => ({
            value: task.code,
            label: task.name
          })) || []
    }
  )

  return {
    json: [
      Fields.useName(),
      ...extra,
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(),
      Fields.useEnvironmentName(model, !data?.id),
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useFailed(),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useRelationCustomParams({
        model,
        children: {
          type: 'custom-parameters',
          field: 'dependItemList',
          span: 18,
          children: [
            {
              type: 'select',
              field: 'depTaskCode',
              span: 10,
              options: taskCodeOptions
            },
            {
              type: 'select',
              field: 'status',
              span: 10,
              options: [
                {
                  value: 'SUCCESS',
                  label: t('project.node.success')
                },
                {
                  value: 'FAILURE',
                  label: t('project.node.failed')
                }
              ]
            }
          ]
        },
        childrenField: 'dependItemList'
      }),
      Fields.usePreTasks(model)
    ] as IJsonItem[],
    model
  }
}
