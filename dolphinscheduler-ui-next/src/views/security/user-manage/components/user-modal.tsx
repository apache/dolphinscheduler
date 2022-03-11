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

import { defineComponent, inject } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NInput,
  NForm,
  NFormItem,
  NSelect,
  NRadio,
  NRadioGroup,
  NRadioButton,
  NSpace,
  NAlert,
  NTransfer,
  NTreeSelect
} from 'naive-ui'

import Modal from '@/components/modal'
import {
  useModal,
  useSharedUserModalState,
  UserModalSharedStateKey
} from './use-modal'

export const UserModal = defineComponent({
  name: 'user-modal',
  setup() {
    const { t } = useI18n()
    const sharedState =
      inject(UserModalSharedStateKey) || useSharedUserModalState()
    const modalState = useModal(sharedState)

    return {
      t,
      ...modalState
    }
  },
  render() {
    const { t } = this
    return (
      <Modal
        show={this.show}
        title={this.titleMap?.[this.mode || 'add']}
        onCancel={this.onModalCancel}
        confirmDisabled={this.optionsLoading}
        confirmLoading={this.confirmLoading}
        onConfirm={this.onConfirm}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
      >
        {{
          default: () => {
            if (this.mode === 'delete') {
              return (
                <NAlert type='error' title={t('security.user.delete_confirm')}>
                  {t('security.user.delete_confirm_tip')}
                </NAlert>
              )
            }
            if (this.mode === 'auth_project') {
              return (
                <NTransfer
                  virtualScroll
                  options={this.projects}
                  filterable
                  v-model:value={this.authorizedProjects}
                  style={{ margin: '0 auto' }}
                />
              )
            }
            if (this.mode === 'auth_datasource') {
              return (
                <NTransfer
                  virtualScroll
                  options={this.datasource}
                  filterable
                  v-model:value={this.authorizedDatasource}
                  style={{ margin: '0 auto' }}
                />
              )
            }
            if (this.mode === 'auth_udf') {
              return (
                <NTransfer
                  virtualScroll
                  options={this.UDFs}
                  filterable
                  v-model:value={this.authorizedUDF}
                  style={{ margin: '0 auto' }}
                />
              )
            }
            if (this.mode === 'auth_resource') {
              return (
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
                    multiple
                    cascade
                    checkable
                    checkStrategy='child'
                    defaultExpandAll
                    options={this.resourceTree}
                    v-model:value={this.authorizedFiles}
                  />
                </NSpace>
              )
            }
            return (
              <NForm
                ref='formRef'
                model={this.formValues}
                rules={this.formRules}
                labelPlacement='left'
                labelAlign='left'
                labelWidth={80}
              >
                <NFormItem label={t('security.user.username')} path='userName'>
                  <NInput
                    class='input-username'
                    inputProps={{ autocomplete: 'off' }}
                    v-model:value={this.formValues.userName}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.user.user_password')}
                  path='userPassword'
                >
                  <NInput
                    class='input-password'
                    inputProps={{ autocomplete: 'off' }}
                    type='password'
                    v-model:value={this.formValues.userPassword}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.user.tenant_code')}
                  path='tenantId'
                >
                  <NSelect
                    class='select-tenant'
                    options={this.tenants}
                    v-model:value={this.formValues.tenantId}
                  />
                </NFormItem>
                <NFormItem label={t('security.user.queue')} path='queue'>
                  <NSelect
                    class='select-queue'
                    options={this.queues}
                    v-model:value={this.formValues.queue}
                  />
                </NFormItem>
                <NFormItem label={t('security.user.email')} path='email'>
                  <NInput
                    class='input-email'
                    v-model:value={this.formValues.email}
                  />
                </NFormItem>
                <NFormItem label={t('security.user.phone')} path='phone'>
                  <NInput
                    class='input-phone'
                    v-model:value={this.formValues.phone}
                  />
                </NFormItem>
                <NFormItem label={t('security.user.state')} path='state'>
                  <NRadioGroup v-model:value={this.formValues.state}>
                    <NSpace>
                      <NRadio value={1} class='radio-state-enable'>
                        启用
                      </NRadio>
                      <NRadio value={0} class='radio-state-disable'>
                        停用
                      </NRadio>
                    </NSpace>
                  </NRadioGroup>
                </NFormItem>
              </NForm>
            )
          }
        }}
      </Modal>
    )
  }
})

export default UserModal
