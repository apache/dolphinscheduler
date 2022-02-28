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
import { NButton, NIcon } from 'naive-ui'
import { defineComponent, onMounted, Ref, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from '../index.module.scss'
import UseTree from '@/views/projects/workflow/definition/tree/use-tree'
import { IChartDataItem } from '@/components/chart/modules/types'
import {Router, useRouter} from 'vue-router'
import {viewTree} from "@/service/modules/process-definition";

export default defineComponent({
  name: 'WorkflowDefinitionTiming',
  setup() {
    const router: Router = useRouter()

    const projectCode = ref(Number(router.currentRoute.value.params.projectCode))
    const definitionCode = ref(Number(router.currentRoute.value.params.definitionCode))

    // let chartData: IChartDataItem = {name: '', value: ''}
    //
    // const getWorkflowTreeData = async ()  => {
    //   if (projectCode && definitionCode) {
    //     const res = await viewTree(projectCode.value, definitionCode.value, {limit: 50})
    //     initChartData(chartData, res)
    //     chartData = res
    //     console.log(chartData)
    //   }
    // }
    //
    // const initChartData = (chartData: any, node: any) => {
    //   chartData.children = []
    //   node?.children.map((child: any) => {
    //     let newChild = {}
    //     initChartData(newChild, child)
    //     chartData.children.push(newChild)
    //     console.log(chartData)
    //   })
    //   chartData.name = node.name
    //   chartData.value = node?.type
    // }
    //
    let chartData: Ref<IChartDataItem> = ref({} as IChartDataItem)
    onMounted(() => {
      // getWorkflowTreeData()
      chartData.value = {
        "name": "Upstream shell",
        "value": "SHELL",
        "children": [
          {
            "children": [
              {
                "children": [],
                "name": "Downstream shell 0",
                "value": "SHELL"
              },
              {
                "children": [],
                "name": "Downstream shell 1",
                "value": "SHELL"
              }
            ],
            "name": "Switch Task",
            "value": "SWITCH"
          }
        ]
      }
      console.log(chartData)
    })

    //
    // const chartData: IChartDataItem = {
    //   name: 'flare',
    //   children: [
    //     {
    //       name: 'data',
    //       children: [
    //         {
    //           name: 'converters',
    //           children: [
    //             { name: 'Converters', value: 721 },
    //             { name: 'DelimitedTextConverter', value: 4294 }
    //           ]
    //         },
    //         {
    //           name: 'DataUtil',
    //           value: 3322
    //         }
    //       ]
    //     },
    //     {
    //       name: 'display',
    //       children: [
    //         { name: 'DirtySprite', value: 8833 },
    //         { name: 'LineSprite', value: 1732 },
    //         { name: 'RectSprite', value: 3623 }
    //       ]
    //     },
    //     {
    //       name: 'flex',
    //       children: [{ name: 'FlareVis', value: 4116 }]
    //     },
    //     {
    //       name: 'query',
    //       children: [
    //         { name: 'AggregateExpression', value: 1616 },
    //         { name: 'And', value: 1027 },
    //         { name: 'Arithmetic', value: 3891 },
    //         { name: 'Average', value: 891 },
    //         { name: 'BinaryExpression', value: 2893 },
    //         { name: 'Comparison', value: 5103 },
    //         { name: 'CompositeExpression', value: 3677 },
    //         { name: 'Count', value: 781 },
    //         { name: 'DateUtil', value: 4141 },
    //         { name: 'Distinct', value: 933 },
    //         { name: 'Expression', value: 5130 }
    //       ]
    //     },
    //     {
    //       name: 'scale',
    //       children: [
    //         { name: 'IScaleMap', value: 2105 },
    //         { name: 'LinearScale', value: 1316 },
    //         { name: 'LogScale', value: 3151 },
    //         { name: 'OrdinalScale', value: 3770 },
    //         { name: 'QuantileScale', value: 2435 },
    //         { name: 'QuantitativeScale', value: 4839 },
    //         { name: 'RootScale', value: 1756 },
    //         { name: 'Scale', value: 4268 },
    //         { name: 'ScaleType', value: 1821 },
    //         { name: 'TimeScale', value: 5833 }
    //       ]
    //     }
    //   ]
    // }

    return {
      chartData
    }
  },
  render() {
    const { chartData } = this
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
          </div>
        </Card>
        <Card title={t('project.workflow.tree_view')}>
            <UseTree chartData={ [chartData] }/>
        </Card>
      </div>
    )
  }
})
