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
  <div class="dataquality-model">
    <m-list-box>
      <div slot="text">{{$t('Rule Name')}}</div>
      <div slot="content">
        <el-select
            style="width: 130px;"
            size="small"
            v-model="ruleId"
            :disabled="isDetails"
            @change="_handleRuleChange">
          <el-option
            v-for="rule in ruleNameList"
            :key="rule.value"
            :value="rule.value"
            :label="rule.label">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
    <div class="form-box">
        <form-create v-model="fApi" :rule="rule" :option="option"></form-create>
    </div>
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <el-radio-group v-model="deployMode" size="small">
          <el-radio :label="'cluster'" :disabled="isDetails"></el-radio>
          <el-radio :label="'client'" :disabled="isDetails"></el-radio>
          <el-radio :label="'local'" :disabled="isDetails"></el-radio>
        </el-radio-group>
      </div>
    </m-list-box>
    <m-list-4-box>
      <div slot="text">{{$t('Driver Cores')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="driverCores"
          :placeholder="$t('Please enter Driver cores')">
        </el-input>
      </div>
      <div slot="text-2">{{$t('Driver Memory')}}</div>
      <div slot="content-2">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="driverMemory"
          :placeholder="$t('Please enter Driver memory')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-4-box>
      <div slot="text">{{$t('Executor Number')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="numExecutors"
          :placeholder="$t('Please enter Executor number')">
        </el-input>
      </div>
      <div slot="text-2">{{$t('Executor Memory')}}</div>
      <div slot="content-2">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="executorMemory"
          :placeholder="$t('Please enter Executor memory')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-4-box>
      <div slot="text">{{$t('Executor Cores')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="executorCores"
          :placeholder="$t('Please enter Executor cores')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-box>
      <div slot="text">{{$t('Option Parameters')}}</div>
      <div slot="content">
        <el-input
            :disabled="isDetails"
            :autosize="{minRows:2}"
            type="textarea"
            size="small"
            v-model="others"
            :placeholder="$t('Please enter option parameters')">
        </el-input>
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
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mLocalParams from './_source/localParams'
  import mListBox from './_source/listBox'
  import mList4Box from './_source/list4Box'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'dataquality',
    data () {
      return {
        // Deployment method
        deployMode: 'cluster',
        // Custom function
        localParams: [],
        // Driver Number of cores
        driverCores: 1,
        // Driver Number of memory
        driverMemory: '512M',
        // Executor Number
        numExecutors: 2,
        // Executor Number of memory
        executorMemory: '2G',
        // Executor Number of cores
        executorCores: 2,
        // Other parameters
        others: '--conf spark.yarn.maxAppAttempts=1',

        ruleId: 0,
        // ruleNameList
        ruleNameList: [],

        sparkParam: {},

        inputEntryValueMap: {},

        normalizer (node) {
          return {
            label: node.name
          }
        },
        rule: [],
        fApi: {},
        option: {
          resetBtn: false,
          submitBtn: false,
          row: {
            gutter: 0
          }
        }
      }
    },
    props: {
      backfillItem: Object
    },
    mixins: [disabledState],
    methods: {

      _handleRuleChange (o) {
        this._getRuleInputEntryList(o)
      },
      /**
       * Get the rule input entry list
       */
      _getRuleInputEntryList (ruleId) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getRuleInputEntryList', ruleId).then(res => {
            this.rule = JSON.parse(res.data).map(item => {
              if (item.title.indexOf('$t') !== -1) {
                item.title = this.$t(item.field)
              }

              if (item.field === 'mapping_columns') {
                item.props.rules.forEach((pro) => {
                  pro.title = this.$t(pro.title)
                })
              }

              this._replaceOptionLabel(item, 'check_type')
              this._replaceOptionLabel(item, 'failure_strategy')
              this._replaceOptionLabel(item, 'comparison_type')

              item.props = item.props || {}
              return item
            })
            this.fApi.on('src_connector_type-change', this._srcConnectorTypeChange)
            this.fApi.on('target_connector_type-change', this._targetConnectorTypeChange)
            this.fApi.on('writer_connector_type-change', this._writerConnectorTypeChange)
            this.fApi.on('src_datasource_id-change', this._srcDatasourceIdChange)
            this.fApi.on('target_datasource_id-change', this._targetDatasourceIdChange)
            this.fApi.on('src_table-change', this._srcTableChange)
            this.fApi.on('target_table-change', this._targetTableChange)
            this.fApi.on('comparison_type-change', this._comparisonTypeChange)
          })
        })
      },

      _replaceOptionLabel (item, field) {
        if (item.field === field) {
          item.options.forEach((op) => {
            op.label = this.$t(op.label)
          })
        }
      },

      _comparisonTypeChange () {
        if (this.fApi.getValue('comparison_type') === 1) {
          this.fApi.append(this._getComparisonNameInput(), 'comparison_type')
        } else {
          this.fApi.removeField('comparison_name')
        }
      },

      _getComparisonNameInput () {
        return this.$formCreate.maker.input($t('fix_value'), 'comparison_name', this.inputEntryValueMap.comparison_name)
      },

      _srcConnectorTypeChange () {
        this._updateSelectFieldOptions(
          'dag/getDatasourceOptionsById',
          'src_connector_type',
          'src_connector_type',
          this.fApi.getValue('src_connector_type'),
          'src_datasource_id', ['src_datasource_id', 'src_table', 'src_field'])
      },

      _targetConnectorTypeChange () {
        this._updateSelectFieldOptions(
          'dag/getDatasourceOptionsById',
          'target_connector_type',
          'target_connector_type',
          this.fApi.getValue('target_connector_type'),
          'target_datasource_id', ['target_datasource_id', 'target_table', 'target_field'])
      },

      _writerConnectorTypeChange () {
        this._updateSelectFieldOptions(
          'dag/getDatasourceOptionsById',
          'writer_connector_type',
          'writer_connector_type',
          this.fApi.getValue('writer_connector_type'),
          'writer_datasource_id', ['writer_datasource_id'])
      },

      _srcDatasourceIdChange () {
        this._updateSelectFieldOptions(
          'dag/getTablesById',
          'src_datasource_id',
          'src_datasource_id',
          { datasourceId: this.fApi.getValue('src_datasource_id') },
          'src_table', ['src_table', 'src_field'])
      },

      _targetDatasourceIdChange () {
        this._updateSelectFieldOptions(
          'dag/getTablesById',
          'target_datasource_id',
          'target_datasource_id',
          { datasourceId: this.fApi.getValue('target_datasource_id') },
          'target_table', ['target_table', 'target_field'])
      },

      _targetTableChange () {
        this._updateSelectFieldOptions(
          'dag/getTableColumnsByIdAndName',
          'target_table',
          'target_table',
          { datasourceId: this.fApi.getValue('target_datasource_id'), tableName: this.fApi.getValue('target_table') },
          'target_field', ['target_field'])
      },

      _srcTableChange () {
        this._updateSelectFieldOptions(
          'dag/getTableColumnsByIdAndName',
          'src_table',
          'src_table',
          { datasourceId: this.fApi.getValue('src_datasource_id'), tableName: this.fApi.getValue('src_table') },
          'src_field', ['src_field'])
      },

      _updateSelectFieldOptions (url, item, type, typeValue, id, clearList) {
        if (item === type) {
          return new Promise((resolve, reject) => {
            this.store.dispatch(url, typeValue).then(res => {
              if (res.data) {
                this.fApi.updateRule(id, {
                  options: res.data
                }, true)

                if (clearList !== undefined && clearList.length > 0) {
                  clearList.forEach(i => {
                    if (i !== id) {
                      this.fApi.updateRule(i, {
                        options: []
                      }, true)
                    }
                    this.fApi.setValue(i, null)
                  })
                }
              } else {
                this.fApi.updateRule(id, {
                  options: []
                }, true)
                this.fApi.setValue(id, null)
              }
            })
          })
        }
      },

      /**
       * Get rule list
       */
      _getRuleList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getRuleList', 1).then(res => {
            this.ruleNameList = []
            res.data.forEach((item, i) => {
              let obj = {}
              if (item.name.indexOf('$t') !== -1) {
                if (item.name) {
                  obj.label = this.$t((item.name).replace('$t(', '').replace(')', ''))
                }
              } else {
                obj.label = item.name
              }

              obj.value = item.id
              this.ruleNameList.push(obj)
            })
            if (this.ruleId === 0) {
              this.ruleId = this.ruleNameList[0].value
              this._getRuleInputEntryList(this.ruleId)
            } else {
              this._getRuleInputEntryList(this.ruleId)
              window.setTimeout(() => {
                this._operateFields()
              }, 1000)
            }
          })
        })
      },

      /**
       * operate fields
       */
      _operateFields () {
        let fields = this.fApi.fields()
        fields.forEach(item => {
          if (this.inputEntryValueMap[item] !== null) {
            this._updateSelectFieldOptions(
              'dag/getDatasourceOptionsById',
              item, 'src_connector_type',
              this.inputEntryValueMap[item],
              'src_datasource_id', [])

            this._updateSelectFieldOptions(
              'dag/getDatasourceOptionsById',
              item, 'target_connector_type',
              this.inputEntryValueMap[item],
              'target_datasource_id', [])

            this._updateSelectFieldOptions(
              'dag/getDatasourceOptionsById',
              item, 'writer_connector_type',
              this.inputEntryValueMap[item],
              'writer_datasource_id', [])

            this._updateSelectFieldOptions(
              'dag/getTablesById',
              item, 'src_datasource_id',
              { datasourceId: this.inputEntryValueMap[item] },
              'src_table', [])

            this._updateSelectFieldOptions(
              'dag/getTablesById',
              item, 'target_datasource_id',
              { datasourceId: this.inputEntryValueMap[item] },
              'target_table', [])

            this._updateSelectFieldOptions(
              'dag/getTableColumnsByIdAndName',
              item, 'target_table',
              { datasourceId: this.inputEntryValueMap.target_datasource_id, tableName: this.inputEntryValueMap[item] },
              'target_field', [])

            this._updateSelectFieldOptions(
              'dag/getTableColumnsByIdAndName',
              item, 'src_table',
              { datasourceId: this.inputEntryValueMap.src_datasource_id, tableName: this.inputEntryValueMap[item] },
              'src_field', [])

            if (item === 'comparison_type') {
              if (this.inputEntryValueMap.comparison_type === 1) {
                this.fApi.append(this._getComparisonNameInput(), 'comparison_type')
              } else {
                this.fApi.removeField('comparison_name')
              }
            }

            this.fApi.setValue(item, this.inputEntryValueMap[item])
          }

          if (this.isDetails) {
            this.fApi.disabled(true, item)
          }
        })
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
        this._checkSparkParam()

        this.sparkParam = {
          deployMode: this.deployMode,
          driverCores: this.driverCores,
          driverMemory: this.driverMemory,
          numExecutors: this.numExecutors,
          executorMemory: this.executorMemory,
          executorCores: this.executorCores,
          others: this.others
        }

        this.inputEntryValueMap = this.fApi.formData()

        let fields = this.fApi.fields()
        try {
          fields.forEach(item => {
            this.fApi.setValue(item, this.inputEntryValueMap[item])
            if (item !== 'mapping_columns') {
              this.fApi.validateField(item, (errMsg) => {
                if (errMsg) {
                  throw new Error(errMsg)
                }
              })
            } else {
              this.inputEntryValueMap.mapping_columns = JSON.stringify(this.inputEntryValueMap.mapping_columns)
            }
          })
        } catch (error) {
          this.$message.warning(error.message)
          return false
        }

        // storage
        this.$emit('on-params', {
          ruleId: this.ruleId,
          sparkParameters: this.sparkParam,
          localParams: this.localParams,
          ruleInputParameter: this.inputEntryValueMap
        })
        return true
      },

      _checkSparkParam () {
        if (!this.numExecutors) {
          this.$message.warning(`${i18n.$t('Please enter Executor number')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.numExecutors))) {
          this.$message.warning(`${i18n.$t('The Executor Number should be a positive integer')}`)
          return false
        }

        if (!this.executorMemory) {
          this.$message.warning(`${i18n.$t('Please enter Executor memory')}`)
          return false
        }

        if (!_.isNumber(parseInt(this.executorMemory))) {
          this.$message.warning(`${i18n.$t('Memory should be a positive integer')}`)
          return false
        }

        if (!this.executorCores) {
          this.$message.warning(`${i18n.$t('Please enter Executor cores')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.executorCores))) {
          this.$message.warning(`${i18n.$t('Core number should be positive integer')}`)
          return false
        }

        if (!this.driverCores) {
          this.$message.warning(`${i18n.$t('Please enter Driver cores')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.driverCores))) {
          this.$message.warning(`${i18n.$t('Core number should be positive integer')}`)
          return false
        }

        if (!this.driverMemory) {
          this.$message.warning(`${i18n.$t('Please enter Driver memory')}`)
          return false
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
      },

      _isArrayFn (o) {
        return Object.prototype.toString.call(o) === '[object Array]'
      }
    },

    watch: {
      // Watch the cacheParams
      cacheParams (val) {
        this.$emit('on-cache-params', val)
      }
    },

    computed: {

      cacheParams () {
        return {
          ruleId: this.ruleId,
          sparkParameters: this.sparkParam,
          localParams: this.localParams,
          ruleInputParameter: this.inputEntryValueMap
        }
      }
    },

    created () {
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.deployMode = o.params.sparkParameters.deployMode || ''
        this.driverCores = o.params.sparkParameters.driverCores || 1
        this.driverMemory = o.params.sparkParameters.driverMemory || '512M'
        this.numExecutors = o.params.sparkParameters.numExecutors || 2
        this.executorMemory = o.params.sparkParameters.executorMemory || '2G'
        this.executorCores = o.params.sparkParameters.executorCores || 2
        this.others = o.params.sparkParameters.others
        this.ruleId = o.params.ruleId || 0
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }

        this.inputEntryValueMap = o.params.ruleInputParameter
        if (this.inputEntryValueMap.mapping_columns) {
          this.inputEntryValueMap.mapping_columns = JSON.parse(this.inputEntryValueMap.mapping_columns)
        }
      }
    },

    mounted () {
      this._getRuleList()
    },

    components: {
      mLocalParams,
      mListBox,
      mList4Box
    }

  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .form-box{
    margin-left: 13px;
    margin-right: 25px;
  }
  .form-box .el-form-item{
    margin-bottom: -1px
  }

  .form-box .form-create .el-form-item{
    margin-bottom: 0px
  }
</style>
