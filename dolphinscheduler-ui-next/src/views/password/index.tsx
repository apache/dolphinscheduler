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

import { defineComponent, toRefs } from 'vue'
import { NForm, NFormItem, NButton, NInput } from 'naive-ui'
import { useForm } from './use-form'
import Card from '@/components/card'

const password = defineComponent({
  name: 'password',
  setup() {
    const { state, t } = useForm()

    return { ...toRefs(state), t }
  },
  render() {
    const { rules, passwordForm, t, handlePasswordInput } = this

    return (
      <Card title={t('password.edit_password')}>
        {{
          default: () => (
            <div>
              <NForm rules={rules} ref='passwordFormRef'>
                <NFormItem label={t('password.password')} path='password'>
                  <NInput
                    type='password'
                    placeholder={t('password.password_tips')}
                    v-model={[passwordForm.password, 'value']}
                    onInput={handlePasswordInput}
                  />
                </NFormItem>
                <NFormItem
                  label={t('password.confirm_password')}
                  path='confirmPassword'
                  ref='confirmPasswordItemFormRef'
                >
                  <NInput
                    type='password'
                    placeholder={t('password.confirm_password_tips')}
                    v-model={[passwordForm.confirmPassword, 'value']}
                  />
                </NFormItem>
              </NForm>
              <NButton
                disabled={
                  !passwordForm.password ||
                  !passwordForm.confirmPassword ||
                  passwordForm.password !== passwordForm.confirmPassword
                }
                type='info'
              >
                {t('password.submit')}
              </NButton>
            </div>
          ),
        }}
      </Card>
    )
  },
})

export default password
