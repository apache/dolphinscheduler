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

import _ from 'lodash'
import { ref, reactive, SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  createResource,
  createUdfFunc,
  queryResourceList,
  updateUdfFunc
} from '@/service/modules/resources'
import { useAsyncState } from '@vueuse/core'

export function useModal(
  state: any,
  uploadState: any,
  ctx: SetupContext<('update:show' | 'updateList')[]>
) {
  const { t } = useI18n()

  const handleCreateFunc = async () => {
    submitRequest(
      async () =>
        await createUdfFunc({
          ...state.functionForm
        })
    )
  }

  const handleRenameFunc = async (id: number) => {
    submitRequest(async () => {
      await updateUdfFunc(
        {
          ...state.functionForm,
          id
        },
        id
      )
    })
  }

  const submitRequest = async (serviceHandle: any) => {
    await state.functionFormRef.validate()

    if (state.saving) return
    state.saving = true

    try {
      await serviceHandle()
      window.$message.success(t('resource.function.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
    } catch (err) {
      state.saving = false
    }
  }

  const variables = reactive({
    uploadShow: ref(false),
    udfResourceList: [],
    udfResourceDirList: []
  })

  const filterEmptyDirectory = (list: any) => {
    for (const item of list) {
      if (item.children) {
        filterEmptyDirectory(item.children)
      }
    }
    return list.filter(
      (n: any) =>
        (/\.jar$/.test(n.name) && n.children.length === 0) ||
        (!/\.jar$/.test(n.name) && n.children.length > 0)
    )
  }

  // filterJarFile
  const filterJarFile = (array: any) => {
    for (const item of array) {
      if (item.children) {
        item.children = filterJarFile(item.children)
      }
    }
    return array.filter((n: any) => !/\.jar$/.test(n.name))
  }

  // recursiveTree
  const recursiveTree = (item: any) => {
    // Recursive convenience tree structure
    item.forEach((item: any) => {
      item.children === '' ||
      item.children === undefined ||
      item.children === null ||
      item.children.length === 0
        ? delete item.children
        : recursiveTree(item.children)
    })
  }

  const getUdfList = () => {
    const { state } = useAsyncState(
      queryResourceList({ type: 'UDF', fullName: '' }).then((res: any) => {
        let item = res
        let item1 = _.cloneDeep(res)

        filterEmptyDirectory(item)
        item = filterEmptyDirectory(item)
        recursiveTree(item)
        recursiveTree(filterJarFile(item1))
        item1 = item1.filter((item: any) => {
          if (item.dirctory) {
            return item
          }
        })
        variables.udfResourceList = item
        variables.udfResourceDirList = item1
      }),
      {}
    )
    return state
  }

  const resetUploadForm = () => {
    uploadState.uploadForm.name = ''
    uploadState.uploadForm.file = ''
    uploadState.uploadForm.pid = -1
    uploadState.uploadForm.currentDir = '/'
    uploadState.uploadForm.description = ''
  }

  const handleUploadFile = () => {
    uploadState.uploadFormRef.validate(async (valid: any) => {
      if (!valid) {
        const formData = new FormData()
        formData.append('file', uploadState.uploadForm.file)
        formData.append('type', 'UDF')
        formData.append('name', uploadState.uploadForm.name)
        formData.append('pid', uploadState.uploadForm.pid)
        formData.append('currentDir', uploadState.uploadForm.currentDir)
        formData.append('description', uploadState.uploadForm.description)

        const res = await createResource(formData as any)
        window.$message.success(t('resource.function.success'))
        variables.uploadShow = false
        resetUploadForm()
        getUdfList()
        state.functionForm.resourceId = res.id
      }
    })
  }

  return {
    variables,
    getUdfList,
    handleCreateFunc,
    handleRenameFunc,
    handleUploadFile
  }
}
