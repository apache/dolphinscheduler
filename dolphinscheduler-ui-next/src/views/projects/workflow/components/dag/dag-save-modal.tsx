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

import { defineComponent, PropType, ref, computed, onMounted } from 'vue'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import {
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSwitch,
  NInputNumber,
  NDynamicInput
} from 'naive-ui'
import { queryTenantList } from '@/service/modules/tenants'
import { SaveForm } from './types'
import './x6-style.scss'

const props = {
  visible: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

interface Tenant {
  tenantCode: string
  id: number
}

export default defineComponent({
  name: 'dag-save-modal',
  props,
  emits: ['update:show', 'save'],
  setup(props, context) {
    const { t } = useI18n()

    const tenants = ref<Tenant[]>([])
    const tenantsDropdown = computed(() => {
      if (tenants.value) {
        return tenants.value
          .map((t) => ({
            label: t.tenantCode,
            value: t.tenantCode
          }))
          .concat({ label: 'default', value: 'default' })
      }
      return []
    })
    onMounted(() => {
      queryTenantList().then((res: any) => {
        tenants.value = res
      })
    })

    const formValue = ref<SaveForm>({
      name: '',
      description: '',
      tenantCode: 'default',
      timeoutFlag: false,
      timeout: 0,
      globalParams: []
    })
    const formRef = ref()
    const rule = {
      name: {
        required: true
      }
    }
    const onSubmit = () => {
      context.emit('save', formValue.value)
    }
    const onCancel = () => {
      context.emit('update:show', false)
    }

    return () => (
      <Modal
        show={props.visible}
        title={t('project.dag.basic_info')}
        onConfirm={onSubmit}
        onCancel={onCancel}
        autoFocus={false}
      >
        <NForm
          label-width='100'
          model={formValue.value}
          rules={rule}
          size='medium'
          label-placement='left'
          ref={formRef}
        >
          <NFormItem label={t('project.dag.workflow_name')} path='name'>
            <NInput v-model:value={formValue.value.name} />
          </NFormItem>
          <NFormItem label={t('project.dag.description')} path='description'>
            <NInput
              type='textarea'
              v-model:value={formValue.value.description}
            />
          </NFormItem>
          <NFormItem label={t('project.dag.tenant')} path='tenantCode'>
            <NSelect
              options={tenantsDropdown.value}
              v-model:value={formValue.value.tenantCode}
            />
          </NFormItem>
          <NFormItem label={t('project.dag.timeout_alert')} path='timeoutFlag'>
            <NSwitch v-model:value={formValue.value.timeoutFlag} />
          </NFormItem>
          {formValue.value.timeoutFlag && (
            <NFormItem label=' ' path='timeout'>
              <NInputNumber
                v-model:value={formValue.value.timeout}
                show-button={false}
                min={0}
                v-slots={{
                  suffix: () => '分'
                }}
              ></NInputNumber>
            </NFormItem>
          )}
          <NFormItem
            label={t('project.dag.global_variables')}
            path='globalParams'
          >
            <NDynamicInput
              v-model:value={formValue.value.globalParams}
              preset='pair'
              key-placeholder={t('project.dag.key')}
              value-placeholder={t('project.dag.value')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
