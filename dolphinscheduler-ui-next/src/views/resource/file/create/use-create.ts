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
import { useFileStore } from '@/store/file/file'
import { onlineCreateResource } from '@/service/modules/resources'

export function useCreate(state: any) {
  const { t } = useI18n()
  const router: Router = useRouter()
  const fileStore = useFileStore()

  const handleCreateFile = () => {
    const pid = router.currentRoute.value.params.id || -1
    const currentDir = fileStore.getCurrentDir || '/'
    state.fileFormRef.validate(async (valid: any) => {
      if (!valid) {
        await onlineCreateResource({
          ...state.fileForm,
          ...{ pid, currentDir }
        })

        window.$message.success(t('resource.file.success'))
        const name = pid ? 'resource-file-subdirectory' : 'file'
        router.push({ name, params: { id: pid } })
      }
    })
  }

  return {
    handleCreateFile
  }
}
