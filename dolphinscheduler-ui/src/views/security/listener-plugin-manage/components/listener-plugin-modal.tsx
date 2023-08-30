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

import {
  defineComponent,
  getCurrentInstance,
  PropType,
  toRefs,
  watch
} from 'vue'
import Modal from '@/components/modal'
import { NButton, NForm, NFormItem, NInput, NSelect, NUpload } from 'naive-ui'
import { useModalData } from './use-modalData'
import { useI18n } from 'vue-i18n'

const ListenerPluginModal = defineComponent({
  name: 'tenant-modal',
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false
    },
    statusRef: {
      type: Number as PropType<number>,
      default: 0
    },
    row: {
      type: Object as PropType<any>,
      default: {}
    }
  },
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, handleValidate } = useModalData(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.classPath = ''
        variables.model.pluginJar = ''
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const customRequest = ({ file }: any) => {
      variables.model.pluginJar = file.file
      variables.listenerPluginFormRef.validate()
    }
  
    // watch(
    //   () => props.showModalRef,
    //   () => {
    //     props.showModalRef && getListData(props.statusRef)
    //   }
    // )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.classPath = ''
          variables.model.pluginJar = ''
        } else {
          variables.model.id = props.row.id
          variables.model.classPath = props.row.pluginClassName
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.classPath = props.row.pluginClassName
      }
    )

    return { customRequest, t, ...toRefs(variables), cancelModal, confirmModal, trim }
  },
  render() {
    const { t } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.listener_plugin.register_plugin')
              : t('security.listener_plugin.update_plugin')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='listenerPluginFormRef'
                require-mark-placement='left'
                size='small'
                style="{ maxWidth: '240px' }"
              >
                <NFormItem
                  label={t('security.listener_plugin.plugin_class_name')}
                  path='classPath'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-class-name'
                    disabled={this.statusRef === 1}
                    placeholder={t('security.listener_plugin.class_path_tips')}
                    v-model={[this.model.classPath, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.listener_plugin.plugin_file_name')}
                  path='pluginJar'
                >
                  <NUpload
                    v-model={[this.model.pluginJar, 'value']}
                    class='btn-upload'
                    max={1}
                    accept='.jar'
                    customRequest={this.customRequest}
                  >
                    <NButton>{t('security.listener_plugin.upload_file')}</NButton>
                  </NUpload>
                </NFormItem>
              </NForm>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default ListenerPluginModal
