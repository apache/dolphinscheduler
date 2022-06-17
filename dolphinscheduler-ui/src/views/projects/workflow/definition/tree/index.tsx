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

import Card from '@/components/card'
import { ArrowLeftOutlined } from '@vicons/antd'
import { NButton, NFormItem, NIcon, NSelect, NSpace, NImage } from 'naive-ui'
import { defineComponent, onMounted, Ref, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import UseTree from '@/views/projects/workflow/definition/tree/use-tree'
import { IChartDataItem } from '@/components/chart/modules/types'
import { Router, useRouter } from 'vue-router'
import { viewTree } from '@/service/modules/process-definition'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'
import { find } from 'lodash'
import { tasksState } from '@/common/common'
import type { ITaskTypeNodeOption } from './types'

export default defineComponent({
  name: 'WorkflowDefinitionTiming',
  setup() {
    const router: Router = useRouter()
    const { t, locale } = useI18n()
    const options: Ref<Array<SelectMixedOption>> = ref([
      { label: '25', value: 25 },
      { label: '50', value: 50 },
      { label: '75', value: 75 },
      { label: '100', value: 100 }
    ])

    const projectCode = ref(
      Number(router.currentRoute.value.params.projectCode)
    )
    const definitionCode = ref(
      Number(router.currentRoute.value.params.definitionCode)
    )

    const chartData: Ref<Array<IChartDataItem>> = ref([] as IChartDataItem[])
    const taskStateMap = ref()

    const taskTypeNodeOptions: Ref<Array<ITaskTypeNodeOption>> = ref([
      {
        taskType: 'SHELL',
        color: '#646464',
        image: `${import.meta.env.BASE_URL}images/task-icons/shell.png`
      },
      {
        taskType: 'SUB_PROCESS',
        color: '#4295DA',
        image: `${import.meta.env.BASE_URL}images/task-icons/sub_process.png`
      },
      {
        taskType: 'PROCEDURE',
        color: '#545CC6',
        image: `${import.meta.env.BASE_URL}images/task-icons/procedure.png`
      },
      {
        taskType: 'SQL',
        color: '#8097A0',
        image: `${import.meta.env.BASE_URL}images/task-icons/sql.png`
      },
      {
        taskType: 'SPARK',
        color: '#a16435',
        image: `${import.meta.env.BASE_URL}images/task-icons/spark.png`
      },
      {
        taskType: 'FLINK',
        color: '#d68f5b',
        image: `${import.meta.env.BASE_URL}images/task-icons/flink.png`
      },
      {
        taskType: 'MR',
        color: '#A1A5C9',
        image: `${import.meta.env.BASE_URL}images/task-icons/mr.png`
      },
      {
        taskType: 'PYTHON',
        color: '#60BCD5',
        image: `${import.meta.env.BASE_URL}images/task-icons/python.png`
      },
      {
        taskType: 'DEPENDENT',
        color: '#60BCD5',
        image: `${import.meta.env.BASE_URL}images/task-icons/dependent.png`
      },
      {
        taskType: 'HTTP',
        color: '#7f3903',
        image: `${import.meta.env.BASE_URL}images/task-icons/http.png`
      },
      {
        taskType: 'DATAX',
        color: '#75CC71',
        image: `${import.meta.env.BASE_URL}images/task-icons/datax.png`
      },
      {
        taskType: 'PIGEON',
        color: '#5EC459',
        image: `${import.meta.env.BASE_URL}images/task-icons/pigeon.png`
      },
      {
        taskType: 'SQOOP',
        color: '#f98b3d',
        image: `${import.meta.env.BASE_URL}images/task-icons/sqoop.png`
      },
      {
        taskType: 'CONDITIONS',
        color: '#b99376',
        image: `${import.meta.env.BASE_URL}images/task-icons/conditions.png`
      },
      {
        taskType: 'SWITCH',
        color: '#ff6f00',
        image: `${import.meta.env.BASE_URL}images/task-icons/switch.png`
      },
      {
        taskType: 'SEATUNNEL',
        color: '#8c8c8f',
        image: `${import.meta.env.BASE_URL}images/task-icons/seatunnel.png`
      },
      { taskType: 'DAG', color: '#bbdde9' }
    ])

    const initTaskStateMap = () => {
      taskStateMap.value = Object.entries(tasksState(t)).map(([key, item]) => ({
        state: key,
        value: item.desc,
        color: item.color
      }))
    }

    const initChartData = (node: any, newNode: any) => {
      newNode.children = []
      node?.children.map((child: any) => {
        const newChild = {}
        initChartData(child, newChild)
        newNode.children.push(newChild)
      })

      newNode.name = node.name
      newNode.value = node.name === 'DAG' ? 'DAG' : node?.type
      const taskTypeNodeOption = find(taskTypeNodeOptions.value, {
        taskType: newNode.value
      })
      if (taskTypeNodeOption) {
        newNode.itemStyle = { color: taskTypeNodeOption.color }
        if (newNode.name !== 'DAG') {
          let taskState = null
          if (
            node.instances &&
            node.instances.length > 0 &&
            node.instances[0].state
          ) {
            taskState = find(taskStateMap.value, {
              state: node.instances[0].state
            })
          }
          newNode.label = {
            show: true,
            formatter: [
              `{name|${t('project.task.task_name')}:${newNode.name}}`,
              `{type|${t('project.task.task_type')}:${
                taskTypeNodeOption.taskType
              }}`,
              taskState
                ? `{state|${t('project.workflow.task_state')}: ${
                    taskState.value
                  }}`
                : ''
            ].join('\n'),
            rich: {
              type: {
                lineHeight: 20,
                align: 'left'
              },
              name: {
                lineHeight: 20,
                align: 'left'
              },
              state: {
                lineHeight: 20,
                align: 'left',
                color: taskState ? taskState.color : 'black'
              }
            }
          }
        }
      }
    }

    const getWorkflowTreeData = async (limit: number) => {
      if (projectCode.value && definitionCode) {
        const res = await viewTree(projectCode.value, definitionCode.value, {
          limit: limit
        })
        chartData.value = [{ name: 'DAG', value: 'DAG' }]
        initChartData(res, chartData.value[0])
      }
    }

    const onSelectChange = (value: number) => {
      if (value) {
        getWorkflowTreeData(value)
      }
    }

    const initData = () => {
      initTaskStateMap()
      getWorkflowTreeData(25)
    }

    onMounted(() => {
      initData()
    })

    watch(
      () => locale.value,
      () => {
        initData()
      }
    )

    return {
      chartData,
      options,
      onSelectChange,
      taskTypeNodeOptions
    }
  },
  render() {
    const { chartData, options, onSelectChange, taskTypeNodeOptions } = this
    const { t } = useI18n()
    const router: Router = useRouter()

    return (
      <div class={styles.content}>
        <Card class={styles.card}>
          <div class={styles.header}>
            <NButton type='primary' onClick={() => router.go(-1)}>
              <NIcon>
                <ArrowLeftOutlined />
              </NIcon>
            </NButton>
            <NFormItem
              size={'small'}
              class={styles.right}
              showFeedback={false}
              labelPlacement={'left'}
              label={t('project.workflow.tree_limit')}
            >
              <NSelect
                size='small'
                defaultValue={25}
                onUpdateValue={onSelectChange}
                options={options}
              />
            </NFormItem>
          </div>
        </Card>
        <Card title={t('project.workflow.tree_view')}>
          <NSpace align='center'>
            {taskTypeNodeOptions
              .filter((option: any) => option.image)
              .map((option: any, unused: number) => (
                <NButton text size='tiny' color={option.color}>
                  <NImage width='20' src={option.image} />
                  {option.taskType}
                </NButton>
              ))}
          </NSpace>
          <UseTree chartData={chartData} />
        </Card>
      </div>
    )
  }
})
