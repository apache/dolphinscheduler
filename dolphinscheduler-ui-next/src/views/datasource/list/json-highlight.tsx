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

import { defineComponent, PropType } from 'vue'
import { NText } from 'naive-ui'
import { isBoolean, isNumber, isPlainObject } from 'lodash'
import styles from './json-highlight.module.scss'

const props = {
  rowData: {
    type: Object as PropType<object>,
    default: {}
  }
}

const JsonHighlight = defineComponent({
  name: 'JsonHighlight',
  props,
  render(props: { rowData: { connectionParams: string } }) {
    return (
      <pre class={styles['json-highlight']}>
        {syntaxHighlight(props.rowData.connectionParams)}
      </pre>
    )
  }
})

const syntaxHighlight = (json: string) => {
  if (!isPlainObject(JSON.parse(json))) return ''
  const lines = [<NText v-html='{'></NText>]
  const entries = Object.entries(JSON.parse(json))
  for (let i = 0, len = entries.length; i < len; i++) {
    const [key, value] = entries[i]
    let type = ''
    if (isBoolean(value) || value === null) {
      type = 'info'
    } else if (isNumber(value)) {
      type = 'warning'
    } else {
      type = 'success'
    }
    lines.push(
      <NText tag='div' class={styles['line']}>
        <NText type='error'>"{key}": </NText>
        <NText type={type}>
          "{value}"{i !== len - 1 ? ',' : ''}
        </NText>
      </NText>
    )
  }
  lines.push(<NText v-html='}'></NText>)
  return lines
}

export default JsonHighlight
