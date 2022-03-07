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
  ref,
  reactive,
  watch,
  nextTick,
  provide,
  computed,
  onMounted
} from 'vue'
import { useI18n } from 'vue-i18n'
import { omit } from 'lodash'
import Modal from '@/components/modal'
import Detail from './detail'
import { formatModel } from './format-data'
import type { ITaskData, ITaskType } from './types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: Object as PropType<ITaskData>,
    default: { code: 0, taskType: 'SHELL', name: '' }
  },
  projectCode: {
    type: Number as PropType<number>,
    required: true
  },
  readonly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  from: {
    type: Number as PropType<number>,
    default: 0
  }
}

const NodeDetailModal = defineComponent({
  name: 'NodeDetailModal',
  props,
  emits: ['cancel', 'submit'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const detailRef = ref()
    const state = reactive({
      saving: false,
      linkEventShowRef: ref(),
      linkEventTextRef: ref(),
      linkUrlRef: ref()
    })

    const onConfirm = async () => {
      await detailRef.value.value.validate()
      emit('submit', { data: detailRef.value.value.getValues() })
    }
    const onCancel = () => {
      emit('cancel')
    }

    const onJumpLink = () => {
      // TODO: onJumpLink
    }

    const getLinkEventText = (status: boolean, text: string, url: 'string') => {
      state.linkEventShowRef = status
      state.linkEventTextRef = text
      state.linkUrlRef = url
    }

    const onTaskTypeChange = (taskType: ITaskType) => {
      props.data.taskType = taskType
    }

    provide(
      'data',
      computed(() => ({
        projectCode: props.projectCode,
        data: props.data,
        from: props.from,
        readonly: props.readonly
      }))
    )

    watch(
        () => [props.show, props.data],
        async () => {
          if (!props.show) return
          await nextTick()
          detailRef.value.value.setValues(formatModel(props.data))
        }
    )

    return () => (
      <Modal
        show={props.show}
        title={`${t('project.node.current_node_settings')}`}
        onConfirm={onConfirm}
        confirmLoading={false}
        confirmDisabled={props.readonly}
        onCancel={onCancel}
        linkEventShow={state.linkEventShowRef}
        linkEventText={state.linkEventTextRef}
        onJumpLink={onJumpLink}
      >
        <Detail
          ref={detailRef}
          onTaskTypeChange={onTaskTypeChange}
          key={props.data.taskType}
        />
      </Modal>
    )
  }
})

export default NodeDetailModal
