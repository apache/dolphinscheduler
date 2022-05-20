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

import { defineComponent, PropType, toRefs, onMounted, ref, Ref } from 'vue'
import { NForm, NFormItem, NInput, NSelect, NInputNumber } from 'naive-ui'
import { useForm } from '../use-form'
import Modal from '@/components/modal'
import { createTaskGroup, updateTaskGroup } from '@/service/modules/task-group'
import { queryAllProjectList } from '@/service/modules/projects'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: Object as PropType<any>
  },
  status: {
    type: Number as PropType<number>,
    default: 0
  }
}

const FormModal = defineComponent({
  name: 'FormModal',
  props,
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    const { state, t } = useForm()
    const projectOptions: Ref<Array<SelectMixedOption>> = ref([])

    onMounted(() => {
      queryAllProjectList().then((res: any[]) => {
        res.map((item) => {
          const option: SelectMixedOption = {
            label: item.name,
            value: item.code
          }
          projectOptions.value.push(option)
        })
      })
      if (props.status === 1) {
        state.formData.id = props.data.id
        state.formData.name = props.data.name
        state.formData.projectCode = props.data.projectCode
        state.formData.groupSize = String(props.data.groupSize)
        state.formData.status = props.data.status
        state.formData.description = props.data.description
      } else {
        state.formData.groupSize = '10'
      }
    })

    const onConfirm = async () => {
      if (state.saving) return
      state.saving = true
      try {
        props.status === 1
          ? await updateTaskGroup(state.formData)
          : await createTaskGroup(state.formData)
        state.saving = false
        emit('confirm')
      } catch (err) {
        state.saving = false
      }
    }

    const onCancel = () => {
      state.formData.projectCode = ''
      state.formData.description = ''
      emit('cancel')
    }

    return { ...toRefs(state), t, onConfirm, onCancel, projectOptions }
  },
  render() {
    const { t, onConfirm, onCancel, show, status, projectOptions } = this
    return (
      <Modal
        title={
          status === 0
            ? t('resource.task_group_option.create')
            : t('resource.task_group_option.edit')
        }
        show={show}
        onConfirm={onConfirm}
        onCancel={onCancel}
        confirmDisabled={
          !this.formData.name ||
          !this.formData.groupSize ||
          !this.formData.description
        }
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='formRef'>
          <NFormItem label={t('resource.task_group_option.name')} path='name'>
            <NInput
              v-model={[this.formData.name, 'value']}
              placeholder={t('resource.task_group_option.please_enter_name')}
            />
          </NFormItem>
          {this.status === 0 && (
            <NFormItem
              label={t('resource.task_group_option.project_name')}
              path='projectCode'
            >
              <NSelect
                options={projectOptions}
                v-model:value={this.formData.projectCode}
                placeholder={t(
                  'resource.task_group_option.please_select_project'
                )}
              />
            </NFormItem>
          )}
          <NFormItem
            label={t('resource.task_group_option.resource_pool_size')}
            path='groupSize'
          >
            <NInputNumber
              v-model:value={this.formData.groupSize}
              style={{width: '100%'}}
              min={1}
              placeholder={t(
                'resource.task_group_option.please_enter_resource_pool_size'
              )}
            />
          </NFormItem>
          <NFormItem
            label={t('resource.task_group_option.desc')}
            path='description'
          >
            <NInput
              v-model={[this.formData.description, 'value']}
              type='textarea'
              placeholder={t('resource.task_group_option.please_enter_desc')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default FormModal
