<template>
  <div class="flink-model">
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
        <x-select
                style="width: 100%;"
                :placeholder="$t('Please enter main jar package')"
                v-model="mainJar"
                filterable
                :disabled="isDetails">
          <x-option
                  v-for="city in mainJarList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </x-option>
        </x-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <x-radio-group v-model="deployMode">
          <x-radio :label="'yarn-cluster'" :disabled="isDetails"></x-radio>
        </x-radio-group>
      </div>
    </m-list-box>
    <div class="list-box-4p" v-if="deployMode == 'yarn-cluster'">
      <div class="clearfix list">
        <span class="sp1">{{$t('Number of task manager')}}</span>
        <span class="sp2">
          <x-input
            :disabled="isDetails"
            type="input"
            v-model="yarncontainer"
            :placeholder="$t('Please enter the number of Executor')"
            style="width: 200px;"
            autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('yarn application name')}}</span>
        <span class="sp2">
          <x-input
            :disabled="isDetails"
            type="input"
            v-model="yarnName"
            :placeholder="$t('Please enter yarn application name')"
            style="width: 186px;"
            autocomplete="off">
        </x-input>
        </span>
      </div>
      <div class="clearfix list">
        <span class="sp1">{{$t('task manager memory(M)')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="yarntaskManagerMemory"
                  :placeholder="$t('Please enter task manager number')"
                  style="width: 200px;"
                  autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('job manager memory(M)')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="yarnjobManagerMemory"
                  :placeholder="$t('Please enter job manager use')"
                  style="width: 186px;"
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
      <div slot="text">{{$t('Resources')}}</div>
      <div slot="content">
        <m-resources
                ref="refResources"
                @on-resourcesData="_onResourcesData"
                :resource-list="resourceList">
        </m-resources>
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
  import mResources from './_source/resources'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'flink',
    data () {
      return {
        // Main function class
        mainClass: '',
        // Master jar package
        mainJar: null,
        // Master jar package(List)
        mainJarList: [],
        // Deployment method
        deployMode: 'yarn-cluster',
        // Resource(list)
        resourceList: [],
        // Custom function
        localParams: [],
        // yarn task manager memory
        yarntaskManagerMemory: 1024,
        // yarn job manager memory
        yarnjobManagerMemory: '1024',
        // the default number of yarn task manager
        yarncontainer: 1,
        // Command line argument
        mainArgs: '',
        // Program type
        programType: 'JAVA',
        // Program type(List)
        programTypeList: [{ code: 'JAVA' }, { code: 'SCALA' }]
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
       * verification
       */
      _verification () {

        if (!this.mainJar) {
          this.$message.warning(`${i18n.$t('Please enter main jar package')}`)
          return false
        }

        if(this.deployMode == 'yarn-cluster') {
          if (!this.yarntaskManagerMemory) {
            this.$message.warning(`${i18n.$t('Please enter task manager number')}`)
            return false
          }

          if (!this.yarnjobManagerMemory) {
            this.$message.warning(`${i18n.$t('Please enter job manager use')}`)
            return false
          }

          if (!this.yarncontainer) {
            this.$message.warning(`${i18n.$t('Please enter the number of Executor')}`)
            return false
          }
          if (!Number.isInteger(parseInt(this.yarntaskManagerMemory))) {
            this.$message.warning(`${i18n.$t('The number of task manager should be a positive integer')}`)
            return false
          }

          if (!_.isNumber(parseInt(this.yarnjobManagerMemory))) {
            this.$message.warning(`${i18n.$t('Memory should be a positive integer')}`)
            return false
          }

        }
        if (!this.$refs.refResources._verifResources()) {
          return false
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }

        // storage
        this.$emit('on-params', {
          mainClass: this.mainClass,
          mainJar: {
            res: this.mainJar
          },
          deployMode: this.deployMode,
          resourceList: this.resourceList,
          localParams: this.localParams,
          yarntaskManagerMemory: this.yarntaskManagerMemory,
          yarnjobManagerMemory: this.yarnjobManagerMemory,
          yarncontainer: this.yarncontainer,
          mainArgs: this.mainArgs,
          programType: this.programType,
          yarnName: this.yarnName

        })
        return true
      },
      /**
       * get resources list
       */
      _getResourcesList () {
        return new Promise((resolve, reject) => {
          let isJar = (alias) => {
            return alias.substring(alias.lastIndexOf('.') + 1, alias.length) !== 'jar'
          }
          this.mainJarList = _.map(_.cloneDeep(this.store.state.dag.resourcesListS), v => {
            return {
              id: v.id,
              code: v.alias,
              disabled: isJar(v.alias)
            }
          })
          resolve()
        })
      }
    },
    watch: {

    },
    created () {
      this._getResourcesList().then(() => {
        let o = this.backfillItem

        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.mainClass = o.params.mainClass || ''
          this.mainJar = o.params.mainJar.res || ''
          this.deployMode = o.params.deployMode || ''
          this.yarncontainer = o.params.yarncontainer || 1
          this.yarntaskManagerMemory = o.params.yarntaskManagerMemory || '1024'
          this.yarnjobManagerMemory = o.params.yarnjobManagerMemory || '1024'
          this.mainArgs = o.params.mainArgs || ''
          this.programType = o.params.programType || 'JAVA'

          let resourceList = o.params.resourceList || []
          if (resourceList.length) {
            this.resourceList = resourceList
          }

          // backfill localParams
          let localParams = o.params.localParams || []
          if (localParams.length) {
            this.localParams = localParams
          }
        }
      })
    },
    mounted () {

    },
    components: { mLocalParams, mListBox, mResources }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .flink-model {
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
