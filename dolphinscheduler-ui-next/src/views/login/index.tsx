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

import { defineComponent, reactive, ref, toRefs, withKeys } from 'vue'
import styles from './index.module.scss'
import { useI18n } from 'vue-i18n'
import { NInput, NButton, NSwitch, NForm, NFormItem, FormRules } from 'naive-ui'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'

const Login = defineComponent({
  name: 'login',
  setup() {
    const { t, locale } = useI18n()
    const state = reactive({
      loginFormRef: ref(),
      loginForm: {
        username: '',
        password: '',
      },
      rules: {
        username: {
          trigger: ['input', 'blur'],
          validator() {
            if (state.loginForm.username === '') {
              return new Error(`${t('login.username_tips')}`)
            }
          },
        },
        password: {
          trigger: ['input', 'blur'],
          validator() {
            if (state.loginForm.password === '') {
              return new Error(`${t('login.password_tips')}`)
            }
          },
        },
      } as FormRules,
    })

    const handleChange = (value: string) => {
      locale.value = value
    }

    const router: Router = useRouter()
    const handleLogin = () => {
      state.loginFormRef.validate((valid: any) => {
        if (!valid) {
          router.push({ path: 'home' })
        } else {
          console.log('Invalid')
        }
      })
    }

    return { t, locale, handleChange, handleLogin, ...toRefs(state) }
  },
  render() {
    return (
      <div class={styles.container}>
        <div class={styles['language-switch']}>
          <NSwitch
            onUpdateValue={this.handleChange}
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
            <div class={styles['logo-img']}></div>
          </div>
          <div class={styles['form-model']}>
            <NForm rules={this.rules} ref='loginFormRef'>
              <NFormItem
                label={this.t('login.username')}
                label-style={{ color: 'black' }}
                path='username'
              >
                <NInput
                  type='text'
                  size='large'
                  v-model={[this.loginForm.username, 'value']}
                  placeholder={this.t('login.username_tips')}
                  autofocus
                  onKeydown={withKeys(this.handleLogin, ['enter'])}
                />
              </NFormItem>
              <NFormItem
                label={this.t('login.password')}
                label-style={{ color: 'black' }}
                path='password'
              >
                <NInput
                  type='password'
                  size='large'
                  v-model={[this.loginForm.password, 'value']}
                  placeholder={this.t('login.password_tips')}
                  onKeydown={withKeys(this.handleLogin, ['enter'])}
                />
              </NFormItem>
            </NForm>
            <NButton round type='primary' onClick={this.handleLogin}>
              {this.t('login.signin')}
            </NButton>
          </div>
        </div>
      </div>
    )
  },
})

export default Login
