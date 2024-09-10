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

import { defineComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import { NDataTable } from 'naive-ui'
import Modal from '@/components/modal'
import type { PropType } from 'vue'
import type {
  RowData,
  TableColumns
} from 'naive-ui/es/data-table/src/interface'

const props = {
  showModal: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: Array as PropType<Array<RowData>>,
    default: () => []
  }
}

const NodeModal = defineComponent({
  props,
  emits: ['confirmModal'],
  setup(props, ctx) {
    const { t } = useI18n()
    const columnsRef: TableColumns<any> = [
      { title: '#', key: 'index', render: (row, index) => index + 1 },
      { title: t('monitor.master.directory'), key: 'directory' }
    ]

    const onConfirm = () => {
      ctx.emit('confirmModal')
    }

    return { t, columnsRef, onConfirm }
  },
  render() {
    const { t, columnsRef, onConfirm } = this

    return (
      <Modal
        title={t('monitor.master.directory_detail')}
        show={this.showModal}
        cancelShow={false}
        onConfirm={onConfirm}
      >
        {{
          default: () => (
            <NDataTable
              columns={columnsRef}
              data={this.data}
              striped
              size={'small'}
            />
          )
        }}
      </Modal>
    )
  }
})

export default NodeModal
