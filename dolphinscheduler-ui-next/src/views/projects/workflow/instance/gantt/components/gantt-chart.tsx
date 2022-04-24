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

import { defineComponent, ref, PropType } from 'vue'
import * as echarts from 'echarts'
import type { Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import initChart from '@/components/chart'
import { tasksState } from '@/common/common'
import { format } from 'date-fns'
import { parseTime } from '@/common/common'
import type { ISeriesData, ITaskState } from '../type'

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
  taskList: {
    type: Array as PropType<Array<string>>,
    default: []
  }
}

const GanttChart = defineComponent({
  name: 'GanttChart',
  props,
  setup(props) {
    const graphChartRef: Ref<HTMLDivElement | null> = ref(null)
    const { t } = useI18n()

    const state = tasksState(t)

    const data: ISeriesData = {}
    Object.keys(state).forEach((key) => (data[key] = []))
    const series = Object.keys(state).map((key) => ({
      id: key,
      type: 'custom',
      name: state[key as ITaskState].desc,
      renderItem: renderItem,
      itemStyle: {
        opacity: 0.8,
        color: state[key as ITaskState].color,
        color0: state[key as ITaskState].color
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: data[key]
    }))

    // format series data
    let minTime = Number(new Date())
    props.seriesData.forEach(function (task, index) {
      minTime = minTime < task.startDate[0] ? minTime : task.startDate[0]
      data[task.status].push({
        name: task.name,
        value: [
          index,
          task.startDate[0],
          task.endDate[0],
          task.endDate[0] - task.startDate[0]
        ],
        itemStyle: {
          color: state[task.status as ITaskState].color
        }
      })
    })

    // customer render
    function renderItem(params: any, api: any) {
      const taskIndex = api.value(0)
      const start = api.coord([api.value(1), taskIndex])
      const end = api.coord([api.value(2), taskIndex])
      const height = api.size([0, 1])[1] * 0.6

      const rectShape = echarts.graphic.clipRectByRect(
        {
          x: start[0],
          y: start[1] - height / 2,
          width: end[0] - start[0],
          height: height
        },
        {
          x: params.coordSys.x,
          y: params.coordSys.y,
          width: params.coordSys.width,
          height: params.coordSys.height
        }
      )
      return (
        rectShape && {
          type: 'rect',
          transition: ['shape'],
          shape: rectShape,
          style: api.style()
        }
      )
    }

    const option = {
      title: {
        text: t('project.workflow.task_state'),
        textStyle: {
          fontWeight: 'normal',
          fontSize: 14
        },
        left: 50
      },
      tooltip: {
        formatter: function (params: any) {
          const taskName = params.data.name
          const data = props.seriesData.filter(
            (item) => item.taskName === taskName
          )
          let str = `taskName : ${taskName}</br>`
          str += `status : ${state[data[0].status as ITaskState].desc} (${
            data[0].status
          })</br>`
          str += `startTime : ${data[0].isoStart}</br>`
          str += `endTime : ${data[0].isoEnd}</br>`
          str += `duration : ${data[0].duration}</br>`
          return str
        }
      },
      legend: {
        left: 150,
        padding: [5, 5, 5, 5]
      },
      dataZoom: [
        {
          type: 'slider',
          filterMode: 'weakFilter',
          showDataShadow: false,
          top:
            props.taskList.length * 100 > 200
              ? props.taskList.length * 100
              : 200,
          labelFormatter: ''
        },
        {
          type: 'inside',
          filterMode: 'weakFilter'
        }
      ],
      grid: {
        height: props.taskList.length * 50,
        top: 80
      },
      xAxis: {
        min: minTime,
        scale: true,
        position: 'top',
        axisTick: { show: true },
        splitLine: { show: false },
        axisLabel: {
          formatter: function (val: number) {
            return format(parseTime(val), 'HH:mm:ss')
          }
        }
      },
      yAxis: {
        axisTick: { show: false },
        splitLine: { show: false },
        axisLine: { show: false },
        max: props.taskList.length,
        data: props.taskList
      },
      series: series
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

export default GanttChart
