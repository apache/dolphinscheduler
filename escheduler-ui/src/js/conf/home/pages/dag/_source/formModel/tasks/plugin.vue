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
    <m-plugin-stage :stage-definition="stageDefinition" :stage-config="stageConfig" @on-stageChange="_onStageChange">
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
        stageDefinition: null,
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
        this.stageConfig = _.merge(this.stageConfig, a)
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
        this.stageConfig = o.params.stageConfig
      } else {
        this.stageConfig = {
          name: this.stageInfo.name,
          libraryName: this.stageInfo.libraryName,
          stageVersion: this.stageInfo.stageVersion,
          configValue: JSON.parse(this.stageInfo.defaultConfigurationJson)
        }
      }
      if (!_.some(this.store.state.dag.tasks, { id: this.createNodeId }) &&
        this.router.history.current.name !== 'definition-create') {
        //this._getReceiver()
      }
      this.stageDefinition = JSON.parse(this.stageInfo.configurationDefinitionJson)
    },
    mounted () {
    },
    destroyed () {
    },
    computed: {},
    components: { mListBox, mLocalParams, mPluginStage }
  }
</script>
