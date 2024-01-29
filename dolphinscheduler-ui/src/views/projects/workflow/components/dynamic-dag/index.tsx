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

import { defineComponent, ref } from 'vue'
import { DagSidebar } from './dag-sidebar'
import { DagCanvas } from './dag-canvas'
import { useDagStore } from '@/store/project/dynamic/dag'
import { NodeShape, NodeHeight, NodeWidth } from './dag-setting'
import { TaskForm } from './task'
import { queryDynamicTaskResource } from '@/service/modules/dynamic-dag'
import styles from './index.module.scss'

const DynamicDag = defineComponent({
  name: 'DynamicDag',
  setup() {
    const draggedTask = ref()
    const formData = ref()
    const showModal = ref(false)

    const handelDragstart = (task: any) => {
      draggedTask.value = task.name

      queryDynamicTaskResource(task.json).then((res: any) => {
        formData.value = res
      })
    }

    const handelDrop = (e: DragEvent) => {
      if (!draggedTask.value) return

      const shapes = useDagStore().getDagTasks

      shapes.push({
        id: String(shapes.length + 1),
        x: e.offsetX,
        y: e.offsetY,
        width: NodeWidth,
        height: NodeHeight,
        shape: NodeShape,
        label: draggedTask.value.name + String(shapes.length + 1),
        zIndex: 1,
        task: draggedTask.value.name
      })

      useDagStore().setDagTasks(shapes)

      showModal.value = true
    }

    return {
      draggedTask,
      formData,
      handelDragstart,
      handelDrop,
      showModal
    }
  },
  render() {
    return (
      <>
        <div class={styles['workflow-dag']}>
          <DagSidebar onDragstart={this.handelDragstart} />
          <DagCanvas onDrop={this.handelDrop} />
        </div>
        {this.draggedTask && this.formData && (
          <TaskForm
            task={this.draggedTask}
            formData={this.formData}
            showModal={this.showModal}
            onCancelModal={() => (this.showModal = false)}
            onConfirmModal={() => (this.showModal = false)}
          />
        )}
      </>
    )
  }
})

export { DynamicDag }
