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

import { defineComponent, PropType, ref, watch, reactive } from 'vue'
import initChart from '@/components/chart'
import type { Ref } from 'vue'
import { IChartDataItem } from '@/components/chart/modules/types'

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
    type: Array as PropType<Array<IChartDataItem>>
  }
}

const TreeChart = defineComponent({
  name: 'TreeChart',
  props,
  setup(props) {
    const treeChartRef: Ref<HTMLDivElement | null> = ref(null)

    const option = reactive({
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff'
      },
      textStyle: {
        fontSize: 14
      },
      series: [
        {
          type: 'tree',
          id: 0,
          name: 'tree1',
          data: props.data,
          top: '10%',
          left: '5%',
          bottom: '10%',
          right: '15%',
          symbol: 'circle',
          symbolSize: 18,
          edgeShape: 'polyline',
          edgeForkPosition: '63%',
          initialTreeDepth: 'auto',
          lineStyle: {
            width: 3
          },
          label: {
            backgroundColor: '#fff',
            position: 'left',
            verticalAlign: 'middle',
            align: 'right'
          },
          leaves: {
            label: {
              position: 'right',
              verticalAlign: 'middle',
              align: 'left'
            }
          },
          emphasis: {
            focus: 'descendant'
          },
          expandAndCollapse: true,
          animationDuration: 550,
          animationDurationUpdate: 750
        }
      ]
    })

    initChart(treeChartRef, option)

    watch(
      () => props.data,
      () => {
        option.series[0].data = props.data
      }
    )

    return { treeChartRef }
  },
  render() {
    const { height, width } = this
    return (
      <div
        ref='treeChartRef'
        style={{
          height: typeof height === 'number' ? height + 'px' : height,
          width: typeof width === 'number' ? width + 'px' : width
        }}
      />
    )
  }
})

export default TreeChart
