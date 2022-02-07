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

import type { PropType, Ref } from 'vue'
import type { Dragged } from './index'
import { defineComponent, ref, inject } from 'vue'
import { ALL_TASK_TYPES } from '../../../task/constants/task-type'
import { useSidebarDrag } from './dag-hooks'
import Styles from './dag.module.scss'

const props = {
  dragged: {
    type: Object as PropType<Ref<Dragged>>,
    default: ref({
      x: 0,
      y: 0,
      type: ''
    })
  }
}

export default defineComponent({
  name: 'workflow-dag-sidebar',
  props,
  setup(props) {
    const readonly = inject('readonly', ref(false))
    const dragged = props.dragged
    const { onDragStart } = useSidebarDrag({
      readonly,
      dragged
    })
    const allTaskTypes = Object.keys(ALL_TASK_TYPES).map((type) => ({
      type,
      ...ALL_TASK_TYPES[type]
    }))

    return () => (
      <div class={Styles.sidebar}>
        {allTaskTypes.map((task) => (
          <div
            class={Styles.draggable}
            draggable='true'
            onDragstart={(e) => onDragStart(e, task.type)}
          >
            <em
              class={[
                Styles['sidebar-icon'],
                Styles['icon-' + task.type.toLocaleLowerCase()]
              ]}
            ></em>
            <span>{task.alias}</span>
          </div>
        ))}
      </div>
    )
  }
})
