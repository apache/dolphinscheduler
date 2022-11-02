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

import { defineComponent, reactive } from 'vue'
import { DagSidebar } from './dag-sidebar'
import { DagCanvas } from './dag-canvas'
import { useDagStore } from '@/store/project/dynamic/dag'
import styles from './index.module.scss'

const DynamicDag = defineComponent({
  name: 'DynamicDag',
  setup() {
    const dragged = reactive({
      x: 0,
      y: 0,
      task: ''
    })

    const handelDragstart = (e: DragEvent, task: string) => {
      dragged.x = e.offsetX
      dragged.y = e.offsetY
      dragged.task = task
    }

    const handelDrop = (e: DragEvent) => {
      if (!dragged.task) return

      dragged.x = e.offsetX
      dragged.y = e.offsetY

      const shapes = useDagStore().getDagTasks
      if (shapes) {
        shapes.push(dragged)
      }
      useDagStore().setDagTasks(shapes)
    }

    return {
      handelDragstart,
      handelDrop
    }
  },
  render() {
    return (
      <div class={styles['workflow-dag']}>
        <DagSidebar onDragstart={this.handelDragstart} />
        <DagCanvas onDrop={this.handelDrop} />
      </div>
    )
  }
})

export { DynamicDag }