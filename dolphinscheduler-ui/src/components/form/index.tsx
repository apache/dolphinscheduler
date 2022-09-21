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

import { defineComponent, PropType, toRefs, h, unref } from 'vue'
import { NSpin, NGrid, NForm, NFormItemGi } from 'naive-ui'
import { useForm } from './use-form'
import type { GridProps, IMeta } from './types'
import { IFormItem } from "./types";
import styles from './index.module.scss'

const props = {
  meta: {
    type: Object as PropType<IMeta>,
    default: {},
    required: true
  },
  layout: {
    type: Object as PropType<GridProps>
  },
  loading: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

const taskParamDefineGroup = ['name', 'taskType', 'processName', 'flag', 'description', 'taskPriority', 'workerGroup', 'environmentCode', 'taskGroupId', 'taskGroupPriority', 'failRetryTimes', 'failRetryInterval', 'cpuQuota', 'memoryMax', 'delayTime', 'timeoutFlag', 'timeoutNotifyStrategy', 'timeout']

const Form = defineComponent({
  name: 'DSForm',
  props,
  setup(props, { expose }) {
    const { state, ...rest } = useForm()
    expose({
      ...rest
    })
    return { ...toRefs(state) }
  },
  render(props: { meta: IMeta; layout?: GridProps; loading?: boolean }) {
    const { loading, layout, meta } = props
    const { elements = [], ...restFormProps } = meta
    const taskDefineParams = []
    const taskBodyParams = []
    for (let element of elements) {
      let pathorclass = element.path ? element.path : element.class ? element.class : ''
      if (taskParamDefineGroup.indexOf(pathorclass) >= 0) {
        taskDefineParams.push(element)
      } else {
        taskBodyParams.push(element)
      }
    }
    const drawElements = (elements: IFormItem[]) => {
      return elements.map((element) => {
        const { span = 24, path, widget, ...formItemProps } = element

        return (
            <NFormItemGi
                { ...formItemProps }
                span={ unref(span) === void 0 ? 24 : unref(span) }
                path={ path }
                key={ path || String(Date.now() + Math.random()) }
            >
              { h(widget) }
            </NFormItemGi>
        )
      })
    }
    debugger;
    return (
        <NSpin show={ loading }>
          <NForm class={ styles.form } { ...restFormProps } ref='formRef'>
            <div class={ styles.paramsLeft }>
              <NGrid { ...layout }>
                { drawElements(taskDefineParams) }
              </NGrid>
            </div>
            <div class={ styles.paramsRight }>
              <NGrid  { ...layout }>
                { drawElements(taskBodyParams) }
              </NGrid>
            </div>
          </NForm>
        </NSpin>
    )
  }
})

export default Form
