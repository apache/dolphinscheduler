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

import {defineComponent, h, PropType, reactive, ref, toRefs, watch} from 'vue'
import { useI18n } from 'vue-i18n'
import { NDataTable } from 'naive-ui'
import Modal from '@/components/modal'
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
  emits: ['cancel', 'confirm'],
  setup(props, ctx) {

    const { t } = useI18n()

    const variables = reactive({
      columns: [],
      tableData: []
    })

    const createColumns = (variables: any) => {
      variables.columns = [
        {
          title: t('data_quality.rule.name'),
          key: 'ruleName'
        },
        {
          title: t('data_quality.rule.type'),
          key: 'ruleTypeName'
        },
        {
          title: t('data_quality.rule.username'),
          key: 'userName'
        },
        {
          title: t('data_quality.rule.create_time'),
          key: 'createTime'
        },
        {
          title: t('data_quality.rule.update_time'),
          key: 'updateTime'
        }
      ]
    }

    const onCancel = () => {
      ctx.emit('cancel')
    }

    const onConfirm = () => {
      ctx.emit('confirm')
    }

    return {
      onCancel,
      onConfirm,
      createColumns,
      ...variables
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={t('data_quality.rule.input_item')}
        onCancel={this.onCancel}
        onConfirm={this.onConfirm}
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
