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
  PropType,
  toRefs,
  onMounted,
  ref,
  computed
} from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import { NForm, NFormItem, NSelect } from 'naive-ui'
import { ProjectList } from '@/service/modules/projects/types'
import { queryProjectCreatedAndAuthorizedByUser } from '@/service/modules/projects'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  codes: {
    type: Array as PropType<Array<string>>,
    default: []
  }
}

export default defineComponent({
  name: 'workflowDefinitionCopy',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const { copyState } = useForm()
    const { handleBatchCopyDefinition } = useModal(copyState, ctx)
    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleCopy = () => {
      handleBatchCopyDefinition(props.codes)
    }

    const projects = ref<ProjectList[]>([])
    const projectOptions = computed(() => {
      return projects.value.map((t) => ({
        label: t.name,
        value: t.code
      }))
    })

    onMounted(() => {
      queryProjectCreatedAndAuthorizedByUser().then(
        (res: Array<ProjectList>) => {
          projects.value = res
        }
      )
    })

    return {
      hideModal,
      handleCopy,
      projectOptions,
      ...toRefs(copyState)
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={t('project.workflow.related_items')}
        onCancel={this.hideModal}
        onConfirm={this.handleCopy}
        confirmLoading={this.saving}
      >
        <NForm rules={this.copyRules} ref='copyFormRef'>
          <NFormItem
            label={t('project.workflow.project_name')}
            path='projectCode'
          >
            <NSelect
              options={this.projectOptions}
              v-model:value={this.copyForm.projectCode}
              placeholder={t('project.workflow.project_tips')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
