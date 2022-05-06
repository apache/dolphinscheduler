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
import { useTable } from '../use-table'
import { NDataTable, NDatePicker, NGrid, NGi } from 'naive-ui'
import PieChart from '@/components/chart/modules/Pie'
import Card from '@/components/card'
import type { StateTableData, StateChartData } from '../types'

const props = {
  title: {
    type: String as PropType<string>
  },
  date: {
    type: Array as PropType<Array<any>>
  },
  tableData: {
    type: Array as PropType<Array<StateTableData>>,
    default: () => []
  },
  chartData: {
    type: Array as PropType<Array<StateChartData>>,
    default: () => []
  },
  loadingRef: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

const StateCard = defineComponent({
  name: 'StateCard',
  props,
  emits: ['updateDatePickerValue'],
  setup(props, ctx) {
    const onUpdateDatePickerValue = (val: any) => {
      ctx.emit('updateDatePickerValue', val)
    }

    return { onUpdateDatePickerValue }
  },
  render() {
    const {
      title,
      date,
      tableData,
      chartData,
      onUpdateDatePickerValue,
      loadingRef
    } = this
    const { columnsRef } = useTable()
    return (
      <Card title={title}>
        {{
          default: () => (
            <NGrid x-gap={12} cols={2}>
              <NGi>{chartData.length > 0 && <PieChart data={chartData} />}</NGi>
              <NGi>
                {tableData && (
                  <NDataTable
                    loading={loadingRef}
                    columns={columnsRef}
                    data={tableData}
                    striped
                    size={'small'}
                  />
                )}
              </NGi>
            </NGrid>
          ),
          'header-extra': () => (
            <NDatePicker
              default-value={date}
              onUpdateValue={onUpdateDatePickerValue}
              size='small'
              type='datetimerange'
              clearable
            />
          )
        }}
      </Card>
    )
  }
})

export default StateCard
