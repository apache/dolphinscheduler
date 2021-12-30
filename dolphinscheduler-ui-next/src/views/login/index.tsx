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

import { withKeys } from 'vue'
import styles from './index.module.scss'
import { NInput, NButton, NSwitch, NForm, NFormItem } from 'naive-ui'
import { useValidate } from './use-validate'
import { useTranslate } from './use-translate'
import { useLogin } from './use-login'

const login = () => {
  const { state, t, locale } = useValidate()

  const { handleChange } = useTranslate(locale)

  const { handleLogin } = useLogin(state)

  return (
    <div class={styles.container}>
      <div class={styles['language-switch']}>
        <NSwitch
          onUpdateValue={handleChange}
          checked-value='en_US'
          unchecked-value='zh_CN'
        >
          {{
            checked: () => 'en_US',
            unchecked: () => 'zh_CN',
          }}
        </NSwitch>
      </div>
      <div class={styles['login-model']}>
        <div class={styles.logo}>
          <div class={styles['logo-img']} />
        </div>
        <div class={styles['form-model']}>
          <NForm rules={state.rules} ref='loginFormRef'>
            <NFormItem
              label={t('login.userName')}
              label-style={{ color: 'black' }}
              path='userName'
            >
              <NInput
                type='text'
                size='large'
                v-model={[state.loginForm.userName, 'value']}
                placeholder={t('login.userName_tips')}
                autofocus
                onKeydown={withKeys(handleLogin, ['enter'])}
              />
            </NFormItem>
            <NFormItem
              label={t('login.userPassword')}
              label-style={{ color: 'black' }}
              path='userPassword'
            >
              <NInput
                type='password'
                size='large'
                v-model={[state.loginForm.userPassword, 'value']}
                placeholder={t('login.userPassword_tips')}
                onKeydown={withKeys(handleLogin, ['enter'])}
              />
            </NFormItem>
          </NForm>
          <NButton
            round
            type='info'
            style={{ width: '100%' }}
            onClick={handleLogin}
          >
            {t('login.login')}
          </NButton>
        </div>
      </div>
    </div>
  )
}

export default login
