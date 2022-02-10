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

import { defineComponent, PropType, ref } from 'vue'
import initChart from '@/components/chart'
import { useI18n } from 'vue-i18n'
import type { Ref } from 'vue'
import { format } from 'date-fns'

const props = {
  height: {
    type: [String, Number] as PropType<string | number>,
    default: window.innerHeight - 174
  },
  width: {
    type: [String, Number] as PropType<string | number>,
    default: '100%'
  },
  seriesData: {
    type: Array as PropType<Array<any>>,
    default: () => []
  },
  labelShow: {
    type: Boolean as PropType<boolean>,
    default: true
  }
}

const GraphChart = defineComponent({
  name: 'GraphChart',
  props,
  setup(props) {
    const graphChartRef: Ref<HTMLDivElement | null> = ref(null)
    const { t } = useI18n()

    console.log(props.seriesData)

    const legendData = [
      { name: t('project.workflow.online') },
      { name: t('project.workflow.workflow_offline') },
      { name: t('project.workflow.schedule_offline') }
    ]

    const getCategory = (schedulerStatus: number, workflowStatus: number) => {
      console.log(schedulerStatus, workflowStatus)

      switch (true) {
        case workflowStatus === 0:
          return 1
        case workflowStatus === 1 && schedulerStatus === 0:
          return 2
        case workflowStatus === 1 && schedulerStatus === 1:
        default:
          return 0
      }
    }

    const option: any = {
      tooltip: {
        confine: true,
        backgroundColor: '#fff',
        formatter: (params: any) => {
          if (!params.data.name) {
            return false
          }

          const {
            name,
            scheduleStartTime,
            scheduleEndTime,
            crontab,
            workFlowPublishStatus,
            schedulePublishStatus
          } = params.data

          return `
            ${t('project.workflow.workflow_name')}：${name}<br/>
            ${t(
              'project.workflow.schedule_start_time'
            )}：${scheduleStartTime}<br/>
            ${t('project.workflow.schedule_end_time')}：${scheduleEndTime}<br/>
            ${t('project.workflow.crontab_expression')}：${
            crontab ? crontab : ' - '
          }<br/>
            ${t(
              'project.workflow.workflow_publish_status'
            )}：${workFlowPublishStatus}<br/>
            ${t(
              'project.workflow.schedule_publish_status'
            )}：${schedulePublishStatus}<br/>
          `
        }
      },
      legend: [
        {
          data: legendData?.map((item) => item.name)
        }
      ],
      series: [
        {
          type: 'graph',
          layout: 'force',
          draggable: true,
          force: {
            repulsion: 300,
            edgeLength: 100
          },
          symbol: 'roundRect',
          symbolSize: 70,
          roam: false,
          label: {
            show: props.labelShow,
            formatter: (val: any) => {
              let newStr = ''
              const str = val.data.name.split('')

              for (let i = 0, s; (s = str[i++]); ) {
                newStr += s
                if (!(i % 10)) newStr += '\n'
              }

              return newStr.length > 60 ? newStr.slice(0, 60) + '...' : newStr
            }
          },
          data: props.seriesData.map((item) => {
            return {
              name: item.name,
              id: item.id,
              category: getCategory(
                Number(item.schedulePublishStatus),
                Number(item.workFlowPublishStatus)
              ),
              workFlowPublishStatus: format(
                new Date(item.workFlowPublishStatus),
                'yyyy-MM-dd HH:mm:ss'
              ),
              schedulePublishStatus: format(
                new Date(item.schedulePublishStatus),
                'yyyy-MM-dd HH:mm:ss'
              ),
              crontab: item.crontab,
              scheduleStartTime:
                Number(item.scheduleStartTime) === 0
                  ? t('project.workflow.offline')
                  : t('project.workflow.online'),
              scheduleEndTime:
                Number(item.scheduleEndTime) === 0
                  ? t('project.workflow.offline')
                  : t('project.workflow.online')
            }
          }),
          categories: legendData
        }
      ]
    }

    initChart(graphChartRef, option)

    return { graphChartRef }
  },
  render() {
    const { height, width } = this
    return (
      <div
        ref='graphChartRef'
        style={{
          height: typeof height === 'number' ? height + 'px' : height,
          width: typeof width === 'number' ? width + 'px' : width
        }}
      />
    )
  }
})

export default GraphChart
