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

import { defineComponent, PropType, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { NDataTable } from 'naive-ui'
import Modal from '@/components/modal'
import { useTable } from '../use-table'
import styles from '../index.module.scss'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

export default defineComponent({
  name: 'ruleInputEntry',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const { variables, getTableData } = useTable(ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    watch(
      () => props.row.code,
      () => {
        getTableData(props.row)
      }
    )

    return {
      hideModal,
      ...toRefs(variables)
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={t('data_quality.rule.input_item')}
        onCancel={this.hideModal}
        onConfirm={this.hideModal}
      >
        <NDataTable
          columns={this.columns}
          data={this.tableData}
          striped
          size={'small'}
          class={styles.table}
        />
      </Modal>
    )
  }
})
