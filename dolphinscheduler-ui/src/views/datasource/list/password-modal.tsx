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
  reactive,
  toRefs,
  watch
} from 'vue'
import {
  NSpin,
  NForm,
  NFormItem,
  NInput
} from 'naive-ui'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import { useDetail } from './use-detail'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  id: {
    type: Number as PropType<number>
  },
  selectType: {
    type: String as PropType<any>,
    default: 'MYSQL'
  }
}

const PasswordModal = defineComponent({
  name: 'PasswordModal',
  props,
  emits: ['cancel', 'update'],
  setup(props, ctx) {
    const { t } = useI18n()

    const state = reactive({
      passwordForm: {
        password: '',
        confirmPassword: ''
      },
      rules: {
        password: {
          trigger: ['input'],
          validator() {
            if (!state.passwordForm.password) {
              return new Error(t('datasource.user_password_tips'))
            }
          }
        },
        confirmPassword: {
          trigger: ['input', 'blur'],
          validator() {
            if (!state.passwordForm.confirmPassword) {
              return new Error(t('datasource.confirm_password_tips'))
            }
            if (state.passwordForm.password !== state.passwordForm.confirmPassword) {
              return new Error(t('datasource.password_mismatch'))
            }
          }
        }
      }
    })

    const { status, updatePassword } = useDetail()

    const onCancel = () => {
      state.passwordForm.password = ''
      state.passwordForm.confirmPassword = ''
      ctx.emit('cancel')
    }

    const onSubmit = async () => {
      // 检查密码是否一致
      if (state.passwordForm.password !== state.passwordForm.confirmPassword) {
        console.error('Passwords do not match')
        return false
      }
      
      // 这里需要调用更新密码的API
      if (!props.id) {
        console.error('Data source ID is required')
        return false
      }
      const res = await updatePassword(props.id, state.passwordForm.password, state.passwordForm.confirmPassword)
      if (res) {
        onCancel()
        ctx.emit('update')
      }
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.show,
      async () => {
        if (props.show && props.id) {
          // 清空密码字段，让用户重新输入
          state.passwordForm.password = ''
          state.passwordForm.confirmPassword = ''
        }
      }
    )

    return {
      t,
      state,
      ...toRefs(status),
      onSubmit,
      onCancel,
      trim
    }
  },
  render() {
    const {
      show,
      id,
      t,
      loading,
      saving,
      onCancel,
      onSubmit
    } = this
    return (
      <Modal
        class='dialog-edit-password'
        show={show}
        title={t('datasource.edit_password')}
        onConfirm={onSubmit}
        confirmLoading={saving || loading}
        onCancel={onCancel}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
      >
        {{
          default: () => (
            <NSpin show={loading}>
              <NForm
                rules={this.state.rules}
                require-mark-placement='left'
                label-align='left'
              >
                <NFormItem
                  label={t('datasource.user_password')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-password'
                    v-model:value={this.state.passwordForm.password}
                    type='password'
                    placeholder={t('datasource.user_password_tips')}
                  />
                </NFormItem>
                <NFormItem
                  label={t('datasource.confirm_password')}
                  path='confirmPassword'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-confirm-password'
                    v-model:value={this.state.passwordForm.confirmPassword}
                    type='password'
                    placeholder={t('datasource.confirm_password_tips')}
                  />
                </NFormItem>
              </NForm>
            </NSpin>
          )
        }}
      </Modal>
    )
  }
})

export default PasswordModal