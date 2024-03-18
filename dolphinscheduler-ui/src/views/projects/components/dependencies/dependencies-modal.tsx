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

import { defineComponent, PropType, h, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { NEllipsis, NModal, NSpace } from 'naive-ui'
import { IDefinitionData } from '@/views/projects/workflow/definition/types'
import ButtonLink from '@/components/button-link'

const props = {
  row: {
    type: Object as PropType<IDefinitionData>,
    default: {},
    required: false
  },
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  required: {
    type: Boolean as PropType<boolean>,
    default: true
  },
  taskLinks: {
    type: Array,
    default: []
  },
  content: {
    type: String,
    default: ''
  }
}

export default defineComponent({
  name: 'dependenciesConfirm',
  props,
  emits: ['update:show', 'update:row', 'confirm'],
  setup(props, ctx) {
    const { t } = useI18n()

    const showRef = ref(props.show)

    const confirmToHandle = () => {
      ctx.emit('confirm')
    }

    const cancelToHandle = () => {
      ctx.emit('update:show', showRef)
    }

    const renderDownstreamDependencies = () => {
      return h(
        <NSpace vertical>
          <div>{props.content}</div>
          <div>{t('project.workflow.warning_dependencies')}</div>
          {props.taskLinks.map((item: any) => {
            return (
              <ButtonLink onClick={item.action} disabled={false}>
                {{
                  default: () =>
                    h(
                      NEllipsis,
                      {
                        style: 'max-width: 350px;line-height: 1.5'
                      },
                      () => item.text
                    )
                }}
              </ButtonLink>
            )
          })}
        </NSpace>
      )
    }

    watch(
      () => props.show,
      () => {
        showRef.value = props.show
      }
    )

    return {
      renderDownstreamDependencies,
      confirmToHandle,
      cancelToHandle,
      showRef
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <NModal
        v-model:show={this.showRef}
        preset={'dialog'}
        type={this.$props.required ? 'error' : 'warning'}
        title={t('project.workflow.warning_dependent_tasks_title')}
        positiveText={this.$props.required ? '' : t('project.workflow.confirm')}
        negativeText={t('project.workflow.cancel')}
        maskClosable={false}
        onNegativeClick={this.cancelToHandle}
        onPositiveClick={this.confirmToHandle}
        onClose={this.cancelToHandle}
      >
        {{
          default: () => this.renderDownstreamDependencies()
        }}
      </NModal>
    )
  }
})
