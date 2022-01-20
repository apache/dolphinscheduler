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

import type { Ref } from 'vue'
import type { Graph } from '@antv/x6'
import type { Dragged } from './dag'
import { genTaskCodeList } from '@/service/modules/task-definition'
import { useGraphOperations } from './dag-hooks'

interface Options {
  readonly: Ref<boolean>
  graph: Ref<Graph | undefined>
  container: Ref<HTMLElement | undefined>
  dragged: Ref<Dragged>
  projectCode: string
}

/**
 * Drop sidebar item in canvas
 */
export function useCanvasDrop(options: Options) {
  const { readonly, graph, container, dragged, projectCode } = options

  const { addNode } = useGraphOperations({ graph })

  const onDrop = (e: DragEvent) => {
    e.stopPropagation()
    e.preventDefault()
    if (readonly.value) {
      return
    }
    if (dragged.value && graph.value && container.value && projectCode) {
      const { type, x: eX, y: eY } = dragged.value
      const { x, y } = graph.value.clientToLocal(e.clientX, e.clientY)
      const genNums = 1
      genTaskCodeList(genNums, Number(projectCode)).then((res) => {
        const [code] = res
        addNode(code + '', type, { x: x - eX, y: y - eY })
        // openTaskConfigModel(code, type)
      })
    }
  }

  const preventDefault = (e: DragEvent) => {
    e.preventDefault()
  }

  return {
    onDrop,
    onDragenter: preventDefault,
    onDragover: preventDefault,
    onDragleave: preventDefault
  }
}
