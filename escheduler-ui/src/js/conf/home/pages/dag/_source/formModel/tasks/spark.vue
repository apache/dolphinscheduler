<template>
  <div class="spark-model">
    <m-list-box>
      <div slot="text">{{$t('程序类型')}}</div>
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
    <m-list-box v-if="programType !== 'PYTHON'">
      <div slot="text">{{$t('主函数的class')}}</div>
      <div slot="content">
        <x-input
                :disabled="isDetails"
                type="input"
                v-model="mainClass"
                :placeholder="$t('请输入mainClass')"
                autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('主jar包')}}</div>
      <div slot="content">
        <x-select
                style="width: 100%;"
                :placeholder="$t('请选择主jar包')"
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
      <div slot="text">{{$t('部署方式')}}</div>
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
        <span class="sp1">{{$t('Driver内核数')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="driverCores"
                  :placeholder="$t('请输入Driver内核数')"
                  style="width: 200px;"
                  autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('Driver内存数')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="driverMemory"
                  :placeholder="$t('请输入Driver内存数')"
                  style="width: 186px;"
                  autocomplete="off">
        </x-input>
        </span>
      </div>
      <div class="clearfix list">
        <span class="sp1">{{$t('Executor数量')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="numExecutors"
                  :placeholder="$t('请输入Executor数量')"
                  style="width: 200px;"
                  autocomplete="off">
        </x-input>
        </span>
        <span class="sp1 sp3">{{$t('Executor内存数')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="executorMemory"
                  :placeholder="$t('请输入Executor内存数')"
                  style="width: 186px;"
                  autocomplete="off">
        </x-input>
        </span>
      </div>
      <div class="clearfix list">
        <span class="sp1">{{$t('Executor内核数')}}</span>
        <span class="sp2">
          <x-input
                  :disabled="isDetails"
                  type="input"
                  v-model="executorCores"
                  :placeholder="$t('请输入Executor内核数')"
                  style="width: 200px;"
                  autocomplete="off">
          </x-input>
        </span>
      </div>
    </div>
    <m-list-box>
      <div slot="text">{{$t('命令行参数')}}</div>
      <div slot="content">
        <x-input
                :autosize="{minRows:2}"
                :disabled="isDetails"
                type="textarea"
                v-model="mainArgs"
                :placeholder="$t('请输入命令行参数')"
                autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('其他参数')}}</div>
      <div slot="content">
        <x-input
                :disabled="isDetails"
                :autosize="{minRows:2}"
                type="textarea"
                v-model="others"
                :placeholder="$t('请输入其他参数')">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('资源')}}</div>
      <div slot="content">
        <m-resources
                ref="refResources"
                @on-resourcesData="_onResourcesData"
                :resource-list="resourceList">
        </m-resources>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('自定义参数')}}</div>
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
    name: 'spark',
    data () {
      return {
        // Main function class
        mainClass: '',
        // Master jar package
        mainJar: null,
        // Master jar package(List)
        mainJarList: [],
        // Deployment method
        deployMode: 'cluster',
        // Resource(list)
        resourceList: [],
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
        programTypeList: [{ code: 'JAVA' }, { code: 'SCALA' }, { code: 'PYTHON' }]
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
        if (this.programType !== 'PYTHON' && !this.mainClass) {
          this.$message.warning(`${i18n.$t('请填写主函数的class')}`)
          return false
        }

        if (!this.mainJar) {
          this.$message.warning(`${i18n.$t('请选择主jar包')}`)
          return false
        }

        if (!this.numExecutors) {
          this.$message.warning(`${i18n.$t('请填写Executor数量')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.numExecutors))) {
          this.$message.warning(`${i18n.$t('Executor数量为正整数')}`)
          return false
        }

        if (!this.executorMemory) {
          this.$message.warning(`${i18n.$t('请填写Executor内存数')}`)
          return false
        }

        if (!this.executorMemory) {
          this.$message.warning(`${i18n.$t('请填写Executor内存数')}`)
          return false
        }

        if (!_.isNumber(parseInt(this.executorMemory))) {
          this.$message.warning(`${i18n.$t('内存数为数字')}`)
          return false
        }

        if (!this.executorCores) {
          this.$message.warning(`${i18n.$t('请填写Executor内核数')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.executorCores))) {
          this.$message.warning(`${i18n.$t('内核数为正整数')}`)
          return false
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
          driverCores: this.driverCores,
          driverMemory: this.driverMemory,
          numExecutors: this.numExecutors,
          executorMemory: this.executorMemory,
          executorCores: this.executorCores,
          mainArgs: this.mainArgs,
          others: this.others,
          programType: this.programType
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
      // Listening type
      programType (type) {
        if (type === 'PYTHON') {
          this.mainClass = ''
        }
      }
    },
    created () {
      this._getResourcesList().then(() => {
        let o = this.backfillItem

        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.mainClass = o.params.mainClass || ''
          this.mainJar = o.params.mainJar.res || ''
          this.deployMode = o.params.deployMode || ''
          this.driverCores = o.params.driverCores || 1
          this.driverMemory = o.params.driverMemory || '512M'
          this.numExecutors = o.params.numExecutors || 2
          this.executorMemory = o.params.executorMemory || '2G'
          this.executorCores = o.params.executorCores || 2
          this.mainArgs = o.params.mainArgs || ''
          this.others = o.params.others
          this.programType = o.params.programType || 'SCALA'

          // backfill resourceList
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
