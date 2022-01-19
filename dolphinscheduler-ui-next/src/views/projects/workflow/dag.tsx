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

import type { Graph } from '@antv/x6';
import { defineComponent, ref, provide } from 'vue'
import DagToolbar from './dag-toolbar';
import DagCanvas from './dag-canvas';
import DagSidebar from './dag-sidebar';
import Styles from './dag.module.scss';
import "./x6-style.scss";


export interface Dragged {
  x: number;
  y: number;
  type: string;
}

export default defineComponent({
  name: "workflow-dag",
  setup(props, context) {

    // Whether the graph can be operated
    const readonly = ref(false);
    provide('readonly', readonly);

    const graph = ref<Graph>();
    provide('graph', graph);

    // The sidebar slots
    const toolbarSlots = {
      left: context.slots.toolbarLeft,
      right: context.slots.toolbarRight
    }

    // The element currently being dragged up
    const dragged = ref<Dragged>({
      x: 0,
      y: 0,
      type: ''
    });

    return () => (
      <div class={Styles.dag}>
        <DagToolbar v-slots={toolbarSlots} />
        <div class={Styles.content}>
          <DagSidebar dragged={dragged} />
          <DagCanvas dragged={dragged} />
        </div>
      </div>
    )
  }
})