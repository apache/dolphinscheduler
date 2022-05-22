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
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { useAsyncState } from '@vueuse/core'
import {
  viewResource,
  updateResourceContent
} from '@/service/modules/resources'

export function useEdit(state: any) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const getResourceView = (id: number) => {
    const params = {
      skipLineNum: 0,
      limit: 3000
    }
    const { state } = useAsyncState(viewResource(params, id), {
      alias: '',
      content: ''
    })
    return state
  }

  const handleUpdateContent = (id: number) => {
    state.fileFormRef.validate(async (valid: any) => {
      if (!valid) {
        await updateResourceContent(
          {
            ...state.fileForm
          },
          id
        )

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
