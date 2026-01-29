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
    default: 590
  },
  width: {
    type: [String, Number] as PropType<string | number>,
    default: '100%'
  },
  data: {
    type: Array as PropType<Array<any>>
  }
}

const PieChart = defineComponent({
  name: 'PieChart',
  props,
  setup(props) {
    const pieChartRef: Ref<HTMLDivElement | null> = ref(null)

    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: '0%',
        left: 'center'
      },
      series: [
        {
          type: 'pie',
          radius: ['35%', '60%'],
          center: ['50%', '40%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          labelLine: {
            show: false
          },
          data: props.data
        }
      ]
    }

    initChart(pieChartRef, option)

    return { pieChartRef }
  },
  render() {
    const { height, width } = this
    return (
      <div
        ref='pieChartRef'
        style={{
          height: typeof height === 'number' ? height + 'px' : height,
          width: typeof width === 'number' ? width + 'px' : width
        }}
      />
    )
  }
})

export default PieChart
