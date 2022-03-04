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
import { ITaskTypeNodeOption, ITaskStateOption } from './types'


export default defineComponent({
  name: 'WorkflowDefinitionTiming',
  setup() {
    const router: Router = useRouter()
    const { t, locale } = useI18n()
    const options: Ref<Array<SelectMixedOption>> = ref([{label: '25', value: 25},
      {label: '50', value: 50},
      {label: '75', value: 75},
      {label: '100', value: 100}]
    )

    const projectCode = ref(Number(router.currentRoute.value.params.projectCode))
    const definitionCode = ref(Number(router.currentRoute.value.params.definitionCode))

    const chartData: Ref<Array<IChartDataItem>> = ref([] as IChartDataItem[])
    const taskStateMap = ref()

    const taskTypeNodeOptions:Ref<Array<ITaskTypeNodeOption>> = ref([
      { taskType: 'SHELL', color: '#646464', image: '/src/assets/images/task-icons/shell.png' },
      { taskType: 'SUB_PROCESS', color: '#4295DA', image: '/src/assets/images/task-icons/sub_process.png' },
      { taskType: 'PROCEDURE', color: '#545CC6', image: '/src/assets/images/task-icons/procedure.png'  },
      { taskType: 'SQL', color: '#8097A0', image: '/src/assets/images/task-icons/sql.png' },
      { taskType: 'SPARK', color: '#a16435', image: '/src/assets/images/task-icons/spark.png' },
      { taskType: 'FLINK', color: '#d68f5b', image: '/src/assets/images/task-icons/flink.png' },
      { taskType: 'MR', color: '#A1A5C9', image: '/src/assets/images/task-icons/mr.png' },
      { taskType: 'PYTHON', color: '#60BCD5', image: '/src/assets/images/task-icons/python.png' },
      { taskType: 'DEPENDENT', color: '#60BCD5', image: '/src/assets/images/task-icons/dependent.png' },
      { taskType: 'HTTP', color: '#7f3903', image: '/src/assets/images/task-icons/http.png' },
      { taskType: 'DATAX', color: '#75CC71', image: '/src/assets/images/task-icons/datax.png' },
      { taskType: 'PIGEON', color: '#5EC459', image: '/src/assets/images/task-icons/pigeon.png' },
      { taskType: 'SQOOP', color: '#f98b3d', image: '/src/assets/images/task-icons/sqoop.png' },
      { taskType: 'CONDITIONS', color: '#b99376', image: '/src/assets/images/task-icons/conditions.png' },
      { taskType: 'SWITCH', color: '#ff6f00', image: '/src/assets/images/task-icons/switch.png' },
      { taskType: 'SEATUNNEL', color: '#8c8c8f', image: '/src/assets/images/task-icons/seatunnel.png' },
      { taskType: 'DAG', color: '#bbdde9' }
    ])

    const initTaskStateMap = () => {
      taskStateMap.value = [
        {state: 'SUBMITTED_SUCCESS', value: t('project.task.submitted_success'), color: '#A9A9A9'},
        {state: 'RUNNING_EXECUTION', value: t('project.task.running_execution'), color: '#4295DA'},
        {state: 'READY_PAUSE', value: t('project.task.ready_pause'), color: '#50AEA3'},
        {state: 'PAUSE', value: t('project.task.pause'), color: '#367A72'},
        {state: 'READY_STOP', value: t('project.task.ready_stop'), color: '#E93424'},
        {state: 'STOP', value: t('project.task.stop'), color: '#D62E20'},
        {state: 'FAILURE', value: t('project.task.failed'), color: '#000000'},
        {state: 'SUCCESS', value: t('project.task.success'), color: '#67C93B'},
        {state: 'NEED_FAULT_TOLERANCE', value: t('project.task.need_fault_tolerance'), color: '#F09235'},
        {state: 'KILL', value: t('project.task.kill'), color: '#991F14'},
        {state: 'WAITING_THREAD', value: t('project.task.waiting_thread'), color: '#8635E4'},
        {state: 'WAITING_DEPEND', value: t('project.task.waiting_depend'), color: '#4A0AB6'},
        {state: 'DELAY_EXECUTION', value: t('project.task.delay_execution'), color: '#c5b4ec'},
        {state: 'FORCED_SUCCESS', value: t('project.task.forced_success'), color: '#453463'},
        {state: 'SERIAL_WAIT', value: t('project.task.serial_wait'), color: '#1b0446'}
      ]
    }

    const initChartData = (node: any, newNode: any) => {
      newNode.children = []
      node?.children.map((child: any) => {
        let newChild = {}
        initChartData(child, newChild)
        newNode.children.push(newChild)
      })

      newNode.name = node.name
      newNode.value = node.name=== 'DAG'? 'DAG':node?.type
      let taskTypeNodeOption = find(taskTypeNodeOptions.value, { taskType:newNode.value })
      if (taskTypeNodeOption) {
        newNode.itemStyle = { color: taskTypeNodeOption.color }
        if (newNode.name !== 'DAG') {
          let taskState = null
          if (node.instances && node.instances.length>0 && node.instances[0].state) {
            taskState = find(taskStateMap.value, {state: node.instances[0].state})
          }
          newNode.label =  {
            show: true,
            formatter: [
              `{name|${t('project.task.task_name')}:${newNode.name}}`,
              `{type|${t('project.task.task_type')}:${taskTypeNodeOption.taskType}}`,
              taskState? `{state|${t('project.workflow.task_state')}: ${taskState.value}}` : ''
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
                color: taskState? taskState.color : 'black',
              },
            }
          }
        }
      }
    }

    const getWorkflowTreeData = async (limit: number)  => {
      if (projectCode && definitionCode) {
        const res = await viewTree(projectCode.value, definitionCode.value, {limit: limit})
        chartData.value = [{name: 'DAG', value: 'DAG'}]
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
    const { chartData, options, onSelectChange, taskTypeNodeOptions} = this
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
            <NFormItem size={'small'} class={styles.right} showFeedback={false} labelPlacement={'left'} label={t('project.workflow.tree_limit')}>
              <NSelect size='small' defaultValue={25} onUpdateValue={onSelectChange} options={ options } />
            </NFormItem>
          </div>
        </Card>
        <Card title={t('project.workflow.tree_view')}>
          <NSpace align="center">
            {taskTypeNodeOptions.filter((option: any) => option.image).map((option:any, index:number) => (
             <NButton text size='tiny' color= { option.color }>
               <NImage width='20' src={ option.image }/>
               { option.taskType }
             </NButton>
            ))}
          </NSpace>
            <UseTree chartData={ chartData }/>
        </Card>
      </div>
    )
  }
})
