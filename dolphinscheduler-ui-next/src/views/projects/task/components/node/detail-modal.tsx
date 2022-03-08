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
  h
} from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import Detail from './detail'
import { formatModel } from './format-data'
import type { ITaskData, ITaskType } from './types'
import { HistoryOutlined, ProfileOutlined } from '@vicons/antd'
import {NIcon} from "naive-ui";
import {Router, useRouter} from "vue-router";
import {IWorkflowTaskInstance} from "@/views/projects/workflow/components/dag/types";


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
  },
  processInstance: {
    type: Object as PropType<any>
  },
  taskInstance: {
    type: Object as PropType<IWorkflowTaskInstance>
  }
}

const NodeDetailModal = defineComponent({
  name: 'NodeDetailModal',
  props,
  emits: ['cancel', 'submit', 'viewLog'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const router: Router = useRouter()
    const renderIcon = (icon: any) => {
      return () => h(NIcon, null, { default: () => h(icon) })
    }
    const detailRef = ref()

    const onConfirm = async () => {
      await detailRef.value.value.validate()
      emit('submit', { data: detailRef.value.value.getValues() })
    }
    const onCancel = () => {
      emit('cancel')
    }

    const headerLinks = ref([] as any)

    const handleViewLog = () => {
      if (props.taskInstance) {
        emit('viewLog', props.taskInstance.id, props.taskInstance.taskType)
      }
    }

    const initHeaderLinks = (processInstance: any) => {
      headerLinks.value = [
          {
            text: t('project.node.view_history'),
            show: true,
            action: () => {
              router.push({ name: 'task-instance', params: { processInstanceId: processInstance.id } })
            },
            icon: renderIcon(HistoryOutlined)
          },
          {
            text: t('project.node.view_log'),
            show: props.taskInstance? true:false,
            action: () => {
              handleViewLog()
            },
            icon: renderIcon(ProfileOutlined)
          }
        ]
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
          if (props.processInstance) {
            initHeaderLinks(props.processInstance)
          }
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
        headerLinks={headerLinks}
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
