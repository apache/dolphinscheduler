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

import { defineComponent, ref, inject } from 'vue'
import Styles from './dag.module.scss'
import type { PropType, Ref } from 'vue'
import type { Dragged } from './dag'
import { useCanvasInit, useCellActive, useCanvasDrop } from './dag-hooks'
import { useRoute } from 'vue-router'

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
  name: 'workflow-dag-canvas',
  props,
  setup(props, context) {
    const readonly = inject('readonly', ref(false))
    const graph = inject('graph', ref())
    const route = useRoute()
    const projectCode = route.params.projectCode as string

    const { paper, minimap, container } = useCanvasInit({ readonly, graph })

    // Change the style on cell hover and select
    useCellActive({ graph })

    // Drop sidebar item in canvas
    const { onDrop, onDragenter, onDragover, onDragleave } = useCanvasDrop({
      readonly,
      dragged: props.dragged,
      graph,
      container,
      projectCode
    })

    return () => (
      <div
        ref={container}
        class={Styles.canvas}
        onDrop={onDrop}
        onDragenter={onDragenter}
        onDragover={onDragover}
        onDragleave={onDragleave}
      >
        <div ref={paper} class={Styles.paper}></div>
        <div ref={minimap} class={Styles.minimap}></div>
      </div>
    )
  }
})
