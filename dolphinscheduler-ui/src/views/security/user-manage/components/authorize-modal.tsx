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

import { defineComponent, PropType, toRefs, watch, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NInput,
  NButton,
  NIcon,
  NTransfer,
  NSpace,
  NRadioGroup,
  NRadioButton,
  NTreeSelect,
  NDataTable,
  NPagination
} from 'naive-ui'
import { useAuthorize } from './use-authorize'
import Modal from '@/components/modal'
import styles from '../index.module.scss'
import type { TAuthType } from '../types'
import { useColumns } from './use-columns'
import { SearchOutlined } from '@vicons/antd'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  userId: {
    type: Number,
    default: 0
  },
  type: {
    type: String as PropType<TAuthType>,
    default: 'auth_project'
  }
}
let projectIds: string
export const AuthorizeModal = defineComponent({
  name: 'authorize-project-modal',
  props,
  emits: ['cancel'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { state, onInit, onSave, onOperationClick, getProjects, revokeProjectByIdRequest, grantProjectRequest, grantProjectWithReadPermRequest, requestData, handleChangePageSize } = useAuthorize()
    console.log('state', state.pagination)
    const onCancel = () => {
      ctx.emit('cancel')
    }
    const onConfirm = async () => {
      const result = await onSave(props.type, props.userId)
      if (result) onCancel()
    }

    // 新增部分
    const { columnsRef } = useColumns(onOperationClick)
    // const checkedRowKeysRef = ref<any[]>([])
    const handleCheck = (rowKeys: Array<number>) => {
      console.log('rowKeys', rowKeys)
      projectIds = rowKeys.join()
      // checkedRowKeysRef.value = rowKeys
    }
    watch(
      () => props.show,
      () => {
        if (props.show) {
          onInit(props.type, props.userId)
        }
      }
    )

    return {
      t,
      columnsRef,
      rowKey: (row: any) => row.id,
      ...toRefs(state),
      onCancel,
      onConfirm,
      getProjects,
      revokeProjectByIdRequest,
      handleCheck,
      grantProjectRequest,
      grantProjectWithReadPermRequest,
      requestData, 
      handleChangePageSize
    }
  },
  render(props: { type: TAuthType, userId: number }) {
    const { t } = this
    const { type, userId } = props
    return (
      <Modal
        show={this.show}
        title={t(`security.user.${type}`)}
        onCancel={this.onCancel}
        confirmLoading={this.loading}
        onConfirm={this.onConfirm}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
      >
        {/* {type === 'authorize_project' && (
          <NTransfer
            virtualScroll
            options={this.unauthorizedProjects}
            filterable
            v-model={[this.authorizedProjects, 'value']}
            class={styles.transfer}
          />
        )} */}
        {type === 'authorize_project' && (

          <NSpace vertical>
            <NSpace>
              <NButton size='small' type='primary' onClick={() => this.revokeProjectByIdRequest(userId, projectIds)}>
                {t('security.user.revoke_auth')}
              </NButton>
              <NButton size='small' type='primary' onClick={() => this.grantProjectWithReadPermRequest(userId, projectIds)}>
              {t('security.user.grant_read')}
              </NButton>
              <NButton size='small' type='primary' onClick={() => this.grantProjectRequest(userId, projectIds)}>
              {t('security.user.grant_all')}
              </NButton>
              <NInput
                size='small'
                // v-model={[this.searchVal, 'value']}s
                placeholder={t('project.list.project_tips')}
                clearable
                v-model:value={this.searchVal}
              />
              {/* <NButton size='small' type='primary' onClick={this.handleSearch}> */}
              <NButton size='small' type='primary' onClick={() => this.getProjects(userId)}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          <NDataTable
            virtualScroll
            row-class-name='items'
            columns={this.columnsRef.columns}
            data={this.projectWithAuthorizedLevel}
            loading={this.loading}
            // scrollX={this.columnsRef.tableWidth}
            max-height="250"
            row-key={this.rowKey}
            on-update:checked-row-keys={this.handleCheck}
          />
          <div class={styles.pagination}>
            <NPagination
              v-model:page={this.pagination.page}
              v-model:page-size={this.pagination.pageSize}
              page-count={this.pagination.totalPage}
              show-size-picker
              page-sizes={[5, 10]}
              show-quick-jumper
              onUpdatePage={this.requestData}
              onUpdatePageSize={this.handleChangePageSize}
            />
          </div>
          </NSpace>
        )}
        {type === 'authorize_datasource' && (
          <NTransfer
            virtualScroll
            options={this.unauthorizedDatasources}
            filterable
            v-model:value={this.authorizedDatasources}
            class={styles.transfer}
          />
        )}
        {type === 'authorize_udf' && (
          <NTransfer
            virtualScroll
            options={this.unauthorizedUdfs}
            filterable
            v-model:value={this.authorizedUdfs}
            class={styles.transfer}
          />
        )}
        {type === 'authorize_resource' && (
          <NSpace vertical>
            <NRadioGroup v-model:value={this.resourceType}>
              <NRadioButton key='file' value='file'>
                {t('security.user.file_resource')}
              </NRadioButton>
              <NRadioButton key='udf' value='udf'>
                {t('security.user.udf_resource')}
              </NRadioButton>
            </NRadioGroup>
            <NTreeSelect
              v-show={this.resourceType === 'file'}
              filterable
              multiple
              cascade
              checkable
              checkStrategy='child'
              key-field='id'
              label-field='fullName'
              options={this.fileResources}
              v-model:value={this.authorizedFileResources}
            />
            <NTreeSelect
              v-show={this.resourceType === 'udf'}
              filterable
              multiple
              cascade
              checkable
              checkStrategy='child'
              key-field='id'
              label-field='fullName'
              options={this.udfResources}
              v-model:value={this.authorizedUdfResources}
            />
          </NSpace>
        )}
        {type === 'authorize_namespace' && (
          <NTransfer
            virtualScroll
            options={this.unauthorizedNamespaces}
            filterable
            v-model:value={this.authorizedNamespaces}
            class={styles.transfer}
          />
        )}
      </Modal>
    )
  }
})

export default AuthorizeModal
