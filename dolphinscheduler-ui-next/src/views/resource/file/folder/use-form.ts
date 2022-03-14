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

import { reactive, ref, unref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormRules } from 'naive-ui'

const defaultValue = () => ({
  pid: -1,
  type: 'FILE',
  name: '',
  description: '',
  currentDir: '/'
})

export function useForm() {
  const { t } = useI18n()

  const resetForm = () => {
    state.folderForm = Object.assign(unref(state.folderForm), defaultValue())
  }

  const state = reactive({
    folderFormRef: ref(),
    folderForm: defaultValue(),
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (state.folderForm.name === '') {
            return new Error(t('resource.file.enter_name_tips'))
          }
        }
      }
    } as FormRules
  })

  return {
    state,
    resetForm
  }
}
