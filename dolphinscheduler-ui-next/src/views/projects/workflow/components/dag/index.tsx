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

import type { Graph } from '@antv/x6'
import { defineComponent, ref, provide, PropType, toRef } from 'vue'
import DagToolbar from './dag-toolbar'
import DagCanvas from './dag-canvas'
import DagSidebar from './dag-sidebar'
import Styles from './dag.module.scss'
import DagAutoLayoutModal from './dag-auto-layout-modal'
import {
  useGraphAutoLayout,
  useGraphBackfill,
  useDagDragAndDrop,
  useTaskEdit,
  useBusinessMapper
} from './dag-hooks'
import { useThemeStore } from '@/store/theme/theme'
import VersionModal from '../../definition/components/version-modal'
import { WorkflowDefinition } from './types'
import DagSaveModal from './dag-save-modal'
import TaskModal from '@/views/projects/node/detail-modal'
import './x6-style.scss'

const props = {
  // If this prop is passed, it means from definition detail
  definition: {
    type: Object as PropType<WorkflowDefinition>,
    default: undefined
  },
  readonly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  projectCode: {
    type: Number as PropType<number>,
    default: 0
  }
}

export default defineComponent({
  name: 'workflow-dag',
  props,
  emits: ['refresh', 'save'],
  setup(props, context) {
    const theme = useThemeStore()

    // Whether the graph can be operated
    provide('readonly', toRef(props, 'readonly'))

    const graph = ref<Graph>()
    provide('graph', graph)

    // Auto layout modal
    const {
      visible: layoutVisible,
      toggle: layoutToggle,
      formValue,
      formRef,
      submit,
      cancel
    } = useGraphAutoLayout({ graph })

    // Edit task
    const {
      taskConfirm,
      taskModalVisible,
      currTask,
      taskCancel,
      appendTask,
      taskDefinitions
    } = useTaskEdit({ graph })

    const { onDragStart, onDrop } = useDagDragAndDrop({
      graph,
      readonly: toRef(props, 'readonly'),
      appendTask
    })

    // backfill
    useGraphBackfill({ graph, definition: toRef(props, 'definition') })

    // version modal
    const versionModalShow = ref(false)
    const versionToggle = (bool: boolean) => {
      if (typeof bool === 'boolean') {
        versionModalShow.value = bool
      } else {
        versionModalShow.value = !versionModalShow.value
      }
    }
    const refreshDetail = () => {
      context.emit('refresh')
      versionModalShow.value = false
    }

    // Save modal
    const saveModalShow = ref(false)
    const saveModelToggle = (bool: boolean) => {
      if (typeof bool === 'boolean') {
        saveModalShow.value = bool
      } else {
        saveModalShow.value = !versionModalShow.value
      }
    }
    const { getConnects, getLocations } = useBusinessMapper()
    const onSave = (saveForm: any) => {
      const edges = graph.value?.getEdges() || []
      const nodes = graph.value?.getNodes() || []
      const connects = getConnects(nodes, edges, taskDefinitions.value as any)
      const locations = getLocations(nodes)
      context.emit('save', {
        taskDefinitions: taskDefinitions.value,
        saveForm,
        connects,
        locations
      })
      saveModelToggle(false)
    }

    return () => (
      <div
        class={[
          Styles.dag,
          Styles[`dag-${theme.darkTheme ? 'dark' : 'light'}`]
        ]}
      >
        <DagToolbar
          layoutToggle={layoutToggle}
          definition={props.definition}
          onVersionToggle={versionToggle}
          onSaveModelToggle={saveModelToggle}
        />
        <div class={Styles.content}>
          <DagSidebar onDragStart={onDragStart} />
          <DagCanvas onDrop={onDrop} />
        </div>
        <DagAutoLayoutModal
          visible={layoutVisible.value}
          submit={submit}
          cancel={cancel}
          formValue={formValue}
          formRef={formRef}
        />
        {!!props.definition && (
          <VersionModal
            v-model:row={props.definition.processDefinition}
            v-model:show={versionModalShow.value}
            onUpdateList={refreshDetail}
          />
        )}
        <DagSaveModal v-model:show={saveModalShow.value} onSave={onSave} />
        <TaskModal
          show={taskModalVisible.value}
          projectCode={props.projectCode}
          taskDefinition={currTask.value}
          onSubmit={taskConfirm}
          onCancel={taskCancel}
        />
      </div>
    )
  }
})
