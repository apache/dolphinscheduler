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
  <div class="sql-model">
    <m-list-box>
      <div slot="text">{{$t('Datasource')}}</div>
      <div slot="content">
        <m-datasource
                ref="refDs"
                @on-dsData="_onDsData"
                :data="{ type:type,datasource:datasource }">
        </m-datasource>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('SQL Type')}}</div>
      <div slot="content">
        <div style="display: inline-block;">
          <m-sql-type @on-sqlType="_onSqlType" :sql-type="sqlType"></m-sql-type>
        </div>
        <div style="display: inline-block;" v-if="sqlType === '0'">
          <span class="text-b">{{$t('Send Email')}}</span>
          <el-switch size="small" v-model="sendEmail"></el-switch>
        </div>
        <div style="display: inline-block;" v-if="sqlType === '0'">
          <span class="text-b">{{$t('Log display')}}</span>
          <m-select-input v-model="displayRows" :list="[1,10,25,50,100]" style="width: 70px;"></m-select-input>
          <span>{{$t('rows of result')}}</span>
        </div>
      </div>
    </m-list-box>
    <template v-if="sqlType === '0' && sendEmail">
      <m-list-box>
        <div slot="text"><strong class='requiredIcon'>*</strong>{{$t('Title')}}</div>
        <div slot="content">
          <el-input
            type="input"
            size="small"
            v-model="title"
            :disabled="isDetails"
            :placeholder="$t('Please enter the title of email')">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text"><strong class='requiredIcon'>*</strong>{{$t('Alarm group')}}</div>
        <div slot="content">
          <m-warning-groups v-model="groupId"></m-warning-groups>
        </div>
      </m-list-box>
    </template>
    <m-list-box v-show="type === 'HIVE'">
      <div slot="text">{{$t('SQL Parameter')}}</div>
      <div slot="content">
        <el-input
                :disabled="isDetails"
                type="input"
                size="small"
                v-model="connParams"
                :placeholder="$t('Please enter format') + ' key1=value1;key2=value2...'">
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('SQL Statement')}}</div>
      <div slot="content">
        <div class="form-mirror">
          <textarea
                  id="code-sql-mirror"
                  name="code-sql-mirror"
                  style="opacity: 0;">
          </textarea>
          <a class="ans-modal-box-max">
            <em class="el-icon-full-screen" @click="setEditorVal"></em>
          </a>
        </div>
      </div>
    </m-list-box>
    <m-list-box v-if="type === 'HIVE'">
      <div slot="text">{{$t('UDF Function')}}</div>
      <div slot="content">
        <m-udfs
                ref="refUdfs"
                @on-udfsData="_onUdfsData"
                :udfs="udfs"
                :type="type">
        </m-udfs>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Custom Parameters')}}</div>
      <div slot="content">
        <m-local-params
                ref="refLocalParams"
                @on-udpData="_onUdpData"
                :udp-list="localParams">
        </m-local-params>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Pre Statement')}}</div>
      <div slot="content">
        <m-statement-list
          ref="refPreStatements"
          @on-statement-list="_onPreStatements"
          :statement-list="preStatements">
        </m-statement-list>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Post Statement')}}</div>
      <div slot="content">
        <m-statement-list
          ref="refPostStatements"
          @on-statement-list="_onPostStatements"
          :statement-list="postStatements">
        </m-statement-list>
      </div>
    </m-list-box>
    <el-dialog
      :visible.sync="scriptBoxDialog"
      append-to-body="true"
      width="80%">
      <m-script-box :item="item" @getSriptBoxValue="getSriptBoxValue" @closeAble="closeAble"></m-script-box>
    </el-dialog>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mUdfs from './_source/udfs'
  import mListBox from './_source/listBox'
  import mScriptBox from './_source/scriptBox'
  import mSqlType from './_source/sqlType'
  import mDatasource from './_source/datasource'
  import mLocalParams from './_source/localParams'
  import mStatementList from './_source/statementList'
  import mWarningGroups from './_source/warningGroups'
  import mSelectInput from '../_source/selectInput'
  import disabledState from '@/module/mixin/disabledState'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editor

  export default {
    name: 'sql',
    data () {
      return {
        // Data source type
        type: '',
        // data source
        datasource: '',
        // Return to the selected data source
        rtDatasource: '',
        // Sql statement
        sql: '',
        // Custom parameter
        localParams: [],
        // UDF function
        udfs: '',
        // Sql type
        sqlType: '0',
        // Send email
        sendEmail: false,
        // Display rows
        displayRows: 10,
        // Email title
        title: '',
        // Sql parameter
        connParams: '',
        // Pre statements
        preStatements: [],
        // Post statements
        postStatements: [],
        item: '',
        scriptBoxDialog: false,
        groupId: null
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      createNodeId: Number
    },
    methods: {
      setEditorVal () {
        this.item = editor.getValue()
        this.scriptBoxDialog = true
      },
      getSriptBoxValue (val) {
        editor.setValue(val)
      },
      /**
       * return sqlType
       */
      _onSqlType (a) {
        this.sqlType = a
      },
      /**
       * return udfs
       */
      _onUdfsData (a) {
        this.udfs = a
      },
      /**
       * return Custom parameter
       */
      _onUdpData (a) {
        this.localParams = a
      },
      /**
       * return data source
       */
      _onDsData (o) {
        this.type = o.type
        this.rtDatasource = o.datasource
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
        if (this.sqlType === '0' && this.sendEmail && !this.title) {
          this.$message.warning(`${i18n.$t('Mail subject required')}`)
          return false
        }
        if (this.sqlType === '0' && this.sendEmail && (this.groupId === '' || this.groupId === null)) {
          this.$message.warning(`${i18n.$t('Alarm group required')}`)
          return false
        }
        // udfs Subcomponent verification Verification only if the data type is HIVE
        if (this.type === 'HIVE') {
          if (!this.$refs.refUdfs._verifUdfs()) {
            return false
          }
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
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
          type: this.type,
          datasource: this.rtDatasource,
          sql: editor.getValue(),
          udfs: this.udfs,
          sqlType: this.sqlType,
          sendEmail: this.sendEmail,
          displayRows: this.displayRows,
          title: this.title,
          groupId: this.groupId,
          localParams: this.localParams,
          connParams: this.connParams,
          preStatements: this.preStatements,
          postStatements: this.postStatements
        })
        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        this._destroyEditor()

        // editor
        editor = codemirror('code-sql-mirror', {
          mode: 'sql',
          readOnly: this.isDetails
        })

        this.keypress = () => {
          if (!editor.getOption('readOnly')) {
            editor.showHint({
              completeSingle: false,
              extraKeys: {
                Enter: ''
              }
            })
          }
        }

        this.changes = () => {
          this._cacheParams()
        }

        // Monitor keyboard
        editor.on('keypress', this.keypress)

        editor.on('changes', this.changes)

        editor.setValue(this.sql)

        return editor
      },
      _cacheParams () {
        this.$emit('on-cache-params', {
          type: this.type,
          datasource: this.rtDatasource,
          sql: editor ? editor.getValue() : '',
          udfs: this.udfs,
          sqlType: this.sqlType,
          sendEmail: this.sendEmail,
          displayRows: this.displayRows,
          title: this.title,
          groupId: this.groupId,
          localParams: this.localParams,
          connParams: this.connParams,
          preStatements: this.preStatements,
          postStatements: this.postStatements
        })
      },
      _destroyEditor () {
        if (editor) {
          editor.toTextArea() // Uninstall
          editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
          editor.off($('.code-sql-mirror'), 'changes', this.changes)
        }
      }
    },
    watch: {
      // Listening to sqlType
      sqlType (val) {
        if (val !== '0') {
          this.title = ''
          this.groupId = null
        }
      },
      // Listening data source
      type (val) {
        if (val !== 'HIVE') {
          this.connParams = ''
        }
      },
      // Watch the cacheParams
      cacheParams (val) {
        this._cacheParams()
      }
    },
    created () {
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        // backfill
        this.type = o.params.type || ''
        this.datasource = o.params.datasource || ''
        this.sql = o.params.sql || ''
        this.udfs = o.params.udfs || ''
        this.sqlType = o.params.sqlType
        this.sendEmail = o.params.sendEmail || false
        this.displayRows = o.params.displayRows || 10
        this.connParams = o.params.connParams || ''
        this.localParams = o.params.localParams || []
        this.preStatements = o.params.preStatements || []
        this.postStatements = o.params.postStatements || []
        this.title = o.params.title || ''
        this.groupId = o.params.groupId
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
        editor.off($('.code-sql-mirror'), 'changes', this.changes)
      }
    },
    computed: {
      cacheParams () {
        return {
          type: this.type,
          datasource: this.rtDatasource,
          udfs: this.udfs,
          sqlType: this.sqlType,
          sendEmail: this.sendEmail,
          displayRows: this.displayRows,
          title: this.title,
          groupId: this.groupId,
          localParams: this.localParams,
          connParams: this.connParams,
          preStatements: this.preStatements,
          postStatements: this.postStatements
        }
      }
    },
    components: { mListBox, mDatasource, mLocalParams, mUdfs, mSqlType, mStatementList, mScriptBox, mWarningGroups, mSelectInput }
  }
</script>
