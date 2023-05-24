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

import { useRoute, useRouter } from 'vue-router'
import { defineComponent, toRefs, watch } from 'vue'
import { NButton, NForm, NFormItem, NSpace, NSpin } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useForm } from './use-form'
import { useEdit } from './use-edit'
import Card from '@/components/card'
import MonacoEditor from '@/components/monaco-editor'
import styles from '../index.module.scss'

export default defineComponent({
  name: 'ResourceEdit',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const componentName = route.name
    // fullname is now the id of resources
    const fullName = String(router.currentRoute.value.query.prefix || '')
    const tenantCode = String(router.currentRoute.value.query.tenantCode || '')

    const { state } = useForm()
    const { getResourceView, handleUpdateContent } = useEdit(state)

    const handleFileContent = () => {
      state.fileForm.content = resourceViewRef.state.value.content
      handleUpdateContent(fullName, tenantCode)
    }

    const handleReturn = () => {
      router.go(-1)
    }

    const resourceViewRef = getResourceView(fullName, tenantCode)
    watch(
      () => resourceViewRef.state.value.content,
      () => (state.fileForm.content = resourceViewRef.state.value.content)
    )

    return {
      componentName,
      resourceViewRef,
      handleReturn,
      handleFileContent,
      ...toRefs(state)
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Card title={t('resource.file.file_details')}>
        {this.resourceViewRef.isReady.value ? (
          <div class={styles['file-edit-content']}>
            <h2>
              <span>{this.resourceViewRef.state.value.alias}</span>
            </h2>
            <NForm
              rules={this.rules}
              ref='fileFormRef'
              class={styles['form-content']}
              disabled={this.componentName !== 'resource-file-edit'}
            >
              <NFormItem path='content'>
                <MonacoEditor
                  v-model={[this.resourceViewRef.state.value.content, 'value']}
                />
              </NFormItem>
              <NSpace>
                <NButton
                  type='info'
                  size='small'
                  text
                  style={{ marginRight: '15px' }}
                  onClick={this.handleReturn}
                  class='btn-cancel'
                >
                  {t('resource.file.return')}
                </NButton>
                {this.componentName === 'resource-file-edit' && (
                  <NButton
                    type='info'
                    size='small'
                    round
                    onClick={() => this.handleFileContent()}
                    class='btn-submit'
                  >
                    {t('resource.file.save')}
                  </NButton>
                )}
              </NSpace>
            </NForm>
          </div>
        ) : (
          <NSpace justify='center'>
            <NSpace vertical>
              <NSpin show={true} />
              <NSpace>
                <NButton
                  type='info'
                  size='small'
                  text
                  style={{ marginRight: '15px' }}
                  onClick={this.handleReturn}
                  class='btn-cancel'
                >
                  {t('resource.file.return')}
                </NButton>
              </NSpace>
            </NSpace>
          </NSpace>
        )}
      </Card>
    )
  }
})
