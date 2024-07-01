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
import { NInput, NForm, NFormItem } from 'naive-ui'
import { usePassword } from './use-password'
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

export const PasswordModal = defineComponent({
  name: 'password-modal',
  props,
  emits: ['cancel', 'update'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { state, IS_ADMIN, formRules, onReset, onSave, onSetValues } =
      usePassword()

    const onCancel = () => {
      onReset()
      ctx.emit('cancel')
    }
    const onConfirm = async () => {
      if (props.currentRecord?.id) {
        const result = await onSave(props.currentRecord)
        if (!result) return
      }
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
  render() {
    const { t } = this

    return (
      <Modal
        show={this.show}
        title={t('security.user.reset_password')}
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
          labelWidth={150}
        >
          <NFormItem label={t('security.user.username')} path='userName'>
            <NInput
              allowInput={this.trim}
              class='input-username'
              v-model:value={this.formData.userName}
              minlength={3}
              maxlength={39}
              disabled={true}
              placeholder={t('security.user.username_tips')}
            />
          </NFormItem>
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
          <NFormItem
            label={t('password.confirm_password')}
            path='confirmPassword'
          >
            <NInput
              allowInput={this.trim}
              type='password'
              v-model:value={this.formData.confirmPassword}
              placeholder={t('password.confirm_password_tips')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default PasswordModal
