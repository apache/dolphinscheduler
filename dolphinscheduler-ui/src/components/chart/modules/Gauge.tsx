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

import {
  defineComponent,
  onMounted,
  onBeforeUnmount,
  PropType,
  ref
} from 'vue'
import initChart from '@/components/chart'
import type { Ref } from 'vue'

const props = {
  height: {
    type: [String, Number] as PropType<string | number>,
    default: 400
  },
  width: {
    type: [String, Number] as PropType<string | number>,
    default: 400
  },
  data: {
    type: [String, Number] as PropType<string | number>
  }
}

const GaugeChart = defineComponent({
  name: 'GaugeChart',
  props,
  setup(props) {
    const gaugeChartRef: Ref<HTMLDivElement | null> = ref(null)
    const windowWidth = window.innerWidth
    // The original size was based on the screen width of 2560
    const defaultFontSize = windowWidth > 2560 ? 20 : (windowWidth / 2560) * 20

    const option = {
      series: [
        {
          type: 'gauge',
          axisLine: {
            lineStyle: {
              width: 30
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            distance: -30,
            length: 8,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          splitLine: {
            distance: -30,
            length: 30,
            lineStyle: {
              color: '#fff',
              width: 4
            }
          },
          axisLabel: {
            color: 'auto',
            distance: 40,
            fontSize: defaultFontSize
          },
          detail: {
            valueAnimation: true,
            formatter: '{value} %',
            color: 'auto',
            fontSize: defaultFontSize * 1.5
          },
          data: [
            {
              value: props.data
            }
          ]
        }
      ]
    }

    const resize = (chart: any) => {
      const clientWidth = gaugeChartRef.value?.clientWidth || 400
      const axisLabelFontSize =
        clientWidth > 400
          ? defaultFontSize
          : (clientWidth / 400) * defaultFontSize
      chart &&
        chart.setOption({
          series: [
            {
              axisLabel: {
                fontSize: axisLabelFontSize
              },
              detail: {
                fontSize: axisLabelFontSize * 1.5
              }
            }
          ]
        })
      chart && chart.resize()
    }

    initChart(gaugeChartRef, option, resize)

    onMounted(() => {
      addEventListener('resize', resize)
    })

    onBeforeUnmount(() => {
      removeEventListener('resize', resize)
    })

    return { gaugeChartRef }
  },
  render() {
    const { height, width } = this
    return (
      <div
        ref='gaugeChartRef'
        style={{
          height: typeof height === 'number' ? height + 'px' : height,
          width: typeof width === 'number' ? width + 'px' : width
        }}
      />
    )
  }
})

export default GaugeChart
