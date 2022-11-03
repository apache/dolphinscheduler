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

import { reactive } from 'vue'
import { useDynamicLocales } from './use-dynamic-locales'
import { useFormField } from './use-form-field'

const data = {
  task: 'shell',
  locales: {
    zh_CN: {
      node_name: '节点名称',
      node_name_tips: '请输入节点名称',
      task_priority: '任务优先级',
      highest: '最高',
      high: '高',
      medium: '中',
      low: '低',
      lowest: '最低',
      worker_group: 'Worker 分组',
      script: '脚本'
    },
    en_US: {
      node_name: 'Node Name',
      node_name_tips: 'Please entry node name',
      task_priority: 'Task Priority',
      highest: 'Highest',
      high: 'High',
      medium: 'Medium',
      low: 'Low',
      lowest: 'Lowest',
      worker_group: 'Worker Group',
      script: 'Script'
    }
  },
  apis: [
    {
      name: 'getWorkerGroupList',
      uri: '/worker-groups/all',
      method: 'get'
    }
  ],
  forms: [
    {
      label: 'task_components.node_name',
      type: 'input',
      field: 'name',
      defaultValue: '',
      placeholder: 'task_components.node_name_tips'
    },
    {
      label: 'task_components.task_priority',
      type: 'select',
      field: 'taskPriority',
      options: [
        { label: 'task_components.highest', value: 'HIGHEST' },
        { label: 'task_components.high', value: 'HIGH' },
        { label: 'task_components.medium', value: 'MEDIUM' },
        { label: 'task_components.low', value: 'LOW' },
        { label: 'task_components.lowest', value: 'LOWEST' }
      ],
      defaultValue: 'MEDIUM'
    },
    {
      label: 'task_components.worker_group',
      type: 'select',
      field: 'workerGroup',
      options: [],
      defaultValue: 'default',
      api: 'getWorkerGroupList'
    },
    {
      label: 'task_components.script',
      type: 'studio',
      field: 'taskParams.rawScript',
      defaultValue: ''
    }
  ]
}

export function useTaskForm() {
  const variables = reactive({
    formStructure: {},
    model: {}
  })

  variables.formStructure = data
  variables.model = useFormField(data.forms)
  useDynamicLocales(data.locales)

  return {
    variables
  }
}
