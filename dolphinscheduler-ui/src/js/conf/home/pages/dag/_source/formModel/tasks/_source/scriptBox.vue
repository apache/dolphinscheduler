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
<template>
  <div class="script-model">
    <m-list-box>
      <div slot="content">
        <div class="from-mirror1">
          <textarea
            id="code-shell-mirror1"
            name="code-shell-mirror1"
            style="opacity: 0">
          </textarea>
        </div>
      </div>
    </m-list-box>
    <a class="ans-modal-box-close">
      <em class="ans-icon-min" @click="closeModal"></em>
    </a>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './listBox'
  import disabledState from '@/module/mixin/disabledState'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editor

  export default {
    name: 'shell',
    data () {
      return {
        // script
        rawScript: '',
      }
    },
    mixins: [disabledState],
    props: {
      item: String
    },
    methods: {
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // editor
        let self =this
        editor = codemirror('code-shell-mirror1', {
          mode: 'shell',
          readOnly: this.isDetails
        })
        editor.on("change",function(){
          self.$emit('getSriptBoxValue',editor.getValue())
        })

        this.keypress = () => {
          if (!editor.getOption('readOnly')) {
            editor.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editor.on('keypress', this.keypress)

        editor.setValue(this.rawScript)

        return editor
      },
      closeModal() {
        let self = this
        self.$emit('closeAble')
      }
    },
    watch: {},
    created () {
      let o = this.item

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.rawScript = o
      }
    },
    mounted () {
      setTimeout(() => {
        this._handlerEditor()
      }, 200)
    },
    destroyed () {
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-shell-mirror1'), 'keypress', this.keypress)
      }
    },
    components: { mListBox }
  }
</script>
<style lang="scss" rel="stylesheet/scss" scope>
  .script-model {
    width:100%;
  }
  .from-mirror1 {
    .CodeMirror {
      min-height: 600px;
      max-height: 700px;
    }
  }
</style>
