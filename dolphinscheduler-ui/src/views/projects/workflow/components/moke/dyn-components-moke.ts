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

export const componentList = [
  {
    taskName: 'SHELL',
    isDyn: true,
    hover: 'shell_hover.png', // hover icon
    icon: 'shell.png', // default icon
    type: 'SHELL',
    taskType: 'SHELL',
    jsonData: {
      task: 'shell',
      useCommonForm: 'true',
      locales: {
        zh_CN: {
          node_name: '节点名称 dyn',
          highest: 'HIGHEST',
          delay_time: '延时执行时间 dyn',
          node_name_validate_message: '请选择节点id',
          task_name: '任务名称'
        },
        en_US: {
          node_name: 'Node Name dyn',
          highest: 'HIGHEST',
          node_name_validate_message: 'please select taskId',
          node_name_tips: 'Please entry node name'
        }
      },
      apis: {},
      forms: [
        {
          type: 'common'
        },
        {
          type: 'taskGroup'
        },
        {
          type: 'failed'
        },
        {
          type: 'input-number',
          field: 'delayTime',
          label: 'task_components.delay_time',
          props: {
            suffix: 'project.node.minute'
          },
          span: 12,
          defaultValue: 0
        },
        {
          type: 'shell'
        },
        { type: 'preTask' }
      ],
      model: {
        taskType: 'SHELL',
        name: '',
        flag: 'YES',
        description: '',
        timeoutFlag: false,
        timeoutNotifyStrategy: ['WARN'],
        timeout: 30,
        localParams: [],
        dataSourceList: [],
        environmentCode: null,
        failRetryInterval: 1,
        failRetryTimes: 0,
        workerGroup: null,
        delayTime: 0,
        rawScript: '',
        code: -1
      },
      taskConfig: {
        language: 'shell',
        paneType: 'setting',
        handlers: [
          {
            key: 'script',
            name: 'project.node.script'
          }
        ]
      }
    },
    taskCategory: 'Dynamic'
  },
  {
    taskName: 'SUB_PROCESS',
    hover: 'sub_process_hover.png', // hover icon
    icon: 'sub_process.png', // default icon
    type: 'SUB_PROCESS',
    taskType: 'SUB_PROCESS',
    isDyn: true,
    jsonData: {
      task: 'shell',
      useCommonForm: 'true',
      locales: {
        zh_CN: {
          highest: 'HIGHEST',
          select_name2: '任务名称 dyn',
          node_name_validate_message: '请选择节点id',
          task_name: '任务名称'
        },
        en_US: {
          highest: 'HIGHEST',
          node_name_validate_message: 'please select taskId',
          node_name_tips: 'Please entry node name'
        }
      },
      apis: {},
      forms: [
        {
          type: 'common'
        },
        {
          type: 'childNode'
        },
        {
          type: 'preTask'
        }
      ],
      model: {
        taskType: 'SUB_PROCESS',
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
        timeoutNotifyStrategy: ['WARN']
      }
    },
    taskCategory: 'Dynamic'
  }
]
