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
import { NSelect, NInput, NSwitch, NRadioGroup, NSpace, NRadio } from 'naive-ui'
import { isFunction } from 'lodash'
import { useI18n } from 'vue-i18n'
import { useForm } from './use-form'
import { useDetail } from './use-detail'
import Modal from '@/components/modal'
import Form from '@/components/form'
import getElementByJson from '@/components/form/get-elements-by-json'
import type { IRecord, IFormRules, IFormItem } from './types'
import type { PropType, Ref } from 'vue'

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
    const warningTypeSpan = ref(24)

    const {
      meta,
      state,
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
      () => state.detailForm.instanceType,
      () => warningTypeSpan.value = state.detailForm.instanceType === 'GLOBAL' ? 0 : 24
    )
    watch(
      () => state.json,
      () => {
        if (!state.json?.length) return
        state.json.forEach((item) => {
          const mergedItem = isFunction(item) ? item() : item
          mergedItem.name = t(
            'security.alarm_instance' + '.' + mergedItem.field
          )
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
      warningTypeSpan,
      rules,
      elements,
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
      warningTypeSpan,
      rules,
      elements,
      detailForm,
      uiPlugins,
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
            ? 'security.alarm_instance.edit_alarm_instance'
            : 'security.alarm_instance.create_alarm_instance'
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
                    label: t('security.alarm_instance.alarm_instance_name'),
                    widget: (
                      <NInput
                        allowInput={this.trim}
                        v-model={[detailForm.instanceName, 'value']}
                        placeholder={t(
                          'security.alarm_instance.alarm_instance_name_tips'
                        )}
                      />
                    )
                  },
                  {
                    path: 'instanceType',
                    label: t('security.alarm_instance.is_global_instance'),
                    widget: (
                      <NSwitch
                      checkedValue={'GLOBAL'}
                      uncheckedValue={'NORMAL'}
                      disabled={!!currentRecord?.id}
                      v-model:value={detailForm.instanceType}
                    />
                    )
                  },
                  {
                    path: 'warningType',
                    label: t('security.alarm_instance.WarningType'),
                    span: warningTypeSpan,
                    widget: (
                      <NRadioGroup v-model:value={detailForm.warningType}>
                      <NSpace>
                        <NRadio value={'SUCCESS'}>
                          {"success"}
                        </NRadio>
                        <NRadio value={'FAILURE'} >
                          {"failure"}
                        </NRadio>
                        <NRadio value={'ALL'} >
                          {"all"}
                        </NRadio>
                      </NSpace>
                    </NRadioGroup>
                    )
                  },
                  {
                    path: 'pluginDefineId',
                    label: t('security.alarm_instance.select_plugin'),
                    widget: (
                      <NSelect
                        v-model={[detailForm.pluginDefineId, 'value']}
                        options={uiPlugins}
                        disabled={!!currentRecord?.id}
                        placeholder={t(
                          'security.alarm_instance.select_plugin_tips'
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
