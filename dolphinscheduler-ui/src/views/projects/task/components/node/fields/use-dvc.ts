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
import type { IJsonItem } from '../types'
import { watch, ref } from 'vue'

export const DVC_TASK_TYPE = [
  {
    label: 'Upload',
    value: 'Upload'
  },
  {
    label: 'Download',
    value: 'Download'
  },
  {
    label: 'Init DVC',
    value: 'Init DVC'
  }
]

export function useDvc(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const dvcLoadSaveDataPathSpan = ref(0)
  const dvcDataLocationSpan = ref(0)
  const dvcVersionSpan = ref(0)
  const dvcMessageSpan = ref(0)
  const dvcStoreUrlSpan = ref(0)

  const setFlag = () => {
    model.isUpload = model.dvcTaskType === 'Upload'
    model.isDownload = model.dvcTaskType === 'Download'
    model.isInit = model.dvcTaskType === 'Init DVC'
  }

  const resetData = () => {
    dvcLoadSaveDataPathSpan.value = model.isUpload || model.isDownload ? 24 : 0
    dvcDataLocationSpan.value = model.isUpload || model.isDownload ? 24 : 0
    dvcVersionSpan.value = model.isUpload || model.isDownload ? 24 : 0
    dvcMessageSpan.value = model.isUpload ? 24 : 0
    dvcStoreUrlSpan.value = model.isInit ? 24 : 0
  }

  watch(
    () => [model.dvcTaskType],
    () => {
      setFlag()
      resetData()
    }
  )
  setFlag()
  resetData()

  return [
    {
      type: 'select',
      field: 'dvcTaskType',
      name: t('project.node.dvc_task_type'),
      span: 12,
      options: DVC_TASK_TYPE
    },
    {
      type: 'input',
      field: 'dvcRepository',
      name: t('project.node.dvc_repository'),
      span: 24,
      props: {
        placeholder: t('project.node.dvc_repository_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.dvc_empty_tips')
      }
    },
    {
      type: 'input',
      field: 'dvcDataLocation',
      name: t('project.node.dvc_data_location'),
      props: { placeholder: 'dvcDataLocation' },
      span: dvcDataLocationSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if ((model.isUpload || model.isDownload) && !value) {
            return new Error(t('project.node.dvc_empty_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'dvcLoadSaveDataPath',
      name: t('project.node.dvc_load_save_data_path'),
      span: dvcLoadSaveDataPathSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if ((model.isUpload || model.isDownload) && !value) {
            return new Error(t('project.node.dvc_empty_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'dvcVersion',
      name: t('project.node.dvc_version'),
      span: dvcVersionSpan,
      props: {
        placeholder: t('project.node.dvc_version_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if ((model.isUpload || model.isDownload) && !value) {
            return new Error(t('project.node.dvc_empty_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'dvcMessage',
      name: t('project.node.dvc_message'),
      span: dvcMessageSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (model.isUpload && !value) {
            return new Error(t('project.node.dvc_empty_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'dvcStoreUrl',
      name: t('project.node.dvc_store_url'),
      span: dvcStoreUrlSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!model.isInit && value) {
            return new Error(t('project.node.dvc_empty_tips'))
          }
        }
      }
    }
  ]
}
