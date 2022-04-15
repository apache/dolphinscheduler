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
import Modal from '@/components/modal'
import { NDataTable, NPagination } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useVersion } from './use-version'
import styles from './version.module.scss'

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

const VersionModal = defineComponent({
  name: 'VersionModal',
  props,
  emits: ['confirm', 'refresh'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { variables, getTableData, createColumns } = useVersion()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page
      })
    }

    watch(
      () => props.show,
      () => {
        if (props.show) {
          variables.taskVersion = props.row?.taskVersion
          variables.taskCode = props.row?.taskCode
          createColumns(variables)
          requestData()
        }
      }
    )

    watch(
      () => variables.refreshTaskDefinition,
      () => {
        if (variables.refreshTaskDefinition) {
          ctx.emit('refresh')
          variables.refreshTaskDefinition = false
        }
      }
    )

    const onConfirm = () => {
      ctx.emit('confirm')
    }

    return { t, ...toRefs(variables), requestData, onConfirm }
  },
  render() {
    const { t, requestData, onConfirm, show, loadingRef } = this

    return (
      <Modal
        show={show}
        title={t('project.task.version')}
        cancelShow={false}
        onConfirm={onConfirm}
      >
        <NDataTable
          loading={loadingRef}
          columns={this.columns}
          data={this.tableData}
        />
        <div class={styles.pagination}>
          <NPagination
            v-model:page={this.page}
            v-model:page-size={this.pageSize}
            page-count={this.totalPage}
            onUpdatePage={requestData}
          />
        </div>
      </Modal>
    )
  }
})

export default VersionModal
