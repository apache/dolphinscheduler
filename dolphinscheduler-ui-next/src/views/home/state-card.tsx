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
import { useTable } from './use-table'
import styles from '@/views/home/index.module.scss'
import PieChart from '@/components/chart/modules/Pie'
import { NDataTable, NDatePicker } from 'naive-ui'
import Card from '@/components/card'

const props = {
  title: {
    type: String as PropType<string>,
  },
  date: {
    type: Array as PropType<Array<any>>,
  },
  tableData: {
    type: [Array, Boolean] as PropType<Array<any> | false>,
  },
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
    const { title, date, tableData, onUpdateDatePickerValue } = this
    const { columnsRef } = useTable()

    return (
      <Card title={title}>
        {{
          default: () => (
            <div class={styles['card-table']}>
              <PieChart />
              {tableData && (
                <NDataTable
                  columns={columnsRef}
                  data={tableData}
                  striped
                  size={'small'}
                />
              )}
            </div>
          ),
          'header-extra': () => (
            <NDatePicker
              default-value={date}
              onUpdateValue={onUpdateDatePickerValue}
              size='small'
              type='datetimerange'
              clearable
            />
          ),
        }}
      </Card>
    )
  },
})

export default StateCard
