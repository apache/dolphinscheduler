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
  toRefs,
  watch,
  onMounted,
  ref,
  getCurrentInstance
} from 'vue'
import { NSelect, NInput, NCheckboxGroup, NSpace, NCheckbox } from 'naive-ui'
import { isFunction } from 'lodash'
import { useI18n } from 'vue-i18n'
import { useForm } from './use-form'
import { useDetail } from './use-detail'
import Modal from '@/components/modal'
import Form from '@/components/form'
import getElementByJson from '@/components/form/get-elements-by-json'
import type { IRecord, IFormRules, IFormItem } from './types'
import type { PropType, Ref } from 'vue'
import { stateType } from '@/common/common'

interface IElements extends Omit<Ref, 'value'> {
  value: IFormItem[]
}

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  currentRecord: {
    type: Object as PropType<IRecord>,
    default: {}
  }
}
const DetailModal = defineComponent({
  name: 'DetailModal',
  props,
  emits: ['cancel', 'update'],
  setup(props, ctx) {
    const { t } = useI18n()

    const rules = ref<IFormRules>({})
    const elements = ref<IFormItem[]>([]) as IElements
    const {
      meta,
      state,
      eventTypes,
      setDetail,
      initForm,
      resetForm,
      getFormValues,
      changePlugin
    } = useForm()

    const { status, createOrUpdate } = useDetail(getFormValues)

    const onCancel = () => {
      resetForm()
      rules.value = {}
      elements.value = []
      ctx.emit('cancel')
    }

    const onSubmit = async () => {
      await state.detailFormRef.validate()
      const res = await createOrUpdate(props.currentRecord, state.json)
      if (res) {
        onCancel()
        ctx.emit('update')
      }
    }
    const onChangePlugin = changePlugin

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.show,
      async () => {
        props.show && props.currentRecord && setDetail(props.currentRecord)
      }
    )
    watch(
      () => state.json,
      () => {
        if (!state.json?.length) return
        state.json.forEach((item) => {
          const mergedItem = isFunction(item) ? item() : item
          mergedItem.name = mergedItem.title
        })
        const { rules: fieldsRules, elements: fieldsElements } =
          getElementByJson(state.json, state.detailForm)
        rules.value = fieldsRules
        elements.value = fieldsElements
      }
    )

    onMounted(() => {
      initForm()
    })

    return {
      t,
      ...toRefs(state),
      ...toRefs(status),
      meta,
      rules,
      elements,
      eventTypes,
      onChangePlugin,
      onSubmit,
      onCancel,
      trim
    }
  },
  render(props: { currentRecord: IRecord }) {
    const {
      show,
      t,
      meta,
      rules,
      elements,
      detailForm,
      uiPlugins,
      eventTypes,
      pluginsLoading,
      loading,
      saving,
      onChangePlugin,
      onCancel,
      onSubmit
    } = this
    const { currentRecord } = props
    return (
      <Modal
        show={show}
        title={t(
          currentRecord?.id
            ? 'security.listener_instance.edit_listener_instance'
            : 'security.listener_instance.create_listener_instance'
        )}
        onConfirm={onSubmit}
        confirmLoading={saving || loading}
        onCancel={onCancel}
      >
        {{
          default: () => (
            <Form
              ref='detailFormRef'
              loading={loading || pluginsLoading}
              meta={{
                ...meta,
                rules: {
                  ...meta.rules,
                  ...rules
                },
                elements: [
                  {
                    path: 'instanceName',
                    label: t('security.listener_instance.instance_name'),
                    widget: (
                      <NInput
                        allowInput={this.trim}
                        v-model={[detailForm.instanceName, 'value']}
                        placeholder={t(
                          'security.listener_instance.instance_name_tips'
                        )}
                      />
                    )
                  },
                  {
                    path: 'listenerEventTypes',
                    label: t('security.listener_instance.listener_event_types'),
                    widget: (
                      <NCheckboxGroup 
                        disabled={!this.trim}
                        v-model={[detailForm.listenerEventTypes, 'value']}>
                        <NSpace style="display: flex;">
                          {
                            Object.values(
                              eventTypes
                            ).map((item)=>{
                              return <NCheckbox value={item.value} label={item.label} defaultChecked={true}/>
                            })
                          }
                        </NSpace>
                    </NCheckboxGroup>
                    )
                  },
                  {
                    path: 'pluginDefineId',
                    label: t('security.listener_instance.select_plugin'),
                    widget: (
                      <NSelect
                        v-model={[detailForm.pluginDefineId, 'value']}
                        options={uiPlugins}
                        disabled={!!currentRecord?.id}
                        placeholder={t(
                          'security.listener_instance.select_plugin_tips'
                        )}
                        on-update:value={onChangePlugin}
                      />
                    )
                  },
                  ...elements
                ]
              }}
              layout={{
                cols: 24
              }}
            />
          )
        }}
      </Modal>
    )
  }
})

export default DetailModal
