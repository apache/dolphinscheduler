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

import { useRouter } from 'vue-router'
import { defineComponent, onMounted, ref, toRefs } from 'vue'
import { NButton, NForm, NFormItem } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'
import MonacoEditor from '@/components/monaco-editor'
import { useForm } from './use-form'
import { useEdit } from './use-edit'

import styles from '../index.module.scss'
import type { Router } from 'vue-router'

export default defineComponent({
  name: 'ResourceFileEdit',
  setup() {
    const router: Router = useRouter()

    const resourceViewRef = ref()
    const idRef = ref(Number(router.currentRoute.value.params.id))
    const codeEditorRef = ref()

    const { state } = useForm()
    const { getResourceView, handleUpdateContent } = useEdit(state)

    const handleFileContent = () => {
      state.fileForm.content = codeEditorRef.value?.getValue()
      handleUpdateContent(idRef.value)
    }

    const handleReturn = () => {
      router.go(-1)
    }

    onMounted(() => {
      resourceViewRef.value = getResourceView(idRef.value)
    })

    return {
      idRef,
      codeEditorRef,
      resourceViewRef,
      handleReturn,
      handleFileContent,
      ...toRefs(state),
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Card title={t('resource.file_details')}>
        <div class={styles['file-edit-content']}>
          <h2>
            <span>{this.resourceViewRef?.value.alias}</span>
          </h2>
          <NForm rules={this.rules} ref='fileFormRef'>
            <NFormItem path='content'>
              <div
                class={styles.cont}
                style={{
                  width: '90%',
                }}
              >
                <MonacoEditor
                  ref='codeEditorRef'
                  modelValue={this.resourceViewRef?.value.content}
                />
              </div>
            </NFormItem>
            <div class={styles['submit-c']}>
              <NButton
                type='info'
                size='small'
                text
                style={{ marginRight: '15px' }}
                onClick={this.handleReturn}
              >
                {t('resource.return')}
              </NButton>
              <NButton
                type='info'
                size='small'
                round
                onClick={() => this.handleFileContent()}
              >
                {t('resource.save')}
              </NButton>
            </div>
          </NForm>
        </div>
      </Card>
    )
  },
})
