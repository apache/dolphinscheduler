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
  <div class="flinkx-model">
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <x-radio-group v-model="mode">
          <x-radio :label="'local'" :disabled="isDetails"></x-radio>
          <x-radio :label="'standalone'" :disabled="true"></x-radio>
          <x-radio :label="'yarn'" :disabled="true"></x-radio>
          <x-radio :label="'yarnPer'" :disabled="true"></x-radio>
        </x-radio-group>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Custom template')}}</div>
      <div slot="content">
        <label class="label-box">
          <div style="padding-top: 5px;">
            <x-switch v-model="enable" @on-click="_onSwitch" :disabled="isDetails"></x-switch>
          </div>
        </label>
      </div>
    </m-list-box>
    <div v-if="!enable">
      <!-- content     -->
      <!-- reader      -->
      <m-list-box>
        <div slot="text">{{$t('SourceDataBase')}}</div>
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
        <div slot="text">{{$t('Use SQL Statement')}}</div>
        <div slot="content">
          <label class="label-box">
            <div style="padding-top: 5px;">
              <x-switch v-model="isSqlStatement" @on-click="_onSqlStatementSwitch" :disabled="isDetails"></x-switch>
            </div>
          </label>
        </div>
      </m-list-box>
      <div v-if="isSqlStatement">
        <m-list-box>
          <div slot="text">{{$t('SQL Statement')}}</div>
          <div slot="content">
            <div class="form-mirror">
            <textarea
              id="code-sql-mirror"
              name="code-sql-mirror"
              style="opacity: 0;">
            </textarea>
            </div>
          </div>
        </m-list-box>
      </div>
      <div v-else>
        <m-list-box>
          <div slot="text">{{$t('SourceTable')}}</div>
          <div slot="content">
            <x-input
              type="input"
              :disabled="isDetails"
              v-model="sourceTable"
              :placeholder="$t('Please enter the table of source')"
              autocomplete="off">
            </x-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('SourceColumn')}}</div>
          <div slot="content">
            <m-local-columns
              ref="refSourceColumns"
              @on-local-columns="_onLocalSourceColumns"
              :udp-list="sourceColumns"
              :data-source="rtDatasource"
              :data-table="sourceTable"
              :hide="true">
            </m-local-columns>
          </div>
        </m-list-box>
      </div>

      <!-- writer      -->
      <m-list-box>
        <div slot="text">{{$t('TargetDataBase')}}</div>
        <div slot="content">
          <m-datasource
                  ref="refDt"
                  @on-dsData="_onDtData"
                  :supportType="['MYSQL','POSTGRESQL', 'ORACLE', 'SQLSERVER']"
                  :data="{ type:dtType,datasource:datatarget}">
          </m-datasource>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('TargetTable')}}</div>
        <div slot="content">
          <x-input
            type="input"
            :disabled="isDetails"
            v-model="targetTable"
            :placeholder="$t('Please enter the table of target')"
            autocomplete="off">
          </x-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('TargetColumn')}}</div>
        <div slot="content">
          <m-local-columns
            ref="refTargetColumns"
            @on-local-columns="_onLocalTargetColumns"
            :udp-list="targetColumns"
            :data-source="rtDatatarget"
            :data-table="targetTable"
            :hide="true">
          </m-local-columns>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('Write Mode')}}</div>
        <div slot="content">
          <x-select
            style="width: 130px;"
            v-model="writeMode"
            @on-change="_onWriteModeChange"
            :disabled="isDetails">
            <x-option
              v-for="city in writeModeList"
              :key="city.code"
              :value="city.code"
              :label="city.code">
            </x-option>
          </x-select>
        </div>
      </m-list-box>
      <div v-if="writeMode==='UPDATE'">
        <m-list-box>
          <div slot="text">{{$t('Unique Index')}}</div>
          <div slot="content">
            <x-select multiple
                      :disabled="isDetails"
                      ref="customMultiple"
                      style="width: 260px"
                      @on-change="_onUniqueKeyChange"
                      v-model="uniqueKey">
              <slot slot="multiple" slot-scope="{selectedList}">
                <span class="tag-wrapper" v-for=" o in selectedList" :key="o.value">
                  <span class="tag-text">{{o.value}}</span>
                </span>
              </slot>
              <x-option
                v-for="city in targetColumns"
                :key="city.name"
                :value="city.name"
                :label="city.name">
              </x-option>
            </x-select>
          </div>
        </m-list-box>
      </div>
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

      <!-- setting      -->
      <m-list-box>
        <div slot="text">{{$t('Sync Mode')}}</div>
        <div slot="content">
          <x-radio-group v-model="polling" @on-change="_initIncreParams">
            <x-radio :label="false" :disabled="isDetails">{{$t('Full Volume Sync')}}</x-radio>
            <x-radio :label="true" :disabled="isDetails">{{$t('Incremental Sync')}}</x-radio>
          </x-radio-group>
        </div>
      </m-list-box>
      <div v-if="polling">
        <m-list-box>
          <div slot="text">{{$t('Polling Interval')}}</div>
          <div slot="content">
            <m-select-input v-model="pollingInterval" :list="[5,10,20,30,60,120,180]">
            </m-select-input>
            <span>({{$t('Polling Interval Tips')}})</span>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Incremental Column')}}</div>
          <div slot="content">
            <x-select filterable
                      style="width: 160px;"
                      :disabled="isDetails"
                      v-model="increColumn">
              <x-option
                v-for="city in sourceColumns"
                :key="city.name"
                :value="city.name"
                :label="city.name">
              </x-option>
            </x-select>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Start Location')}}</div>
          <div slot="content">
            <x-input
              type="input"
              :disabled="isDetails"
              v-model="startLocation"
              :placeholder="$t('Please enter the start location of incremental column(optional)')"
              autocomplete="off">
            </x-input>
          </div>
        </m-list-box>
      </div>

      <m-list-box>
        <div slot="text">{{$t('Use Split Pk')}}</div>
        <div slot="content">
          <label class="label-box">
            <div style="padding-top: 5px;">
              <x-switch v-model="isSplit" @on-click="_onSplitPkSwitch" :disabled="isDetails"></x-switch>
            </div>
          </label>
        </div>
      </m-list-box>
      <div v-if="isSplit">
        <m-list-box>
          <div slot="text">{{$t('Split Pk Column')}}</div>
          <div slot="content">
            <x-select ref="clearSelect"
                      style="width: 160px;"
                      :disabled="isDetails"
                      @on-change="_onSplitPkChange"
                      v-model="splitPk">
              <x-option
                v-for="city in sourceColumns"
                :key="city.name"
                :value="city.name"
                :label="city.name">
              </x-option>
            </x-select>
            <span>({{$t('This cloumn type must be integral type')}})</span>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">
            <span>{{$t('SpeedChannel')}}</span>
          </div>
          <div slot="content">
            <m-select-input v-model="jobSpeedChannel" :list="[1,2,3,5,8,10]">
            </m-select-input>
          </div>
        </m-list-box>
      </div>

      <m-list-box>
        <div slot="text">
          <span>{{$t('Batch Size')}}</span>
        </div>
        <div slot="content">
          <m-select-input v-model="batchSize" :list="[1,1024,2048,4096,8192,16394]">
          </m-select-input>
          <span>({{$t('1 means unlimited')}})</span>
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
          <span>{{$t('Sync Type')}}</span>
        </div>
        <div slot="content">
            <label class="label-box">
              <x-radio-group v-model="isStream" >
                <x-radio :label="false" :disabled="isDetails">{{$t('Offline Sync')}}</x-radio>
                <x-radio :label="true" :disabled="true">{{$t('Realtime Sync')}}</x-radio>
              </x-radio-group>
            </label>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('Resume From Break Point')}}</div>
        <div slot="content">
          <label class="label-box">
            <div style="padding-top: 5px;">
              <x-switch v-model="isRestore" @on-click="_onRestoreSwitch" :disabled="isDetails"></x-switch>
            </div>
          </label>
        </div>
      </m-list-box>
      <div v-if="isRestore">
        <m-list-box>
          <div slot="text">{{$t('Resume Column Name')}}</div>
          <div slot="content">
            <x-select style="width: 160px;"
                      :disabled="isDetails"
                      @on-change="_onRestoreColumnNameChange"
                      v-model="restoreColumnName">
              <x-option
                v-for="city in sourceColumns"
                :key="city.name"
                :value="city.name"
                :label="city.name">
              </x-option>
            </x-select>
          </div>
        </m-list-box>
      </div>

      <m-list-box>
        <div slot="text">
          <span>{{$t('Checkpoint Num')}}</span>
        </div>
        <div slot="content">
          <m-select-input v-model="maxRowNumForCheckpoint" :list="[0,200,500,1000,2000,5000,10000]">
          </m-select-input>
          <span>({{$t('max row num for checkpoint')}})</span>
        </div>
      </m-list-box>
    </div>

    <div v-else>
      <m-list-box>
        <div slot="text">json</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-json-mirror"
              name="code-json-mirror"
              style="opacity: 0;">
            </textarea>
          </div>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('Custom Parameters')}}</div>
        <div slot="content">
          <m-local-params
                  ref="refLocalParams"
                  @on-local-params="_onLocalParams"
                  :udp-list="localParams"
                  :hide="false">
          </m-local-params>
        </div>
      </m-list-box>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import {integralType} from './_source/commcon'
  import mListBox from './_source/listBox'
  import mDatasource from './_source/datasourceNew'
  import mLocalParams from './_source/localParams'
  import mLocalColumns from './_source/localColumns'
  import mStatementList from './_source/statementList'
  import disabledState from '@/module/mixin/disabledState'
  import mSelectInput from '../_source/selectInput'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editor
  let jsonEditor

  export default {
  name: 'flinkx',

    data () {
      return {
        customConfig: 0,

        // Data Custom template
        enable: false,
        // json
        json: '',
        // Custom parameter
        localParams: [],

        //reader
        // Data source type
        dsType: '',
        // data source
        datasource: '',
        // source table
        sourceTable: '',
        //source column
        sourceColumns: [],
        // Return to the selected data source
        rtDatasource: '',
        //isSqlStatement
        isSqlStatement: false,
        //customSql
        customSql: '',
        //isSplit
        isSplit: false,
        //split pk
        splitPk: '',
        // Polling
        polling: false,
        //Polling Interval
        pollingInterval: 5,
        //incremental column
        increColumn: '',
        //start location
        startLocation: '',

        //writer
        // Data source type
        dtType: '',
        // data source
        datatarget: '',
        // target table
        targetTable: '',
        //target column
        targetColumns: [],
        // Pre statements
        preStatements: [],
        // Post statements
        postStatements: [],
        // Return to the selected data target
        rtDatatarget: '',
        //write mode
        writeMode: 'INSERT',
        //batch size
        batchSize: 1024,
        //write mode list  unuse { code: 'REPLACE' }
        writeModeList: [{ code: 'INSERT' }, { code: 'UPDATE' }],
        //update key (unique key)
        uniqueKey:[],

        //setting
        // speed byte
        jobSpeedByte: 0,
        //speed channel
        jobSpeedChannel: 1,
        //mode
        mode: 'local',
        // 是否开启实时同步
        isStream: false,
        // 是否开启断点续传
        isRestore: false,
        //开启断点续传后必填 断点续传字段索引
        restoreColumnIndex: -1,
        //开启断点续传后必填 断点续传字段名称
        restoreColumnName: '',
        // 触发checkpoint数据条数
        maxRowNumForCheckpoint: 0,

      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      createNodeId: Number
    },
    methods: {
      _onSwitch (is) {
        if(is) {
          this.customConfig = 1
          setTimeout(() => {
            this._handlerJsonEditor()
          }, 200)
        } else {
          this.customConfig = 0
          if(this.isSqlStatement){
            setTimeout(() => {
              this._handlerEditor()
            }, 250)
          }
        }
      },
      _onSqlStatementSwitch:_.debounce(function (is){
        this.sourceTable=''
        this.sourceColumns=[]
        this.isSqlStatement = is
        if(is){
          setTimeout(() => {
            this._handlerEditor()
          }, 200)
        }else {
          this._destroyEditor()
        }
        this._initIncreParams(false)
        this._onSplitPkSwitch(false)
        this._onRestoreSwitch(false)

      },600),

      _checkColumns(){
        if(!this.isSqlStatement){
          if(_.isEmpty(this.sourceColumns)){
            this.$message.warning(`${i18n.$t('Please be sure the source columns is not null')}`)
            return false
          }
        }else {
          if(_.isEmpty(this.targetColumns)){
            this.sourceColumns= []
            this.$message.warning(`${i18n.$t('Please be sure the target columns is not null')}`)
            return false
          }else{
            this.sourceColumns = this.targetColumns
          }
        }
        return true
      },

      _checkIntegralType(type){
        const index = integralType.indexOf(type)
        if(index === -1){
          this.$message.error(`${i18n.$t('This cloumn type must be integral type')}`)
          this.splitPk =''
          this.$refs.clearSelect.setSelected('')
          return false
        }else {
          return true
        }
      },

      _onSplitPkSwitch(is){
        if(is){
          this._checkColumns()
        }
        this.isSplit = is
        if(!is){
          this.splitPk = ''
          this.jobSpeedChannel = 1
        }
      },

      _onSplitPkChange:function(val){
        let arr = []
        if(this.isSqlStatement){
          arr = this.targetColumns
        }else {
          arr = this.sourceColumns
        }
        // const o = this.sourceColumns
        if(!_.isEmpty(val)){
          const label = val['label']
          arr.filter((item,index)=>{
            if(item['name']===label){
              if(this._checkIntegralType(item['type'])){
                this.splitPk = val['label']
              }
              return true
            }
            return false
          })
        }
      },

      _onRestoreSwitch(is){
        if(is){
          this._checkColumns()
        }
        this.isRestore = is
        if(!is){
          this.restoreColumnName = ''
          this.restoreColumnIndex = -1
        }
      },

      _onRestoreColumnNameChange: function (val) {
        let arr = []
        if(this.isSqlStatement){
          arr = this.targetColumns
        }else {
          arr = this.sourceColumns
        }
        // const o = this.sourceColumns
        if (!_.isEmpty(val)) {
          this.restoreColumnName = val['label']
          arr.filter((item,index)=>{
            if(item['name']===val['label']){
              this.restoreColumnIndex = index
              return true
            }
            return false
          })
        }
      },

      /**
       * init Incremental params
       */
      _initIncreParams(val){
        if(val){
          this._checkColumns()
        }
        if(!val){
          this.polling = false
          this.increColumn = ''
          this.pollingInterval = 5
          this.startLocation = ''
        }
      },
      /**
       * if write mode is INSERT,init params
       */
      _onWriteModeChange(val){
        if(!_.isEmpty(val) && val["value"] ==='INSERT'){
          this.uniqueKey=[]
        }
      },
      _onUniqueKeyChange(val){
        this.uniqueKey = []
        if(!_.isEmpty(val)){
          for(const item of val){
            this.uniqueKey.push(item["value"])
          }
        }
      },

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
       * return localParams
       */
      _onLocalSourceColumns (a) {
        this.sourceColumns = a
      },

      /**
       * return localParams
       */
      _onLocalTargetColumns (a) {
        this.targetColumns = a
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
        if(this.customConfig) {
          if (!jsonEditor.getValue()) {
            this.$message.warning(`${i18n.$t('Please enter a JSON Statement(required)')}`)
            return false
          }

          // localParams Subcomponent verification
          if (!this.$refs.refLocalParams._verifProp()) {
            return false
          }

          // storage
          this.$emit('on-params', {
            customConfig: this.customConfig,
            json: jsonEditor.getValue(),
            localParams: this.localParams
          })
          return true
        } else {

          if(this.isSqlStatement){
            if(!editor.getValue()){
              this.$message.warning(`${i18n.$t('Please enter a SQL Statement(required)')}`)
              return false
            }
          }else {
            if (!this.sourceTable) {
              this.$message.warning(`${i18n.$t('Please enter a Source Table(required)')}`)
              return false
            }
          }

          if(!this._checkColumns()){
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

          if(this.polling){
            if(!this.increColumn){
              this.$message.warning(`${i18n.$t('Please choose a Incremental Column(required)')}`)
              return false
            }
          }

          if(this.writeMode==='UPDATE'){
            if(this.dtType==='POSTGRESQL'){
              this.$message.warning(`${i18n.$t('POSTGRESQL does not support UPDATE temporarily')}`)
              return false
            }
            if(_.isEmpty(this.uniqueKey)){
              this.$message.warning(`${i18n.$t('Please choose a Unique Index(required)')}`)
              return false
            }
          }

          if(this.isRestore){
            if(!this.restoreColumnName){
              this.$message.warning(`${i18n.$t('Please choose a Resume Column(required)')}`)
              return false
            }
          }

          if(this.isSplit){
            if(!this.splitPk){
              this.$message.warning(`${i18n.$t('Please choose a SplitPk Column(required)')}`)
              return false
            }

            if(this.jobSpeedChannel<=1){
              this.$message.warning(`${i18n.$t('If you use SplitPk,please be sure the number of job channel is bigger than 1')}`)
              return false
            }
          }

          // storage
          this.$emit('on-params', {
            customConfig: this.customConfig,
            polling: this.polling,
            pollingInterval: this.pollingInterval * 1000,
            increColumn: this.increColumn,
            startLocation: this.startLocation,
            writeMode: this.writeMode,
            uniqueKey: this.uniqueKey,
            dsType: this.dsType,
            dataSource: this.rtDatasource,
            dtType: this.dtType,
            dataTarget: this.rtDatatarget,
            sourceTable: this.sourceTable,
            targetTable: this.targetTable,
            sourceColumns: this.sourceColumns,
            targetColumns: this.targetColumns,
            jobSpeedByte: this.jobSpeedByte * 1024,
            preStatements: this.preStatements,
            postStatements: this.postStatements,
            isStream: this.isStream,
            isRestore: this.isRestore,
            restoreColumnIndex: this.restoreColumnIndex,
            restoreColumnName: this.restoreColumnName,
            maxRowNumForCheckpoint: this.maxRowNumForCheckpoint,
            mode: this.mode,
            isSplit: this.isSplit,
            splitPk: this.splitPk,
            jobSpeedChannel:this.jobSpeedChannel,
            isSqlStatement:this.isSqlStatement,
            customSql:editor?editor.getValue():'',
            batchSize:this.batchSize
          })
          return true
        }
      },

      _handlerEditor(){
        this._destroyEditor()
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

        editor.on('changes', () => {
          this._cacheParams()
        })

        editor.setValue(this.customSql)

        return editor

      },

      _handlerJsonEditor () {
        this._destroyJsonEditor()

        // jsonEditor
        jsonEditor = codemirror('code-json-mirror', {
          mode: 'json',
          readOnly: this.isDetails
        })

        this.keypress = () => {
          if (!jsonEditor.getOption('readOnly')) {
            jsonEditor.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        jsonEditor.on('keypress', this.keypress)

        jsonEditor.on('changes', () => {
          // this._cacheParams()
        })

        jsonEditor.setValue(this.json)

        return jsonEditor
      },
      _cacheParams () {
        this.$emit('on-cache-params', {
          polling: this.polling,
          pollingInterval: this.pollingInterval * 1000,
          increColumn: this.increColumn,
          startLocation: this.startLocation,
          writeMode: this.writeMode,
          uniqueKey: this.uniqueKey,
          dsType: this.dsType,
          dataSource: this.rtDatasource,
          dtType: this.dtType,
          dataTarget: this.rtDatatarget,
          sourceTable: this.sourceTable,
          targetTable: this.targetTable,
          sourceColumns: this.sourceColumns,
          targetColumns: this.targetColumns,
          jobSpeedByte: this.jobSpeedByte * 1024,
          preStatements: this.preStatements,
          postStatements: this.postStatements,
          isStream: this.isStream,
          isRestore: this.isRestore,
          restoreColumnIndex: this.restoreColumnIndex,
          restoreColumnName: this.restoreColumnName,
          maxRowNumForCheckpoint: this.maxRowNumForCheckpoint,
          mode: this.mode,
          isSplit: this.isSplit,
          splitPk: this.splitPk,
          jobSpeedChannel:this.jobSpeedChannel,
          isSqlStatement: this.isSqlStatement,
          customSql: editor?editor.getValue():'',
          batchSize:this.batchSize
        });
      },

      _destroyEditor () {
        if (editor) {
          editor.setValue("")
          editor.toTextArea() // Uninstall
          editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
          editor.off($('.code-sql-mirror'), 'changes', this.changes)
        }
      },

      _destroyJsonEditor () {
         if (jsonEditor) {
          jsonEditor.toTextArea() // Uninstall
          jsonEditor.off($('.code-json-mirror'), 'keypress', this.keypress)
          jsonEditor.off($('.code-json-mirror'), 'changes', this.changes)
        }
      }
    },
    created () {
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.mode = o.params.mode || ''
        // backfill
        if(o.params.customConfig == 0) {
          this.customConfig = 0
          this.enable = false
         this.polling = o.params.polling || false
         this.pollingInterval = o.params.pollingInterval /1000 || 5
         this.increColumn = o.params.increColumn || ''
         this.startLocation = o.params.startLocation || ''
          this.writeMode = o.params.writeMode || 'INSERT'
          this.uniqueKey = o.params.uniqueKey || []
          this.dsType = o.params.dsType || ''
          this.datasource = o.params.dataSource || ''
          this.dtType = o.params.dtType || ''
          this.datatarget = o.params.dataTarget || ''
          this.sourceTable = o.params.sourceTable || ''
          this.sourceColumns = o.params.sourceColumns || []
          this.targetTable = o.params.targetTable || ''
          this.targetColumns = o.params.targetColumns || []
          this.jobSpeedByte = o.params.jobSpeedByte / 1024 || 0
          this.preStatements = o.params.preStatements || []
          this.postStatements = o.params.postStatements || []
          this.isStream = o.params.isStream || false
          this.isRestore = o.params.isRestore || false
          this.restoreColumnIndex = o.params.restoreColumnIndex || -1
          this.restoreColumnName = o.params.restoreColumnName || ''
          this.maxRowNumForCheckpoint = o.params.maxRowNumForCheckpoint || 0
          this.splitPk = o.params.splitPk || ''
          this.isSplit = o.params.isSplit || false
          this.jobSpeedChannel = o.params.jobSpeedChannel || 1
          this.isSqlStatement = o.params.isSqlStatement || false
          this.customSql = o.params.customSql || ''
          this.batchSize = o.params.batchSize || 1024
        } else {
          this.customConfig = 1
          this.enable = true
          this.json = o.params.json || []
          this.localParams = o.params.localParams || ''
        }
      }
    },
    mounted () {
      if(this.customConfig) {
        setTimeout(() => {
          this._handlerJsonEditor()
        }, 350)
      } else {
        if(this.isSqlStatement){
          setTimeout(() => {
            this._handlerEditor()
          }, 350)
        }
      }
    },
    destroyed () {
      /**
       * Destroy the editor instance
       */
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-sql-mirror'), 'keypress', this.keypress)
      }

      /**
       * Destroy the editor instance
       */
      if (jsonEditor) {
        jsonEditor.toTextArea() // Uninstall
        jsonEditor.off($('.code-json-mirror'), 'keypress', this.keypress)
      }
    },
    watch: {
      //Watch the cacheParams
      cacheParams (val) {
        this._cacheParams();
      }
    },
    computed: {
      cacheParams () {
        return {
          polling: this.polling,
          pollingInterval: this.pollingInterval * 1000,
          increColumn: this.increColumn,
          startLocation: this.startLocation,
          writeMode: this.writeMode,
          uniqueKey: this.uniqueKey,
          dsType: this.dsType,
          dataSource: this.rtDatasource,
          dtType: this.dtType,
          dataTarget: this.rtDatatarget,
          sourceTable: this.sourceTable,
          targetTable: this.targetTable,
          sourceColumns: this.sourceColumns,
          targetColumns: this.targetColumns,
          jobSpeedByte: this.jobSpeedByte * 1024,
          preStatements: this.preStatements,
          postStatements: this.postStatements,
          isStream: this.isStream,
          isRestore: this.isRestore,
          restoreColumnIndex: this.restoreColumnIndex,
          restoreColumnName: this.restoreColumnName,
          maxRowNumForCheckpoint: this.maxRowNumForCheckpoint,
          mode: this.mode,
          isSplit: this.isSplit,
          splitPk: this.splitPk,
          jobSpeedChannel: this.jobSpeedChannel,
          isSqlStatement: this.isSqlStatement,
          customSql: this.customSql,
          batchSize:this.batchSize
        }
      }
    },
    components: { mListBox, mDatasource, mLocalParams, mLocalColumns, mStatementList, mSelectInput }
  }
</script>
