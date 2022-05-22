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

import { defineComponent, PropType } from 'vue'
import { NGrid, NGi } from 'naive-ui'
import TreeChart from '@/components/chart/modules/Tree'
import Card from '@/components/card'
import { IChartDataItem } from '@/components/chart/modules/types'

const props = {
  title: {
    type: String as PropType<string>
  },
  chartData: {
    type: Array as PropType<Array<IChartDataItem>>,
    default: () => []
  }
}

const UseTree = defineComponent({
  name: 'TreeCard',
  props,
  emits: ['updateDatePickerValue'],
  setup(props, ctx) {
    const onUpdateDatePickerValue = (val: any) => {
      ctx.emit('updateDatePickerValue', val)
    }

    return { onUpdateDatePickerValue }
  },
  render() {
    const { title, chartData } = this

    return (
      <Card title={title}>
        <NGrid x-gap={12} cols={1}>
          <NGi>{chartData.length > 0 && <TreeChart data={chartData} />}</NGi>
        </NGrid>
      </Card>
    )
  }
})

export default UseTree
