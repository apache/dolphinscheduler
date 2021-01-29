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
      <div slot="text">{{$t('Custom Job')}}</div>
      <div slot="content">
        <el-switch size="small" v-model="isCustomTask" @change="_onSwitch" :disabled="isDetails"></el-switch>
      </div>
    </m-list-box>
    <m-list-box v-show="isCustomTask">
      <div slot="text">{{$t('Custom Script')}}</div>
      <div slot="content">
        <div class="from-mirror">
          <textarea id="code-shell-mirror" name="code-shell-mirror" style="opacity: 0;"></textarea>
        </div>
      </div>
    </m-list-box>
    <template v-if="!isCustomTask">
      <m-list-box>
        <div slot="text">{{$t('Sqoop Job Name')}}</div>
        <div slot="content">
          <el-input :disabled="isDetails" size="small" type="text" v-model="jobName" :placeholder="$t('Please enter Job Name(required)')"></el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{$t('Direct')}}</div>
        <div slot="content">
          <el-select
            style="width: 130px;"
            size="small"
            v-model="modelType"
            :disabled="isDetails"
            @change="_handleModelTypeChange">
            <el-option
              v-for="city in modelTypeList"
              :key="city.code"
              :value="city.code"
              :label="city.code">
            </el-option>
          </el-select>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text" style="width: 110px;">{{$t('Hadoop Custom Params')}}</div>
        <div slot="content">
          <m-local-params
            ref="refMapColumnHadoopParams"
            @on-local-params="_onHadoopCustomParams"
            :udp-list="hadoopCustomParams"
            :hide="false">
          </m-local-params>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text" style="width: 100px;">{{$t('Sqoop Advanced Parameters')}}</div>
        <div slot="content">
          <m-local-params
            ref="refMapColumnAdvancedParams"
            @on-local-params="_onSqoopAdvancedParams"
            :udp-list="sqoopAdvancedParams"
            :hide="false">
          </m-local-params>
        </div>
      </m-list-box>

      <template>
        <m-list-box>
          <div slot="text" style="font-weight:bold">{{$t('Data Source')}}</div>
        </m-list-box>
        <hr style="margin-left: 60px;">
        <m-list-box>
          <div slot="text">{{$t('Type')}}</div>
          <div slot="content">
            <el-select
              style="width: 130px;"
              size="small"
              v-model="sourceType"
              :disabled="isDetails"
              @change="_handleSourceTypeChange">
              <el-option
                v-for="city in sourceTypeList"
                :key="city.code"
                :value="city.code"
                :label="city.code">
              </el-option>
            </el-select>
          </div>
        </m-list-box>

        <template v-if="sourceType ==='MYSQL'">

          <m-list-box>
            <div slot="text">{{$t('Datasource')}}</div>
            <div slot="content">
              <m-datasource
                ref="refSourceDs"
                @on-dsData="_onSourceDsData"
                :data="{type:sourceMysqlParams.srcType,
                      typeList: [{id: 0, code: 'MYSQL', disabled: false}],
                      datasource:sourceMysqlParams.srcDatasource }"
              >
              </m-datasource>
            </div>
          </m-list-box>

          <m-list-box>
            <div slot="text">{{$t('ModelType')}}</div>
            <div slot="content">
              <el-radio-group v-model="srcQueryType" size="small" @change="_handleQueryType">
                <el-radio label="0">{{$t('Form')}}</el-radio>
                <el-radio label="1">SQL</el-radio>
              </el-radio-group>
            </div>
          </m-list-box>

          <template v-if="sourceMysqlParams.srcQueryType=='0'">

            <m-list-box>
              <div slot="text">{{$t('Table')}}</div>
              <div slot="content">
                <el-input
                  :disabled="isDetails"
                  type="text"
                  size="small"
                  v-model="sourceMysqlParams.srcTable"
                  :placeholder="$t('Please enter Mysql Table(required)')">
                </el-input>
              </div>
            </m-list-box>

            <m-list-box>
              <div slot="text">{{$t('ColumnType')}}</div>
              <div slot="content">
                <el-radio-group v-model="sourceMysqlParams.srcColumnType" size="small" style="vertical-align: sub;">
                  <el-radio label="0">{{$t('All Columns')}}</el-radio>
                  <el-radio label="1">{{$t('Some Columns')}}</el-radio>
                </el-radio-group>
              </div>
            </m-list-box>

            <m-list-box v-if="sourceMysqlParams.srcColumnType=='1'">
              <div slot="text">{{$t('Column')}}</div>
              <div slot="content">
                <el-input
                  :disabled="isDetails"
                  type="text"
                  size="small"
                  v-model="sourceMysqlParams.srcColumns"
                  :placeholder="$t('Please enter Columns (Comma separated)')">
                </el-input>
              </div>
            </m-list-box>
          </template>
        </template>
      </template>

      <template v-if="sourceType=='HIVE'">
        <m-list-box>
          <div slot="text">{{$t('Database')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="sourceHiveParams.hiveDatabase"
              :placeholder="$t('Please enter Hive Database(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Table')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="sourceHiveParams.hiveTable"
              :placeholder="$t('Please enter Hive Table(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Hive partition Keys')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="sourceHiveParams.hivePartitionKey"
              :placeholder="$t('Please enter Hive Partition Keys')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Hive partition Values')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="sourceHiveParams.hivePartitionValue"
              :placeholder="$t('Please enter Hive Partition Values')">
            </el-input>
          </div>
        </m-list-box>
      </template>

      <template v-if="sourceType=='HDFS'">
        <m-list-box>
          <div slot="text">{{$t('Export Dir')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="sourceHdfsParams.exportDir"
              :placeholder="$t('Please enter Export Dir(required)')">
            </el-input>
          </div>
        </m-list-box>
      </template>

    <m-list-box v-show="srcQueryType === '1' && sourceType ==='MYSQL'">
      <div slot="text">{{$t('SQL Statement')}}</div>
      <div slot="content">
        <div class="from-mirror">
          <textarea
                  id="code-sqoop-mirror"
                  name="code-sqoop-mirror"
                  style="opacity: 0;">
          </textarea>
          <a class="ans-modal-box-max">
            <em class="el-icon-full-screen" @click="setEditorVal"></em>
          </a>
        </div>
      </div>
    </m-list-box>

      <template>
        <m-list-box v-show="sourceType === 'MYSQL'">
          <div slot="text">{{$t('Map Column Hive')}}</div>
          <div slot="content">
            <m-local-params
              ref="refMapColumnHiveParams"
              @on-local-params="_onMapColumnHive"
              :udp-list="sourceMysqlParams.mapColumnHive"
              :hide="false">
            </m-local-params>
          </div>
        </m-list-box>
        <m-list-box v-show="sourceType === 'MYSQL'">
          <div slot="text">{{$t('Map Column Java')}}</div>
          <div slot="content">
            <m-local-params
              ref="refMapColumnJavaParams"
              @on-local-params="_onMapColumnJava"
              :udp-list="sourceMysqlParams.mapColumnJava"
              :hide="false">
            </m-local-params>
          </div>
        </m-list-box>
      </template>

      <m-list-box>
        <div slot="text" style="font-weight:bold">{{$t('Data Target')}}</div>
      </m-list-box>
      <hr style="margin-left: 60px;">

      <m-list-box>
        <div slot="text">{{$t('Type')}}</div>
        <div slot="content">
          <el-select
            style="width: 130px;"
            size="small"
            v-model="targetType"
            :disabled="isDetails">
            <el-option
              v-for="city in targetTypeList"
              :key="city.code"
              :value="city.code"
              :label="city.code">
            </el-option>
          </el-select>
        </div>
      </m-list-box>
      <div v-show="targetType==='HIVE'">
        <m-list-box>
          <div slot="text">{{$t('Database')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHiveParams.hiveDatabase"
              :placeholder="$t('Please enter Hive Database(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Table')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHiveParams.hiveTable"
              :placeholder="$t('Please enter Hive Table(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('CreateHiveTable')}}</div>
          <div slot="content">
            <el-switch v-model="targetHiveParams.createHiveTable" size="small"></el-switch>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('DropDelimiter')}}</div>
          <div slot="content">
            <el-switch v-model="targetHiveParams.dropDelimiter" size="small"></el-switch>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('OverWriteSrc')}}</div>
          <div slot="content">
            <el-switch v-model="targetHiveParams.hiveOverWrite" size="small"></el-switch>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('ReplaceDelimiter')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHiveParams.replaceDelimiter"
              :placeholder="$t('Please enter Replace Delimiter')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Hive partition Keys')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHiveParams.hivePartitionKey"
              :placeholder="$t('Please enter Hive Partition Keys')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Hive partition Values')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHiveParams.hivePartitionValue"
              :placeholder="$t('Please enter Hive Partition Values')">
            </el-input>
          </div>
        </m-list-box>
      </div>
      <div v-show="targetType==='HDFS'">
        <m-list-box>
          <div slot="text">{{$t('Target Dir')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHdfsParams.targetPath"
              :placeholder="$t('Please enter Target Dir(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('DeleteTargetDir')}}</div>
          <div slot="content">
            <el-switch v-model="targetHdfsParams.deleteTargetDir" size="small"></el-switch>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('CompressionCodec')}}</div>
          <div slot="content">
            <el-radio-group v-model="targetHdfsParams.compressionCodec" size="small">
              <el-radio label="snappy">snappy</el-radio>
              <el-radio label="lzo">lzo</el-radio>
              <el-radio label="gzip">gzip</el-radio>
              <el-radio label="">no</el-radio>
            </el-radio-group>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('FileType')}}</div>
          <div slot="content">
            <el-radio-group v-model="targetHdfsParams.fileType" size="small">
              <el-radio label="--as-avrodatafile">avro</el-radio>
              <el-radio label="--as-sequencefile">sequence</el-radio>
              <el-radio label="--as-textfile">text</el-radio>
              <el-radio label="--as-parquetfile">parquet</el-radio>
            </el-radio-group>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('FieldsTerminated')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHdfsParams.fieldsTerminated"
              :placeholder="$t('Please enter Fields Terminated')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('LinesTerminated')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetHdfsParams.linesTerminated"
              :placeholder="$t('Please enter Lines Terminated')">
            </el-input>
          </div>
        </m-list-box>
      </div>
      <div v-show="targetType==='MYSQL'">
        <m-list-box>
          <div slot="text">{{$t('Datasource')}}</div>
          <div slot="content">
            <m-datasource
              ref="refTargetDs"
              @on-dsData="_onTargetDsData"
              :data="{ type:targetMysqlParams.targetType,
                      typeList: [{id: 0, code: 'MYSQL', disabled: false}],
                      datasource:targetMysqlParams.targetDatasource }"
            >
            </m-datasource>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Table')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetMysqlParams.targetTable"
              :placeholder="$t('Please enter Mysql Table(required)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('Column')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetMysqlParams.targetColumns"
              :placeholder="$t('Please enter Columns (Comma separated)')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('FieldsTerminated')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetMysqlParams.fieldsTerminated"
              :placeholder="$t('Please enter Fields Terminated')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('LinesTerminated')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetMysqlParams.linesTerminated"
              :placeholder="$t('Please enter Lines Terminated')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box>
          <div slot="text">{{$t('IsUpdate')}}</div>
          <div slot="content">
            <el-switch v-model="targetMysqlParams.isUpdate" size="small"></el-switch>
          </div>
        </m-list-box>
        <m-list-box v-show="targetMysqlParams.isUpdate">
          <div slot="text">{{$t('UpdateKey')}}</div>
          <div slot="content">
            <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="targetMysqlParams.targetUpdateKey"
              :placeholder="$t('Please enter Update Key')">
            </el-input>
          </div>
        </m-list-box>
        <m-list-box v-show="targetMysqlParams.isUpdate">
          <div slot="text">{{$t('UpdateMode')}}</div>
          <div slot="content">
            <el-radio-group v-model="targetMysqlParams.targetUpdateMode" size="small">
              <el-radio label="updateonly">{{$t('OnlyUpdate')}}</el-radio>
              <el-radio label="allowinsert">{{$t('AllowInsert')}}</el-radio>
            </el-radio-group>
          </div>
        </m-list-box>

      </div>
      <m-list-box>
        <div slot="text">{{$t('Concurrency')}}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="concurrency"
            :placeholder="$t('Please enter Concurrency')">
          </el-input>
        </div>
      </m-list-box>
    </template>
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
  import mListBox from './_source/listBox'
  import mScriptBox from './_source/scriptBox'
  import mDatasource from './_source/datasource'
  import mLocalParams from './_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editor
  let shellEditor

  export default {
    name: 'sql',
    data () {
      return {
        /**
         * Is Custom Task
         */
        isCustomTask: false,
        /**
         * Customer Params
         */
        localParams: [],

        /**
         * Hadoop Custom Params
         */
        hadoopCustomParams: [],

        /**
         * Sqoop Advanced Params
         */
        sqoopAdvancedParams: [],
        /**
         * script
         */
        customShell: '',
        /**
         * task name
         */
        jobName: '',
        /**
         * mysql query type
         */
        srcQueryType: '1',
        /**
         * source data source
         */
        srcDatasource: '',
        /**
         * target data source
         */
        targetDatasource: '',
        /**
         * concurrency
         */
        concurrency: 1,
        /**
         * default job type
         */
        jobType: 'TEMPLATE',
        /**
         * direct model type
         */
        modelType: 'import',

        modelTypeList: [{ code: 'import' }, { code: 'export' }],

        sourceTypeList: [
          {
            code: 'MYSQL'
          }
        ],

        targetTypeList: [
          {
            code: 'HIVE'
          },
          {
            code: 'HDFS'
          }
        ],

        sourceType: 'MYSQL',
        targetType: 'HDFS',

        sourceMysqlParams: {
          srcType: 'MYSQL',
          srcDatasource: '',
          srcTable: '',
          srcQueryType: '1',
          srcQuerySql: '',
          srcColumnType: '0',
          srcColumns: '',
          srcConditionList: [],
          mapColumnHive: [],
          mapColumnJava: []
        },

        sourceHdfsParams: {
          exportDir: ''
        },

        sourceHiveParams: {
          hiveDatabase: '',
          hiveTable: '',
          hivePartitionKey: '',
          hivePartitionValue: ''
        },

        targetHdfsParams: {
          targetPath: '',
          deleteTargetDir: true,
          fileType: '--as-avrodatafile',
          compressionCodec: 'snappy',
          fieldsTerminated: '',
          linesTerminated: ''
        },

        targetMysqlParams: {
          targetType: 'MYSQL',
          targetDatasource: '',
          targetTable: '',
          targetColumns: '',
          fieldsTerminated: '',
          linesTerminated: '',
          preQuery: '',
          isUpdate: false,
          targetUpdateKey: '',
          targetUpdateMode: 'allowinsert'
        },

        targetHiveParams: {
          hiveDatabase: '',
          hiveTable: '',
          createHiveTable: false,
          dropDelimiter: false,
          hiveOverWrite: true,
          replaceDelimiter: '',
          hivePartitionKey: '',
          hivePartitionValue: ''
        },
        item: '',
        scriptBoxDialog: false
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      setEditorVal () {
        this.item = editor.getValue()
        this.scriptBoxDialog = true
      },
      getSriptBoxValue (val) {
        editor.setValue(val)
      },
      _handleQueryType (o) {
        this.sourceMysqlParams.srcQueryType = this.srcQueryType
        this._getTargetTypeList(this.sourceType)
        this.targetType = this.targetTypeList[0].code
      },

      _handleModelTypeChange (a) {
        this._getSourceTypeList(a)
        this.sourceType = this.sourceTypeList[0].code
        this._handleSourceTypeChange({ label: this.sourceType, value: this.sourceType })
      },

      _handleSourceTypeChange (a) {
        this._getTargetTypeList(a.label)
        this.targetType = this.targetTypeList[0].code
      },

      _getSourceTypeList (data) {
        switch (data) {
          case 'import':
            this.sourceTypeList = [
              {
                code: 'MYSQL'
              }
            ]
            break
          case 'export':
            this.sourceTypeList = [
              {
                code: 'HDFS'
              },
              {
                code: 'HIVE'
              }
            ]
            break
          default:
            this.sourceTypeList = [
              {
                code: 'MYSQL'
              },
              {
                code: 'HIVE'
              },
              {
                code: 'HDFS'
              }
            ]
            break
        }
      },

      _getTargetTypeList (data) {
        switch (data) {
          case 'MYSQL':
            if (this.srcQueryType === '1') {
              this.targetTypeList = [
                {
                  code: 'HDFS'
                }]
            } else {
              this.targetTypeList = [
                {
                  code: 'HIVE'
                },
                {
                  code: 'HDFS'
                }
              ]
            }
            break
          case 'HDFS':
            this.targetTypeList = [
              {
                code: 'MYSQL'
              }
            ]
            break
          case 'HIVE':
            this.targetTypeList = [
              {
                code: 'MYSQL'
              }
            ]
            break
          default:
            this.targetTypeList = [
              {
                code: 'HIVE'
              },
              {
                code: 'HDFS'
              }
            ]
            break
        }
      },

      _onMapColumnHive (a) {
        this.sourceMysqlParams.mapColumnHive = a
      },

      _onMapColumnJava (a) {
        this.sourceMysqlParams.mapColumnJava = a
      },

      /**
       * return data source
       */
      _onSourceDsData (o) {
        this.sourceMysqlParams.srcType = o.type
        this.sourceMysqlParams.srcDatasource = o.datasource
      },

      /**
       * return data source
       */
      _onTargetDsData (o) {
        this.targetMysqlParams.targetType = o.type
        this.targetMysqlParams.targetDatasource = o.datasource
      },

      /**
       * stringify the source params
       */
      _handleSourceParams () {
        let params = null
        switch (this.sourceType) {
          case 'MYSQL':
            this.sourceMysqlParams.srcQuerySql = this.sourceMysqlParams.srcQueryType === '1' && editor
              ? editor.getValue() : this.sourceMysqlParams.srcQuerySql
            params = JSON.stringify(this.sourceMysqlParams)
            break
          case 'ORACLE':
            params = JSON.stringify(this.sourceOracleParams)
            break
          case 'HDFS':
            params = JSON.stringify(this.sourceHdfsParams)
            break
          case 'HIVE':
            params = JSON.stringify(this.sourceHiveParams)
            break
          default:
            params = ''
            break
        }
        return params
      },

      /**
       * stringify the target params
       */
      _handleTargetParams () {
        let params = null
        switch (this.targetType) {
          case 'HIVE':
            params = JSON.stringify(this.targetHiveParams)
            break
          case 'HDFS':
            params = JSON.stringify(this.targetHdfsParams)
            break
          case 'MYSQL':
            params = JSON.stringify(this.targetMysqlParams)
            break
          default:
            params = ''
            break
        }

        return params
      },

      /**
       * get source params by source type
       */
      _getSourceParams (data) {
        switch (this.sourceType) {
          case 'MYSQL':
            this.sourceMysqlParams = JSON.parse(data)
            this.srcDatasource = this.sourceMysqlParams.srcDatasource
            break
          case 'ORACLE':
            this.sourceOracleParams = JSON.parse(data)
            break
          case 'HDFS':
            this.sourceHdfsParams = JSON.parse(data)
            break
          case 'HIVE':
            this.sourceHiveParams = JSON.parse(data)
            break
          default:
            break
        }
      },

      /**
       * get target params by target type
       */
      _getTargetParams (data) {
        switch (this.targetType) {
          case 'HIVE':
            this.targetHiveParams = JSON.parse(data)
            break
          case 'HDFS':
            this.targetHdfsParams = JSON.parse(data)
            break
          case 'MYSQL':
            this.targetMysqlParams = JSON.parse(data)
            this.targetDatasource = this.targetMysqlParams.targetDatasource
            break
          default:
            break
        }
      },

      /**
       * verification
       */
      _verification () {
        let sqoopParams = {
          jobType: this.jobType,
          localParams: this.localParams
        }
        if (this.jobType === 'CUSTOM') {
          if (!shellEditor.getValue()) {
            this.$message.warning(`${i18n.$t('Please enter Custom Shell(required)')}`)
            return false
          }
          sqoopParams.customShell = shellEditor.getValue()
        } else {
          if (!this.jobName) {
            this.$message.warning(`${i18n.$t('Please enter Job Name(required)')}`)
            return false
          }

          switch (this.sourceType) {
            case 'MYSQL':
              if (!this.$refs.refSourceDs._verifDatasource()) {
                return false
              }
              if (this.srcQueryType === '1') {
                if (!editor.getValue()) {
                  this.$message.warning(`${i18n.$t('Please enter a SQL Statement(required)')}`)
                  return false
                }
                this.sourceMysqlParams.srcTable = ''
                this.sourceMysqlParams.srcColumnType = '0'
                this.sourceMysqlParams.srcColumns = ''
              } else {
                if (this.sourceMysqlParams.srcTable === '') {
                  this.$message.warning(`${i18n.$t('Please enter Mysql Table(required)')}`)
                  return false
                }
                this.sourceMysqlParams.srcQuerySql = ''
                if (this.sourceMysqlParams.srcColumnType === '1' && this.sourceMysqlParams.srcColumns === '') {
                  this.$message.warning(`${i18n.$t('Please enter Columns (Comma separated)')}`)
                  return false
                }
                if (this.sourceMysqlParams.srcColumnType === '0') {
                  this.sourceMysqlParams.srcColumns = ''
                }
              }

              break
            case 'HDFS':
              if (this.sourceHdfsParams.exportDir === '') {
                this.$message.warning(`${i18n.$t('Please enter Export Dir(required)')}`)
                return false
              }
              break
            case 'HIVE':
              if (this.sourceHiveParams.hiveDatabase === '') {
                this.$message.warning(`${i18n.$t('Please enter Hive Database(required)')}`)
                return false
              }
              if (this.sourceHiveParams.hiveTable === '') {
                this.$message.warning(`${i18n.$t('Please enter Hive Table(required)')}`)
                return false
              }
              break
            default:
              break
          }

          switch (this.targetType) {
            case 'HIVE':
              if (this.targetHiveParams.hiveDatabase === '') {
                this.$message.warning(`${i18n.$t('Please enter Hive Database(required)')}`)
                return false
              }
              if (this.targetHiveParams.hiveTable === '') {
                this.$message.warning(`${i18n.$t('Please enter Hive Table(required)')}`)
                return false
              }
              break
            case 'HDFS':
              if (this.targetHdfsParams.targetPath === '') {
                this.$message.warning(`${i18n.$t('Please enter Target Dir(required)')}`)
                return false
              }
              break
            case 'MYSQL':
              if (!this.$refs.refTargetDs._verifDatasource()) {
                return false
              }

              if (this.targetMysqlParams.targetTable === '') {
                this.$message.warning(`${i18n.$t('Please enter Mysql Table(required)')}`)
                return false
              }
              break
            default:
              break
          }
          sqoopParams.jobName = this.jobName
          sqoopParams.hadoopCustomParams = this.hadoopCustomParams
          sqoopParams.sqoopAdvancedParams = this.sqoopAdvancedParams
          sqoopParams.concurrency = this.concurrency
          sqoopParams.modelType = this.modelType
          sqoopParams.sourceType = this.sourceType
          sqoopParams.targetType = this.targetType
          sqoopParams.targetParams = this._handleTargetParams()
          sqoopParams.sourceParams = this._handleSourceParams()
        }

        // storage
        this.$emit('on-params', sqoopParams)
        return true
      },

      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        this._destroyEditor()

        editor = codemirror('code-sqoop-mirror', {
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

        this.changes = () => {
          this._cacheParams()
        }

        // Monitor keyboard
        editor.on('keypress', this.keypress)

        editor.on('changes', this.changes)

        editor.setValue(this.sourceMysqlParams.srcQuerySql)

        return editor
      },

      /**
       * Processing code highlighting
       */
      _handlerShellEditor () {
        this._destroyShellEditor()

        // shellEditor
        shellEditor = codemirror('code-shell-mirror', {
          mode: 'shell',
          readOnly: this.isDetails
        })

        this.keypress = () => {
          if (!shellEditor.getOption('readOnly')) {
            shellEditor.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        shellEditor.on('keypress', this.keypress)
        shellEditor.setValue(this.customShell)

        return shellEditor
      },

      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },

      /**
       * return hadoopParams
       */
      _onHadoopCustomParams (a) {
        this.hadoopCustomParams = a
      },

      /**
       * return sqoopAdvancedParams
       */
      _onSqoopAdvancedParams (a) {
        this.sqoopAdvancedParams = a
      },

      _cacheParams () {
        this.$emit('on-cache-params', {
          concurrency: this.concurrency,
          modelType: this.modelType,
          sourceType: this.sourceType,
          targetType: this.targetType,
          sourceParams: this._handleSourceParams(),
          targetParams: this._handleTargetParams(),
          localParams: this.localParams
        })
      },

      _destroyEditor () {
        if (editor) {
          editor.toTextArea() // Uninstall
          editor.off($('.code-sqoop-mirror'), 'keypress', this.keypress)
          editor.off($('.code-sqoop-mirror'), 'changes', this.changes)
          editor = null
        }
      },
      _destroyShellEditor () {
        if (shellEditor) {
          shellEditor.toTextArea() // Uninstall
          shellEditor.off($('.code-shell-mirror'), 'keypress', this.keypress)
          shellEditor.off($('.code-shell-mirror'), 'changes', this.changes)
        }
      }
    },
    watch: {
      // Listening to sqlType
      sqlType (val) {
        if (val === 0) {
          this.showType = []
        }
        if (val !== 0) {
          this.title = ''
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
      this._destroyEditor()
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.jobType = o.params.jobType
        this.isCustomTask = false
        if (this.jobType === 'CUSTOM') {
          this.customShell = o.params.customShell
          this.isCustomTask = true
        } else {
          this.jobName = o.params.jobName
          this.hadoopCustomParams = o.params.hadoopCustomParams
          this.sqoopAdvancedParams = o.params.sqoopAdvancedParams
          this.concurrency = o.params.concurrency || 1
          this.modelType = o.params.modelType
          this.sourceType = o.params.sourceType
          this._getTargetTypeList(this.sourceType)
          this.targetType = o.params.targetType
          this._getSourceParams(o.params.sourceParams)
          this._getTargetParams(o.params.targetParams)
          this.localParams = o.params.localParams
        }
      }
    },

    mounted () {
      setTimeout(() => {
        this._handlerEditor()
      }, 200)

      setTimeout(() => {
        this._handlerShellEditor()
      }, 200)

      setTimeout(() => {
        this.srcQueryType = this.sourceMysqlParams.srcQueryType
      }, 500)
    },

    destroyed () {
      /**
       * Destroy the editor instance
       */
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-sqoop-mirror'), 'keypress', this.keypress)
        editor.off($('.code-sqoop-mirror'), 'changes', this.changes)
        editor = null
      }
    },

    computed: {
      cacheParams () {
        return {
          concurrency: this.concurrency,
          modelType: this.modelType,
          sourceType: this.sourceType,
          targetType: this.targetType,
          localParams: this.localParams,
          sourceMysqlParams: this.sourceMysqlParams,
          sourceHdfsParams: this.sourceHdfsParams,
          sourceHiveParams: this.sourceHiveParams,
          targetHdfsParams: this.targetHdfsParams,
          targetMysqlParams: this.targetMysqlParams,
          targetHiveParams: this.targetHiveParams
        }
      }
    },
    components: { mListBox, mDatasource, mLocalParams, mScriptBox }
  }
</script>
