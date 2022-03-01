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
import {NButton, NFormItem, NIcon, NSelect} from 'naive-ui'
import { defineComponent, onMounted, Ref, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import UseTree from '@/views/projects/workflow/definition/tree/use-tree'
import { IChartDataItem } from '@/components/chart/modules/types'
import { Router, useRouter } from 'vue-router'
import { viewTree } from "@/service/modules/process-definition";
import { SelectMixedOption } from "naive-ui/lib/select/src/interface";

export default defineComponent({
  name: 'WorkflowDefinitionTiming',
  setup() {
    const router: Router = useRouter()
    const options: Ref<Array<SelectMixedOption>> = ref([{label: '25', value: 25},
      {label: '50', value: 50},
      {label: '75', value: 75},
      {label: '100', value: 100}]
    )

    const projectCode = ref(Number(router.currentRoute.value.params.projectCode))
    const definitionCode = ref(Number(router.currentRoute.value.params.definitionCode))

    const chartData: Ref<Array<IChartDataItem>> = ref([] as IChartDataItem[])

    const getWorkflowTreeData = async (limit: number)  => {
      if (projectCode && definitionCode) {
        const res = await viewTree(projectCode.value, definitionCode.value, {limit: limit})
        chartData.value = [{name: 'DAG', value: 'DAG'}]
        initChartData(res, chartData.value[0])
      }
    }

    const initChartData = (node: any, newNode: any) => {
      newNode.children = []
      node?.children.map((child: any) => {
        let newChild = {}
        initChartData(child, newChild)
        newNode.children.push(newChild)
      })
      newNode.name = node.name
      newNode.value = node?.type
    }
    const onSelectChange = (value: number) => {
      if (value) {
        getWorkflowTreeData(value)
      }
    }

    onMounted(() => {
      getWorkflowTreeData(25)
    })

    return {
      chartData,
      options,
      onSelectChange
    }
  },
  render() {
    const { chartData, options, onSelectChange} = this
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
            <UseTree chartData={ chartData }/>
        </Card>
      </div>
    )
  }
})
