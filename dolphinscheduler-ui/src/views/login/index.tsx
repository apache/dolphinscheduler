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
  onMounted,
  toRefs,
  withKeys
} from 'vue'
import styles from './index.module.scss'
import {
  NInput,
  NButton,
  NSwitch,
  NForm,
  NFormItem,
  useMessage
} from 'naive-ui'
import { useForm } from './use-form'
import { useTranslate } from './use-translate'
import { useLogin } from './use-login'
import { useLocalesStore } from '@/store/locales/locales'
import { useThemeStore } from '@/store/theme/theme'
import cookies from 'js-cookie'
import { ssoLoginUrl } from '@/service/modules/login'

const login = defineComponent({
  name: 'login',
  setup() {
    window.$message = useMessage()

    const { state, t, locale } = useForm()
    const { handleChange } = useTranslate(locale)
    const { handleLogin } = useLogin(state)
    const localesStore = useLocalesStore()
    const themeStore = useThemeStore()

    if (themeStore.getTheme) {
      themeStore.setDarkTheme()
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    cookies.set('language', localesStore.getLocales, { path: '/' })

    onMounted(async () => {
      const ssoLoginUrlRes = await ssoLoginUrl()
      state.loginForm.ssoLoginUrl = ssoLoginUrlRes
      if (state.loginForm.ssoLoginUrl) {
        const url = new URL(window.location.href)
        const ssoState = url.searchParams.get('state')
        const ssoCode = url.searchParams.get('code')
        if (ssoState && ssoCode) {
          state.loginForm.userName = ssoState
          state.loginForm.userPassword = ssoCode
          handleLogin()
        }
      } else {
        state.loginForm.ssoLoginUrl = ''
      }
    })

    return {
      t,
      handleChange,
      handleLogin,
      ...toRefs(state),
      localesStore,
      trim
    }
  },
  render() {
    return (
      <div class={styles.container}>
        <div class={styles['language-switch']}>
          <NSwitch
            onUpdateValue={this.handleChange}
            default-value={this.localesStore.getLocales}
            checked-value='en_US'
            unchecked-value='zh_CN'
          >
            {{
              checked: () => 'en_US',
              unchecked: () => 'zh_CN'
            }}
          </NSwitch>
        </div>
        <div class={styles['login-model']}>
          <div class={styles.logo}>
            <div class={styles['logo-img']} />
          </div>
          <div
            class={styles['form-model']}
            v-show={this.loginForm.ssoLoginUrl.length === 0}
          >
            <NForm rules={this.rules} ref='loginFormRef'>
              <NFormItem
                label={this.t('login.userName')}
                label-style={{ color: 'black' }}
                path='userName'
              >
                <NInput
                  allowInput={this.trim}
                  class='input-user-name'
                  type='text'
                  size='large'
                  v-model={[this.loginForm.userName, 'value']}
                  placeholder={this.t('login.userName_tips')}
                  autofocus
                  onKeydown={withKeys(this.handleLogin, ['enter'])}
                />
              </NFormItem>
              <NFormItem
                label={this.t('login.userPassword')}
                label-style={{ color: 'black' }}
                path='userPassword'
              >
                <NInput
                  allowInput={this.trim}
                  class='input-password'
                  type='password'
                  size='large'
                  v-model={[this.loginForm.userPassword, 'value']}
                  placeholder={this.t('login.userPassword_tips')}
                  onKeydown={withKeys(this.handleLogin, ['enter'])}
                />
              </NFormItem>
            </NForm>
            <NButton
              class='btn-login'
              round
              type='info'
              disabled={
                !this.loginForm.userName || !this.loginForm.userPassword
              }
              style={{ width: '100%' }}
              onClick={this.handleLogin}
            >
              {this.t('login.login')}
            </NButton>
          </div>
          <div
            class={styles['form-model']}
            v-show={this.loginForm.ssoLoginUrl.length !== 0}
          >
            <a href={this.loginForm.ssoLoginUrl} style='text-decoration:none'>
              <NButton
                class='btn-login-sso'
                round
                type='info'
                style={{ width: '100%', marginTop: '30px' }}
                onClick={this.handleLogin}
              >
                {this.t('login.ssoLogin')}
              </NButton>
            </a>
          </div>
        </div>
      </div>
    )
  }
})

export default login
