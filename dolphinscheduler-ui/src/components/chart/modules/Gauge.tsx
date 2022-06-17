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
            fontSize: 20
          },
          detail: {
            valueAnimation: true,
            formatter: '{value} %',
            color: 'auto'
          },
          data: [
            {
              value: props.data
            }
          ]
        }
      ]
    }

    initChart(gaugeChartRef, option)

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
