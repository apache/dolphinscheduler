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

import { CSSProperties, defineComponent, PropType } from 'vue'
import { NResult } from 'naive-ui'

const defaultContentStyle = {
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center'
}

const props = {
  title: {
    type: String as PropType<string>
  },
  description: {
    type: String as PropType<string>
  },
  size: {
    type: String as PropType<'small' | 'medium' | 'large' | 'huge'>,
    default: 'medium'
  },
  status: {
    type: String as PropType<
      '500' | 'error' | 'info' | 'success' | 'warning' | '404' | '403' | '418'
    >
  },
  contentStyle: {
    type: String as PropType<string | CSSProperties>,
    default: defaultContentStyle
  }
}

const Result = defineComponent({
  name: 'Result',
  props,
  render() {
    const { title, description, size, status, contentStyle, $slots } = this
    return (
      <NResult
        title={title}
        size={size}
        description={description}
        status={status}
        style={contentStyle}
      >
        {$slots}
      </NResult>
    )
  }
})

export default Result
