<template>
  <div class="plugin-model">
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
    <m-plugin-stage :stage-list="stageList" :stage-with-config="stageConfig" @on-stageChange="_onStageChange">
    </m-plugin-stage>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './_source/listBox'
  import mLocalParams from './_source/localParams'
  import mPluginStage from './_source/pluginStage'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'plugin',
    data () {
      return {
        // Custom parameter
        localParams: [],
        // plugin stage list
        stageList: [],
        stageConfig: null
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      createNodeId: Number,
      stageInfo: Object
    },
    methods: {
      /**
       * return Custom parameter
       */
      _onUdpData (a) {
        this.localParams = a
      },
      _onStageChange (a) {
        this.sourceStageConfig = a
      },
      /**
       * verification
       */
      _verification () {
        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        // storage
        this.$emit('on-params', {
          localParams: this.localParams,
          stageConfig: this.stageConfig
        })
        return true
      }
    },
    watch: {
    },
    created () {
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        // backfill
        this.localParams = o.params.localParams || []
        this.sourceStageConfig = o.params.sourceStageConfig
        this.targetStageConfig = o.params.targetStageConfig
      }
      if (!_.some(this.store.state.dag.tasks, { id: this.createNodeId }) &&
        this.router.history.current.name !== 'definition-create') {
        //this._getReceiver()
      }
      // load stage lists
      let stateSdcStageList = this.store.state.plugin.stageListAll || []
      if (stateSdcStageList.length) {
        this.stageList = stateSdcStageList
      } else {
        this.store.dispatch('plugin/getAllStages').then(res => {
          this.$nextTick(() => {
            this.stageList = res
          })
        })
      }
      this.stageConfig = JSON.parse(this.stageInfo.defaultConfigurationJson)
    },
    mounted () {
    },
    destroyed () {
    },
    computed: {},
    components: { mListBox, mLocalParams, mPluginStage }
  }
</script>
