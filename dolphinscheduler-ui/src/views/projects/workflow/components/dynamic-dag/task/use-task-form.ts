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
import type { TaskDynamic } from './types'

const data = [
  {
    task: 'shell',
    locales: {
      zh_CN: {
        node_name: '节点名称',
        node_name_tips: '节点名称不能为空'
      },
      en_US: {
        node_name: 'Node Name',
        node_name_tips: 'Node name cannot be empty'
      }
    },
    apis: [

    ],
    items: [
      {
        label: 'task_components.node_name',
        type: 'input',
        field: '',
        validate: {
          trigger: ['input', 'blur'],
          message: 'task_components.node_name_tips'
        }
      }
    ]
  }
]

export function useTaskForm() {
  const variables = reactive({
    formStructure: {}
  })

  variables.formStructure = data.map((t: TaskDynamic) => {
    useDynamicLocales(t.locales)
    return t
  })

  return {
    variables
  }
}
