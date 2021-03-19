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
  <div class="spark-model">
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
      <div slot="text">{{$t('Driver cores')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="driverCores"
          :placeholder="$t('Please enter Driver cores')">
        </el-input>
      </div>
      <div slot="text-2">{{$t('Driver memory')}}</div>
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
      <div slot="text-2">{{$t('Executor memory')}}</div>
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
      <div slot="text">{{$t('Executor cores')}}</div>
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
      <div slot="text">{{$t('Other parameters')}}</div>
      <div slot="content">
        <el-input
            :disabled="isDetails"
            :autosize="{minRows:2}"
            type="textarea"
            size="small"
            v-model="others"
            :placeholder="$t('Please enter other parameters')">
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
        others: '',
        // Program type
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
        this._getRuluInputEntryList(o)
      },
      /**
       * Get the rule input entry list
       */
      _getRuluInputEntryList (ruleId) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getRuleInputEntryList', ruleId).then(res => {
            this.rule = JSON.parse(res.data)
            this.fApi.on('src_connector_type-change', this.srcConnectorTypeChange)
            this.fApi.on('target_connector_type-change', this.targetConnectorTypeChange)
            this.fApi.on('writer_connector_type-change', this.writerConnectorTypeChange)
          })
        })
      },

      srcConnectorTypeChange () {
        this._updateDatasourceOptions(
          'src_connector_type',
          'src_connector_type',
          this.fApi.getValue('src_connector_type'),
          'src_datasource_id')
      },

      targetConnectorTypeChange () {
        this._updateDatasourceOptions(
          'target_connector_type',
          'target_connector_type',
          this.fApi.getValue('target_connector_type'),
          'target_datasource_id')
      },

      writerConnectorTypeChange () {
        this._updateDatasourceOptions(
          'writer_connector_type',
          'writer_connector_type',
          this.fApi.getValue('writer_connector_type'),
          'writer_datasource_id')
      },

      /**
       * Get rule list
       */
      _getRuluList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getRuleList', 1).then(res => {
            this.ruleNameList = []
            res.data.forEach((item, i) => {
              let obj = {}
              obj.label = item.name
              obj.value = item.id
              this.ruleNameList.push(obj)
            })

            if (this.ruleId === 0) {
              this.ruleId = this.ruleNameList[0].value
              this._getRuluInputEntryList(this.ruleId)
            } else {
              this._getRuluInputEntryList(this.ruleId)
              window.setTimeout(() => {
                let fields = this.fApi.fields()
                fields.forEach(item => {
                  if (this.inputEntryValueMap[item]) {
                    this._updateDatasourceOptions(item, 'src_connector_type', this.inputEntryValueMap[item], 'src_datasource_id')
                    this._updateDatasourceOptions(item, 'target_connector_type', this.inputEntryValueMap[item], 'target_datasource_id')
                    this._updateDatasourceOptions(item, 'writer_connector_type', this.inputEntryValueMap[item], 'writer_datasource_id')

                    this.fApi.setValue(item, this.inputEntryValueMap[item])
                  }

                  if (this.isDetails) {
                    this.fApi.disabled(true, item)
                  }
                })
              }, 1000)
            }
          })
        })
      },

      _updateDatasourceOptions (item, type, typeValue, id) {
        if (item === type) {
          return new Promise((resolve, reject) => {
            this.store.dispatch('dag/getDatasourceOptionsById', typeValue).then(res => {
              if (res.data) {
                this.fApi.updateRule(id, {
                  options: res.data
                }, true)
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
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },

      /**
       * verification
       */
      _verification () {
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
        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }

        this.sparkParam = {
          deployMode: this.deployMode,
          localParams: this.localParams,
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
            this.fApi.validateField(item, (errMsg) => {
              if (errMsg) {
                console.log(errMsg)
                throw new Error(errMsg)
              }
            })

            this.fApi.setValue(item, this.inputEntryValueMap[item])
          })
        } catch (error) {
          this.$message.warning(error.message)
          return false
        }

        // storage
        this.$emit('on-params', {
          ruleId: this.ruleId,
          sparkParameters: this.sparkParam,
          ruleInputParameter: this.inputEntryValueMap
        })
        return true
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
        // backfill localParams
        let localParams = o.params.sparkParameters.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }

        this.inputEntryValueMap = o.params.ruleInputParameter
      }
    },

    mounted () {
      this._getRuluList()
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
    // margin-top: -5px;
    margin-bottom: -1px
  }
</style>
