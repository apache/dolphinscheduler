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

import { defineComponent, PropType, ref, toRef, toRefs } from 'vue'
import Form from '@/components/form'
import { useTask } from './use-task'
import { useDetail } from './use-detail'
import type { ITaskType } from './types'
import getElementByJson from '@/components/form/get-elements-by-json'

const props = {
  projectCode: {
    type: Number as PropType<number>
  },
  taskType: {
    type: String as PropType<ITaskType>,
    default: 'SHELL',
    required: true
  }
}

const NodeDetail = defineComponent({
  name: 'NodeDetail',
  props,
  setup(props, { expose }) {
    const { taskType, projectCode } = props

    const { json, model } = useTask({ taskType, projectCode })
    const { state } = useDetail()

    const jsonRef = ref(json)

    const { rules, elements } = getElementByJson(jsonRef.value, model)

    expose({
      formRef: toRef(state, 'formRef'),
      form: model
    })

    return { rules, elements, model, ...toRefs(state) }
  },
  render() {
    const { rules, elements, model } = this
    return (
      <Form
        ref='formRef'
        meta={{
          model,
          rules,
          elements
        }}
        layout={{
          xGap: 10
        }}
      />
    )
  }
})

export default NodeDetail
