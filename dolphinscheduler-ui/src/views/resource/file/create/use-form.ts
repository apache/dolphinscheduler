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
import { reactive, ref, unref } from 'vue'
import type { FormRules } from 'naive-ui'

const defaultValue = () => ({
  pid: -1,
  type: 'FILE',
  suffix: 'sh',
  fileName: '',
  description: '',
  content: '',
  currentDir: '/'
})

export function useForm() {
  const { t } = useI18n()

  const resetForm = () => {
    state.fileForm = Object.assign(unref(state.fileForm), defaultValue())
  }

  const state = reactive({
    fileFormRef: ref(),
    fileForm: defaultValue(),
    rules: {
      fileName: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (state.fileForm.fileName === '') {
            return new Error(t('resource.file.enter_name_tips'))
          }
          if (state.fileForm.fileName.endsWith(`.${state.fileForm.suffix}`)) {
            return new Error(t('resource.file.duplicate_suffix_tips'))
          }
        }
      },
      suffix: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (state.fileForm.suffix === '') {
            return new Error(t('resource.file.enter_suffix_tips'))
          }
        }
      },
      content: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (state.fileForm.content === '') {
            return new Error(t('resource.file.enter_content_tips'))
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
