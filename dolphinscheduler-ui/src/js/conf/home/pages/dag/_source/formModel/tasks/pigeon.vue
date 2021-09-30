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
  <div class="datax-model">
    <m-list-box>
      <div slot="text">{{$t('TargetJobName')}}</div>
      <div slot="content">
        <el-input
          type="input"
          size="small"
          v-model="targetJobName"
          :placeholder="$t('Please enter Pigeon job name')">
        </el-input>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mListBox from './_source/listBox'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'pigeon',

    data () {
      return {
        // target table
        targetJobName: ''
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      createNodeId: Number
    },
    methods: {
      setEditorVal () {
        // this.item = editor.getValue()
        // this.scriptBoxDialog = true
      },
      getSriptBoxValue (val) {
      },
      /**
       * return pre statements
       */
      _onPreStatements (a) {
        this.preStatements = a
      },
      /**
       * return post statements
       */
      _onPostStatements (a) {
        this.postStatements = a
      },
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      /**
       * verification
       */
      _verification () {
        // storage
        this.$emit('on-params', {
          targetJobName: this.targetJobName
        })

        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // this._destroyEditor()

        // editor
        // editor = codemirror('code-sql-mirror', {
        //   mode: 'sql',
        //   readOnly: this.isDetails
        // })
        //
        // this.keypress = () => {
        //   if (!editor.getOption('readOnly')) {
        //     editor.showHint({
        //       completeSingle: false
        //     })
        //   }
        // }
        //
        // // Monitor keyboard
        // editor.on('keypress', this.keypress)
        //
        // editor.on('changes', () => {
        //   this._cacheParams()
        // })
        //
        // editor.setValue(this.sql)
        //
        // return editor
      },
      // _handlerJsonEditor () {
      //   this._destroyJsonEditor()
      //
      //   // jsonEditor
      //   jsonEditor = codemirror('code-json-mirror', {
      //     mode: 'json',
      //     readOnly: this.isDetails
      //   })
      //
      //   this.keypress = () => {
      //     if (!jsonEditor.getOption('readOnly')) {
      //       jsonEditor.showHint({
      //         completeSingle: false
      //       })
      //     }
      //   }
      //
      //   // Monitor keyboard
      //   jsonEditor.on('keypress', this.keypress)
      //
      //   jsonEditor.on('changes', () => {
      //     // this._cacheParams()
      //   })
      //
      //   jsonEditor.setValue(this.json)
      //
      //   return jsonEditor
      // },
      _cacheParams () {
        this.$emit('on-cache-params', {
          // dsType: this.dsType,
          // dataSource: this.rtDatasource,
          // dtType: this.dtType,
          // dataTarget: this.rtDatatarget,
          // sql: editor ? editor.getValue() : '',
          targetJobName: this.targetJobName
          // jobSpeedByte: this.jobSpeedByte * 1024,
          // jobSpeedRecord: this.jobSpeedRecord,
          // preStatements: this.preStatements,
          // postStatements: this.postStatements,
          // xms: +this.xms,
          // xmx: +this.xmx
        })
      }
      // _destroyEditor () {
      //   if (editor) {
      //     editor.toTextArea() // Uninstall
      //     editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
      //     editor.off($('.code-sql-mirror'), 'changes', this.changes)
      //   }
      // },
      // _destroyJsonEditor () {
      //   if (jsonEditor) {
      //     jsonEditor.toTextArea() // Uninstall
      //     jsonEditor.off($('.code-json-mirror'), 'keypress', this.keypress)
      //     jsonEditor.off($('.code-json-mirror'), 'changes', this.changes)
      //   }
      // }
    },
    created () {
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        // backfill
        this.targetJobName = o.params.targetJobName || ''
      }
    },
    mounted () {
      // if (this.customConfig) {
      //   setTimeout(() => {
      //     this._handlerJsonEditor()
      //   }, 200)
      // } else {
      //   setTimeout(() => {
      //     this._handlerEditor()
      //   }, 200)
      // }
    },
    destroyed () {
      // /**
      //  * Destroy the editor instance
      //  */
      // if (editor) {
      //   editor.toTextArea() // Uninstall
      //   editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
      // }
      // if (jsonEditor) {
      //   jsonEditor.toTextArea() // Uninstall
      //   jsonEditor.off($('.code-json-mirror'), 'keypress', this.keypress)
      // }
    },
    watch: {
      // Watch the cacheParams
      cacheParams (val) {
        this._cacheParams()
      }
    },
    computed: {
      cacheParams () {
        return {
          targetJobName: this.targetJobName
        }
      }
    },
    components: { mListBox }
  }
</script>
