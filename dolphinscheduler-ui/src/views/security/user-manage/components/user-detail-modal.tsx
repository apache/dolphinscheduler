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

import {
  defineComponent,
  getCurrentInstance,
  PropType,
  toRefs,
  watch
} from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NInput,
  NForm,
  NFormItem,
  NSelect,
  NRadio,
  NRadioGroup,
  NSpace
} from 'naive-ui'
import { useUserDetail } from './use-user-detail'
import Modal from '@/components/modal'
import type { IRecord } from '../types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  currentRecord: {
    type: Object as PropType<IRecord | null>,
    default: {}
  }
}

export const UserModal = defineComponent({
  name: 'user-modal',
  props,
  emits: ['cancel', 'update'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { state, IS_ADMIN, formRules, onReset, onSave, onSetValues } =
      useUserDetail()
    const onCancel = () => {
      onReset()
      ctx.emit('cancel')
    }
    const onConfirm = async () => {
      const result = await onSave(props.currentRecord?.id)
      if (!result) return
      onCancel()
      ctx.emit('update')
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.show,
      () => {
        if (props.show && props.currentRecord?.id) {
          onSetValues(props.currentRecord)
        }
      }
    )

    return {
      t,
      ...toRefs(state),
      IS_ADMIN,
      formRules,
      onCancel,
      onConfirm,
      trim
    }
  },
  render(props: { currentRecord: IRecord }) {
    const { t } = this
    const { currentRecord } = props
    return (
      <Modal
        show={this.show}
        title={`${t(
          currentRecord?.id
            ? 'security.user.edit_user'
            : 'security.user.create_user'
        )}`}
        onCancel={this.onCancel}
        confirmLoading={this.loading}
        onConfirm={this.onConfirm}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
      >
        <NForm
          ref='formRef'
          model={this.formData}
          rules={this.formRules}
          labelPlacement='left'
          labelAlign='left'
          labelWidth={80}
        >
          <NFormItem label={t('security.user.username')} path='userName'>
            <NInput
              allowInput={this.trim}
              class='input-username'
              v-model:value={this.formData.userName}
              minlength={3}
              maxlength={39}
              placeholder={t('security.user.username_tips')}
            />
          </NFormItem>
          {!this.currentRecord?.id && (
            <NFormItem
              label={t('security.user.user_password')}
              path='userPassword'
            >
              <NInput
                allowInput={this.trim}
                class='input-password'
                type='password'
                v-model:value={this.formData.userPassword}
                placeholder={t('security.user.user_password_tips')}
              />
            </NFormItem>
          )}
          {this.IS_ADMIN && (
            <NFormItem label={t('security.user.tenant_code')} path='tenantId'>
              <NSelect
                class='select-tenant'
                options={this.tenants}
                v-model:value={this.formData.tenantId}
              />
            </NFormItem>
          )}
          {this.IS_ADMIN && (
            <NFormItem label={t('security.user.queue')} path='queue'>
              <NSelect
                class='select-queue'
                options={this.queues}
                v-model:value={this.formData.queue}
                placeholder={t('security.user.queue_tips')}
              />
            </NFormItem>
          )}
          <NFormItem label={t('security.user.email')} path='email'>
            <NInput
              allowInput={this.trim}
              class='input-email'
              v-model:value={this.formData.email}
              placeholder={t('security.user.email_empty_tips')}
            />
          </NFormItem>
          <NFormItem label={t('security.user.phone')} path='phone'>
            <NInput
              allowInput={this.trim}
              class='input-phone'
              v-model:value={this.formData.phone}
              placeholder={t('security.user.phone_empty_tips')}
            />
          </NFormItem>
          <NFormItem label={t('security.user.state')} path='state'>
            <NRadioGroup v-model:value={this.formData.state}>
              <NSpace>
                <NRadio value={1} class='radio-state-enable'>
                  {this.t('security.user.enable')}
                </NRadio>
                <NRadio value={0} class='radio-state-disable'>
                  {this.t('security.user.disable')}
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default UserModal
