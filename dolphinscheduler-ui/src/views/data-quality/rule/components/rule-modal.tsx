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
import { useI18n } from 'vue-i18n'
import { NDataTable } from 'naive-ui'
import Modal from '@/components/modal'
import { TableColumns } from 'naive-ui/es/data-table/src/interface'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: String as PropType<string>,
    default: ''
  }
}

export default defineComponent({
  name: 'ruleInputEntry',
  props,
  emits: ['cancel', 'confirm'],
  setup(props, ctx) {
    const { t } = useI18n()

    const ruleInputEntryList = JSON.parse(props.data).ruleInputEntryList

    ruleInputEntryList.forEach((item: any) => {
      item.title = t(
        'data_quality.rule.' + item.title.substring(3, item.title.length - 1)
      )
    })

    const columns: TableColumns<any> = [
      {
        title: t('data_quality.rule.input_item_title'),
        key: 'title'
      },
      {
        title: t('data_quality.rule.input_item_placeholder'),
        key: 'field'
      },
      {
        title: t('data_quality.rule.input_item_type'),
        key: 'type'
      }
    ]

    const onCancel = () => {
      ctx.emit('cancel')
    }

    const onConfirm = () => {
      ctx.emit('confirm')
    }

    return {
      onCancel,
      onConfirm,
      columns,
      ruleInputEntryList
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={t('data_quality.rule.input_item')}
        cancelShow={false}
        onConfirm={this.onConfirm}
      >
        <NDataTable
          columns={this.columns}
          data={this.ruleInputEntryList}
          striped
          size={'small'}
        />
      </Modal>
    )
  }
})
