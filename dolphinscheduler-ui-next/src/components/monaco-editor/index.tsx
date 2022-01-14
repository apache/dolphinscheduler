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

import {
  defineComponent,
  onMounted,
  onUnmounted,
  PropType,
  nextTick,
  ref,
  watch
} from 'vue'
import * as monaco from 'monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

const props = {
  modelValue: {
    type: String as PropType<string>,
    default: ''
  },
  language: {
    type: String as PropType<string>,
    default: 'shell'
  },
  readOnly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  options: {
    type: Object,
    default: () => {}
  }
}

// @ts-ignore
window.MonacoEnvironment = {
  getWorker(_: any, label: string) {
    if (label === 'json') {
      return new jsonWorker()
    }
    if (['css', 'scss', 'less'].includes(label)) {
      return new cssWorker()
    }
    if (['html', 'handlebars', 'razor'].includes(label)) {
      return new htmlWorker()
    }
    if (['typescript', 'javascript'].includes(label)) {
      return new tsWorker()
    }
    return new editorWorker()
  }
}

export default defineComponent({
  name: 'MonacoEditor',
  props,
  setup(props) {
    let editor = null as monaco.editor.IStandaloneCodeEditor | null
    const content = ref()
    const getValue = () => editor?.getValue()

    watch(
      () => props.modelValue,
      (val) => {
        if (val !== getValue()) {
          editor?.setValue(val)
        }
      }
    )

    onMounted(async () => {
      content.value = props.modelValue

      await nextTick()
      const dom = document.getElementById('monaco-container')
      if (dom) {
        editor = monaco.editor.create(dom, props.options, {
          value: props.modelValue,
          language: props.language,
          readOnly: props.readOnly,
          automaticLayout: true
        })
      }
    })

    onUnmounted(() => {
      editor?.dispose()
    })

    return { getValue }
  },
  render() {
    return (
      <div
        id='monaco-container'
        style={{
          height: '300px',
          border: '1px solid #eee'
        }}
      ></div>
    )
  }
})
