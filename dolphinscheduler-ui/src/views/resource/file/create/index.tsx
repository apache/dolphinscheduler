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

import { defineComponent, getCurrentInstance, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NSelect, NButton } from 'naive-ui'
import { useI18n } from 'vue-i18n'

import Card from '@/components/card'
import MonacoEditor from '@/components/monaco-editor'
import { useCreate } from './use-create'
import { useForm } from './use-form'
import { fileTypeArr } from '@/common/common'

import styles from '../index.module.scss'

import type { Router } from 'vue-router'

export default defineComponent({
  name: 'ResourceFileCreate',
  setup() {
    const router: Router = useRouter()

    const { state } = useForm()
    const { handleCreateFile } = useCreate(state)

    const fileSuffixOptions = fileTypeArr.map((suffix) => ({
      key: suffix,
      label: suffix,
      value: suffix
    }))

    const handleFile = () => {
      handleCreateFile()
    }

    const handleReturn = () => {
      const { id } = router.currentRoute.value.params
      const name = id ? 'resource-file-subdirectory' : 'file'
      router.push({ name, params: { id } })
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    return {
      fileSuffixOptions,
      handleFile,
      handleReturn,
      ...toRefs(state),
      trim
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Card title={t('resource.file.file_details')}>
        <NForm
          rules={this.rules}
          ref='fileFormRef'
          class={styles['form-content']}
        >
          <NFormItem label={t('resource.file.file_name')} path='fileName'>
            <NInput
              allowInput={this.trim}
              v-model={[this.fileForm.fileName, 'value']}
              placeholder={t('resource.file.enter_name_tips')}
              style={{ width: '300px' }}
              class='input-file-name'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.file_format')} path='suffix'>
            <NSelect
              defaultValue={[this.fileForm.suffix]}
              v-model={[this.fileForm.suffix, 'value']}
              options={this.fileSuffixOptions}
              style={{ width: '100px' }}
              class='select-file-format'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.description')} path='description'>
            <NInput
              allowInput={this.trim}
              type='textarea'
              v-model={[this.fileForm.description, 'value']}
              placeholder={t('resource.file.enter_description_tips')}
              style={{ width: '430px' }}
              class='input-description'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.file_content')} path='content'>
            <div
              style={{
                width: '90%'
              }}
            >
              <MonacoEditor v-model={[this.fileForm.content, 'value']} />
            </div>
          </NFormItem>
          <div class={styles['file-edit-content']}>
            <div class={styles.submit}>
              <NButton
                type='info'
                size='small'
                round
                onClick={this.handleFile}
                class='btn-submit'
              >
                {t('resource.file.save')}
              </NButton>
              <NButton
                type='info'
                size='small'
                text
                style={{ marginLeft: '15px' }}
                onClick={this.handleReturn}
                class='btn-cancel'
              >
                {t('resource.file.return')}
              </NButton>
            </div>
          </div>
        </NForm>
      </Card>
    )
  }
})
