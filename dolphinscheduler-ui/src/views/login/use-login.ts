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

import { useRouter,useRoute } from 'vue-router'
import { clearCookie, getOauth2Provider, login } from '@/service/modules/login'
import { getUserInfo } from '@/service/modules/users'
import { useUserStore } from '@/store/user/user'
import type { Router } from 'vue-router'
import type { LoginRes, OAuth2Provider } from '@/service/modules/login/types'
import type { UserInfoRes } from '@/service/modules/users/types'
import { useRouteStore } from '@/store/route/route'
import { useTimezoneStore } from '@/store/timezone/timezone'
import cookies from 'js-cookie'
import { queryBaseDir } from '@/service/modules/resources'
import { ref } from 'vue'

export function useLogin(state: any) {
  const router: Router = useRouter()
  const userStore = useUserStore()
  const routeStore = useRouteStore()
  const timezoneStore = useTimezoneStore()
  const route = useRoute()
  const handleLogin = () => {
    state.loginFormRef.validate(async (valid: any) => {
      if (!valid) {
        const loginRes: LoginRes = await login({ ...state.loginForm })
        await userStore.setSessionId(loginRes.sessionId)
        await userStore.setSecurityConfigType(loginRes.securityConfigType)
        cookies.set('sessionId', loginRes.sessionId, { path: '/' })

        const userInfoRes: UserInfoRes = await getUserInfo()
        await userStore.setUserInfo(userInfoRes)

        const baseResDir = await queryBaseDir({
          type: 'FILE'
        })
        const baseUdfDir = await queryBaseDir({
          type: 'UDF'
        })
        await userStore.setBaseResDir(baseResDir)
        await userStore.setBaseUdfDir(baseUdfDir)

        const timezone = userInfoRes.timeZone ? userInfoRes.timeZone : 'UTC'
        await timezoneStore.setTimezone(timezone)

        const path = routeStore.lastRoute

        router.push({ path: path || 'home' })
      }
    })
  }



  const handleGetOAuth2Provider = () => {
    getOauth2Provider().then((res: Array<OAuth2Provider> | []) => {
      oauth2Providers.value = res
    })
  }

  const oauth2Providers = ref<Array<OAuth2Provider> | []>([])

  const gotoOAuth2Page = async (oauth2Provider: OAuth2Provider) => {
    await clearCookie()
    window.location.href = `${oauth2Provider.authorizationUri}?client_id=${oauth2Provider.clientId}` +
      `&response_type=code&redirect_uri=${oauth2Provider.redirectUri}?provider=${oauth2Provider.provider}`
  }

  const handleRedirect = async () => {
    const authType = route.query.authType
    if (authType && authType === 'oauth2') {
      const sessionId = route.query.sessionId
      if (sessionId) {
        cookies.set('sessionId', String(sessionId), { path: '/' })
        const userInfoRes: UserInfoRes = await getUserInfo()
        await userStore.setUserInfo(userInfoRes)
        const timezone = userInfoRes.timeZone ? userInfoRes.timeZone : 'UTC'
        await timezoneStore.setTimezone(timezone)
        router.push('home')
      }
      const error = route.query.error
      if (error) {
        window.$message.error(error)
      }
    }
  }

  return {
    handleLogin,
    handleGetOAuth2Provider,
    gotoOAuth2Page,
    oauth2Providers,
    handleRedirect
  }
}
