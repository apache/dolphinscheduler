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
      <div slot="text">{{$t('Datasource')}}</div>
      <div slot="content">
        <m-datasource
                ref="refDs"
                @on-dsData="_onDsData"
                :supportType="['MYSQL','POSTGRESQL', 'ORACLE', 'SQLSERVER']"
                :data="{ type:dsType,datasource:datasource }">
        </m-datasource>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('SQL Statement')}}</div>
      <div slot="content">
        <div class="from-mirror">
          <textarea
            id="code-sql-mirror"
            name="code-sql-mirror"
            style="opacity: 0;">
          </textarea>
        </div>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('TargetDataBase')}}</div>
      <div slot="content">
        <m-datasource
                ref="refDt"
                @on-dsData="_onDtData"
                :supportType="['MYSQL','POSTGRESQL', 'ORACLE', 'SQLSERVER']"
                :data="{ type:dtType,datasource:datatarget }">
        </m-datasource>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('TargetTable')}}</div>
      <div slot="content">
        <x-input
          type="input"
          v-model="targetTable"
          :placeholder="$t('Please enter the table of target')"
          autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('TargetDataBase')}}{{$t('Pre Statement')}}</div>
      <div slot="content">
        <m-statement-list
          ref="refPreStatements"
          @on-statement-list="_onPreStatements"
          :statement-list="preStatements">
        </m-statement-list>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('TargetDataBase')}}{{$t('Post Statement')}}</div>
      <div slot="content">
        <m-statement-list
          ref="refPostStatements"
          @on-statement-list="_onPostStatements"
          :statement-list="postStatements">
        </m-statement-list>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">
        <span>{{$t('SpeedByte')}}</span>
      </div>
      <div slot="content">
        <m-select-input v-model="jobSpeedByte" :list="[0,1,10,50,100,512]">
        </m-select-input>
        <span>({{$t('0 means unlimited by byte')}})</span>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">
        <span>{{$t('SpeedRecord')}}</span>
      </div>
      <div slot="content">
        <m-select-input v-model="jobSpeedRecord" :list="[0,500,1000,1500,2000,2500,3000]">
        </m-select-input>
        <span>({{$t('0 means unlimited by count')}})</span>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './_source/listBox'
  import mDatasource from './_source/datasource'
  import mLocalParams from './_source/localParams'
  import mStatementList from './_source/statementList'
  import disabledState from '@/module/mixin/disabledState'
  import mSelectInput from '../_source/selectInput'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editor

  export default {
    name: 'datax',

    data () {
      return {
        // Data source type
        dsType: '',
        // data source
        datasource: '',
        // Data source type
        dtType: '',
        // data source
        datatarget: '',
        // Return to the selected data source
        rtDatasource: '',
        // Return to the selected data target
        rtDatatarget: '',
        // Sql statement
        sql: '',
        // target table
        targetTable: '',
        // Pre statements
        preStatements: [],
        // Post statements
        postStatements: [],
        // speed byte
        jobSpeedByte: 0,
        // speed record
        jobSpeedRecord: 1000,
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      createNodeId: Number
    },
    methods: {
      /**
       * return data source
       */
      _onDsData (o) {
        this.dsType = o.type
        this.rtDatasource = o.datasource
      },
      /**
       * return data target
       */
      _onDtData (o) {
        this.dtType = o.type
        this.rtDatatarget = o.datasource
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
       * verification
       */
      _verification () {
        if (!editor.getValue()) {
          this.$message.warning(`${i18n.$t('Please enter a SQL Statement(required)')}`)
          return false
        }

        // datasource Subcomponent verification
        if (!this.$refs.refDs._verifDatasource()) {
          return false
        }

        // datasource Subcomponent verification
        if (!this.$refs.refDt._verifDatasource()) {
          return false
        }

        if (!this.targetTable) {
          this.$message.warning(`${i18n.$t('Please enter a Target Table(required)')}`)
          return false
        }

        // preStatements Subcomponent verification
        if (!this.$refs.refPreStatements._verifProp()) {
          return false
        }

        // postStatements Subcomponent verification
        if (!this.$refs.refPostStatements._verifProp()) {
          return false
        }

        // storage
        this.$emit('on-params', {
          dsType: this.dsType,
          dataSource: this.rtDatasource,
          dtType: this.dtType,
          dataTarget: this.rtDatatarget,
          sql: editor.getValue(),
          targetTable: this.targetTable,
          jobSpeedByte: this.jobSpeedByte * 1024,
          jobSpeedRecord: this.jobSpeedRecord,
          preStatements: this.preStatements,
          postStatements: this.postStatements
        })
        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // editor
        editor = codemirror('code-sql-mirror', {
          mode: 'sql',
          readOnly: this.isDetails
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

        editor.setValue(this.sql)

        return editor
      }
    },
    created () {
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        // backfill
        this.dsType = o.params.dsType || ''
        this.datasource = o.params.dataSource || ''
        this.dtType = o.params.dtType || ''
        this.datatarget = o.params.dataTarget || ''
        this.sql = o.params.sql || ''
        this.targetTable = o.params.targetTable || ''
        this.jobSpeedByte = o.params.jobSpeedByte / 1024 || 0
        this.jobSpeedRecord = o.params.jobSpeedRecord || 0
        this.preStatements = o.params.preStatements || []
        this.postStatements = o.params.postStatements || []
      }
    },
    mounted () {
      setTimeout(() => {
        this._handlerEditor()
      }, 200)
    },
    destroyed () {
      /**
       * Destroy the editor instance
       */
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
      }
    },
    computed: {},
    components: { mListBox, mDatasource, mLocalParams, mStatementList, mSelectInput }
  }
</script>
