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

import { useI18n } from 'vue-i18n'
import type { Router } from 'vue-router'
import { useRouter } from 'vue-router'
import { useAsyncState } from '@vueuse/core'
import {
  updateResourceContent,
  viewResource
} from '@/service/modules/resources'
import { defineStore } from 'pinia'

export function useEdit(state: any) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const getResourceView = (fullName: string, tenantCode: string) => {
    const params = {
      skipLineNum: 0,
      limit: 3000,
      fullName: fullName,
      tenantCode: tenantCode
    }
    return useAsyncState(viewResource(params), {
      alias: '',
      content: ''
    })
  }

  const handleUpdateContent = (fullName: string, tenantCode: string) => {
    state.fileFormRef.validate(async (valid: any) => {
      if (!valid) {
        await updateResourceContent({
          ...state.fileForm,
          tenantCode: tenantCode,
          fullName: fullName
        })

        window.$message.success(t('resource.file.success'))
        router.go(-1)
      }
    })
  }

  return {
    getResourceView,
    handleUpdateContent
  }
}

export const useIsDetailPageStore = defineStore("isDetailPage", {
  state:() => {
    return {
      isDetailPage:false
    }
  },
  getters: {
    getIsDetailPage(): boolean {
      return this.isDetailPage
    }
  },
  actions: {
    setIsDetailPage(isDetailPage: boolean) {
      this.isDetailPage = isDetailPage
    }
  }
})

export const isEmpty = (string: any): boolean => {
    if(string === '' || string === undefined || string === null){
        return true
    }else{
        return false
    }
}
