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
      <div slot="text">{{$t('Program Type')}}</div>
      <div slot="content">
        <x-select
                style="width: 130px;"
                v-model="programType"
                :disabled="isDetails">
          <x-option
                  v-for="city in programTypeList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </x-option>
        </x-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Spark Version')}}</div>
      <div slot="content">
        <x-select
                style="width: 130px;"
                v-model="sparkVersion"
                :disabled="isDetails">
          <x-option
                  v-for="city in sparkVersionList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </x-option>
        </x-select>
      </div>
    </m-list-box>
    <m-list-box v-if="programType !== 'PYTHON'">
      <div slot="text">{{$t('Main class')}}</div>
      <div slot="content">
        <x-input
                :disabled="isDetails"
                type="input"
                v-model="mainClass"
                :placeholder="$t('Please enter main class')"
                autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Main jar package')}}</div>
      <div slot="content">
        <treeselect v-model="mainJar" :options="mainJarLists" :disable-branch-nodes="true" :normalizer="normalizer" :placeholder="$t('Please enter main jar package')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <x-radio-group v-model="deployMode">
          <x-radio :label="'cluster'" :disabled="isDetails"></x-radio>
          <x-radio :label="'client'" :disabled="isDetails"></x-radio>
          <x-radio :label="'local'" :disabled="isDetails"></x-radio>
        </x-radio-group>
      </div>
    </m-list-box>
    <div class="list-box-4p">
      <div class="clearfix list">
        <span class="sp1">{{$t('Driver core number')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="driverCores"
                  :placeholder="$t('Please enter driver core number')"
                  style="width: 200px;"
                  autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('Driver memory use')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="driverMemory"
                  :placeholder="$t('Please enter driver memory use')"
                  style="width: 186px;"
                  autocomplete="off">
        </x-input>
        </span>
      </div>
      <div class="clearfix list">
        <span class="sp1">{{$t('Number of Executors')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="numExecutors"
                  :placeholder="$t('Please enter the number of Executor')"
                  style="width: 200px;"
                  autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('Executor memory')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="executorMemory"
                  :placeholder="$t('Please enter the Executor memory')"
                  style="width: 186px;"
                  autocomplete="off">
        </x-input>
        </span>
      </div>
      <div class="clearfix list">
        <span class="sp1">{{$t('Executor core number')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="executorCores"
                  :placeholder="$t('Please enter Executor core number')"
                  style="width: 200px;"
                  autocomplete="off">
          </x-input>
        </span>
      </div>
    </div>
    <m-list-box>
      <div slot="text">{{$t('Command-line parameters')}}</div>
      <div slot="content">
        <x-input
                :autosize="{minRows:2}"
                :disabled="isDetails"
                type="textarea"
                v-model="mainArgs"
                :placeholder="$t('Please enter Command-line parameters')"
                autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Other parameters')}}</div>
      <div slot="content">
        <x-input
                :disabled="isDetails"
                :autosize="{minRows:2}"
                type="textarea"
                v-model="others"
                :placeholder="$t('Please enter other parameters')">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Resources')}}</div>
      <div slot="content">
        <treeselect v-model="resourceList" :multiple="true" :options="mainJarList" :normalizer="normalizer" :placeholder="$t('Please select resources')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
      </div>
    </m-list-box>
    <!-- <m-list-box>
      <div slot="text">{{$t('Resources')}}</div>
      <div slot="content">
        <m-resources
                ref="refResources"
                @on-resourcesData="_onResourcesData"
                @on-cache-resourcesData="_onCacheResourcesData"
                :resource-list="resourceList">
        </m-resources>
      </div>
    </m-list-box> -->
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
  import mResources from './_source/resources'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'spark',
    data () {
      return {
        // Main function class
        mainClass: '',
        // Master jar package
        mainJar: null,
        // Master jar package(List)
        mainJarLists: [],
        mainJarList: [],
        // Deployment method
        deployMode: 'cluster',
        // Resource(list)
        resourceList: [],
        // Cache ResourceList
        cacheResourceList: [],
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
        // Command line argument
        mainArgs: '',
        // Other parameters
        others: '',
        // Program type
        programType: 'SCALA',
        // Program type(List)
        programTypeList: [{ code: 'JAVA' }, { code: 'SCALA' }, { code: 'PYTHON' }],
        // Spark version
        sparkVersion: 'SPARK2',
        // Spark version(LIst)
        sparkVersionList: [{ code: 'SPARK2' }, { code: 'SPARK1' }],
        normalizer(node) {
          return {
            label: node.name
          }
        }
      }
    },
    props: {
      backfillItem: Object
    },
    mixins: [disabledState],
    methods: {
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      /**
       * return resourceList
       */
      _onResourcesData (a) {
        this.resourceList = a
      },
      /**
       * cache resourceList
       */
      _onCacheResourcesData (a) {
        this.cacheResourceList = a
      },
      diGuiTree(item) {  // Recursive convenience tree structure
        item.forEach(item => {
          item.children === '' || item.children === undefined || item.children === null || item.children.length === 0?　　　　　　　　
            delete item.children : this.diGuiTree(item.children);
        })
      },
      /**
       * verification
       */
      _verification () {
        if (this.programType !== 'PYTHON' && !this.mainClass) {
          this.$message.warning(`${i18n.$t('Please enter main class')}`)
          return false
        }

        if (!this.mainJar) {
          this.$message.warning(`${i18n.$t('Please enter main jar package')}`)
          return false
        }

        if (!this.numExecutors) {
          this.$message.warning(`${i18n.$t('Please enter the number of Executor')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.numExecutors))) {
          this.$message.warning(`${i18n.$t('The number of Executors should be a positive integer')}`)
          return false
        }

        if (!this.executorMemory) {
          this.$message.warning(`${i18n.$t('Please enter the Executor memory')}`)
          return false
        }

        if (!this.executorMemory) {
          this.$message.warning(`${i18n.$t('Please enter the Executor memory')}`)
          return false
        }

        if (!_.isNumber(parseInt(this.executorMemory))) {
          this.$message.warning(`${i18n.$t('Memory should be a positive integer')}`)
          return false
        }

        if (!this.executorCores) {
          this.$message.warning(`${i18n.$t('Please enter ExecutorPlease enter Executor core number')}`)
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
        // Process resourcelist
        let dataProcessing= _.map(this.resourceList, v => {
          return {
            id: v
          }
        })

        // storage
        this.$emit('on-params', {
          mainClass: this.mainClass,
          mainJar: {
            id: this.mainJar
          },
          deployMode: this.deployMode,
          resourceList: dataProcessing,
          localParams: this.localParams,
          driverCores: this.driverCores,
          driverMemory: this.driverMemory,
          numExecutors: this.numExecutors,
          executorMemory: this.executorMemory,
          executorCores: this.executorCores,
          mainArgs: this.mainArgs,
          others: this.others,
          programType: this.programType,
          sparkVersion: this.sparkVersion
        })
        return true
      }
    },
    watch: {
      // Listening type
      programType (type) {
        if (type === 'PYTHON') {
          this.mainClass = ''
        }
      },
      //Watch the cacheParams
      cacheParams (val) {
        this.$emit('on-cache-params', val)
      }
    },
    computed: {
      cacheParams () {
        return {
          mainClass: this.mainClass,
          mainJar: {
            id: this.mainJar
          },
          deployMode: this.deployMode,
          resourceList: _.map(this.resourceList, v => {
            return {id: v}
          }),
          localParams: this.localParams,
          driverCores: this.driverCores,
          driverMemory: this.driverMemory,
          numExecutors: this.numExecutors,
          executorMemory: this.executorMemory,
          executorCores: this.executorCores,
          mainArgs: this.mainArgs,
          others: this.others,
          programType: this.programType,
          sparkVersion: this.sparkVersion
        }
      }
    },
    created () {
        let item = this.store.state.dag.resourcesListS
        let items = this.store.state.dag.resourcesListJar
        this.diGuiTree(item)
        this.diGuiTree(items)
        this.mainJarList = item
        this.mainJarLists = items
        let o = this.backfillItem

        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.mainClass = o.params.mainClass || ''
          this.mainJar = o.params.mainJar && o.params.mainJar.res ? o.params.mainJar.res : ''
          this.deployMode = o.params.deployMode || ''
          this.driverCores = o.params.driverCores || 1
          this.driverMemory = o.params.driverMemory || '512M'
          this.numExecutors = o.params.numExecutors || 2
          this.executorMemory = o.params.executorMemory || '2G'
          this.executorCores = o.params.executorCores || 2
          this.mainArgs = o.params.mainArgs || ''
          this.others = o.params.others
          this.programType = o.params.programType || 'SCALA'
          this.sparkVersion = o.params.sparkVersion || 'SPARK2'

          // backfill resourceList
          let resourceList = o.params.resourceList || []
          if (resourceList.length) {
            this.resourceList = _.map(resourceList, v => {
            return v.id
          })
            this.cacheResourceList = resourceList
          }

          // backfill localParams
          let localParams = o.params.localParams || []
          if (localParams.length) {
            this.localParams = localParams
          }
        }
    },
    mounted () {

    },
    components: { mLocalParams, mListBox, mResources, Treeselect }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .spark-model {
    .list-box-4p {
      .list {
        margin-bottom: 14px;
        .sp1 {
          float: left;
          width: 112px;
          text-align: right;
          margin-right: 10px;
          font-size: 14px;
          color: #777;
          display: inline-block;
          padding-top: 6px;
        }
        .sp2 {
          float: left;
          margin-right: 4px;
        }
        .sp3 {
          width: 176px;
        }
      }
    }
  }
</style>
