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

import { SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { useFileStore } from '@/store/file/file'
import {
  createDirectory,
  createResource,
  updateResource
} from '@/service/modules/resources'

export function useModal(
  state: any,
  ctx: SetupContext<('update:show' | 'updateList')[]>
) {
  const { t } = useI18n()
  const router: Router = useRouter()
  const fileStore = useFileStore()

  const handleCreateResource = async () => {
    const pid = router.currentRoute.value.params.id || -1
    const currentDir = pid === -1 ? '/' : fileStore.getCurrentDir || '/'

    submitRequest(
      async () =>
        await createDirectory({
          ...state.folderForm,
          ...{ pid, currentDir }
        })
    )
  }

  const handleRenameResource = async (id: number) => {
    submitRequest(async () => {
      await updateResource(
        {
          ...state.folderForm,
          ...{ id }
        },
        id
      )
    })
  }

  const submitRequest = (serviceHandle: any) => {
    state.folderFormRef.validate(async (valid: any) => {
      if (!valid) {
        try {
          await serviceHandle()
          window.$message.success(t('resource.udf.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  const resetUploadForm = () => {
    state.uploadForm.name = ''
    state.uploadForm.file = ''
    state.uploadForm.description = ''
  }

  const handleUploadFile = () => {
    state.uploadFormRef.validate(async (valid: any) => {
      const pid = router.currentRoute.value.params.id || -1
      const currentDir = pid === -1 ? '/' : fileStore.getCurrentDir || '/'
      if (!valid) {
        const formData = new FormData()
        formData.append('file', state.uploadForm.file)
        formData.append('type', 'UDF')
        formData.append('name', state.uploadForm.name)
        formData.append('pid', String(pid))
        formData.append('currentDir', currentDir)
        formData.append('description', state.uploadForm.description)

        try {
          await createResource(formData as any)
          window.$message.success(t('resource.udf.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
          resetUploadForm()
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  return {
    handleCreateResource,
    handleRenameResource,
    handleUploadFile
  }
}
