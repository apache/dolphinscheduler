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
import { useForm } from './use-form'
import { useModal } from './use-modal'
import { useTable } from './use-table'
import { NDataTable, NPagination } from 'naive-ui'
import Modal from '@/components/modal'
import styles from '../index.module.scss'
import type { IDefinitionData } from '../types'

const props = {
  isInstance: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<IDefinitionData>,
    default: {}
  }
}

export default defineComponent({
  name: 'workflowDefinitionVersion',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const { variables, createColumns, getTableData } = useTable(ctx)
    const { importState } = useForm()
    const { handleImportDefinition } = useModal(importState, ctx)

    const requestData = () => {
      if (props.show && props.row?.code) {
        getTableData(props.row)
      }
    }

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleImport = () => {
      handleImportDefinition()
    }

    const customRequest = ({ file }: any) => {
      importState.importForm.name = file.name
      importState.importForm.file = file.file
    }

    watch(
      () => props.show,
      () => {
        createColumns(variables, props.isInstance)
        requestData()
      }
    )

    watch(useI18n().locale, () => {
      createColumns(variables, props.isInstance)
    })

    return {
      hideModal,
      handleImport,
      customRequest,
      requestData,
      ...toRefs(variables)
    }
  },

  render() {
    const { t } = useI18n()
    const { requestData, loadingRef } = this

    return (
      <Modal
        show={this.$props.show}
        title={t('project.workflow.version_info')}
        onCancel={this.hideModal}
        onConfirm={this.hideModal}
      >
        <NDataTable
          loading={loadingRef}
          columns={this.columns}
          data={this.tableData}
          striped
          size={'small'}
          class={styles.table}
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
