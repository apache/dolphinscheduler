<template>
  <div class="procedure-model">
    <m-list-box>
      <div slot="text">{{$t('Datasource')}}</div>
      <div slot="content">
        <m-datasource
                ref="refDs"
                @on-dsData="_onDsData"
                :supportType="['MYSQL','POSTGRESQL','CLICKHOUSE', 'ORACLE', 'SQLSERVER']"
                :data="{ type:type,datasource:datasource }">
        </m-datasource>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('methods')}}</div>
      <div slot="content">
        <x-input
                type="input"
                :disabled="isDetails"
                v-model="method"
                :placeholder="$t('Please enter method(optional)')"
                autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Custom Parameters')}}</div>
      <div slot="content">
        <m-local-params
                ref="refLocalParams"
                @on-local-params="_onLocalParams"
                :udp-list="localParams">
        </m-local-params>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './_source/listBox'
  import mDatasource from './_source/datasource'
  import mLocalParams from './_source/localParams'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'procedure',
    data () {
      return {
        // method
        method: '',
        // Custom parameter
        localParams: [],
        // Data source type
        type: '',
        // data source
        datasource: ''
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      /**
       * return type or datasource
       */
      _onDsData (o) {
        this.type = o.type
        this.datasource = o.datasource
      },
      /**
       * return udp
       */
      _onLocalParams (a) {
      },
      /**
       * verification
       */
      _verification () {
        // datasource Subcomponent verification
        if (!this.$refs.refDs._verifDatasource()) {
          return false
        }

        // Verification function
        if (!this.method) {
          this.$message.warning(`${i18n.$t('Please enter method')}`)
          return false
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        // storage
        this.$emit('on-params', {
          type: this.type,
          datasource: this.datasource,
          method: this.method,
          localParams: this.localParams
        })
        return true
      }
    },
    watch: {},
    created () {
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.name = o.name
        this.desc = o.desc

        // backfill
        this.type = o.params.type || ''
        this.datasource = o.params.datasource || ''
        this.method = o.params.method || ''

        // backfill localParams
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }
      }
    },
    mounted () {
    },
    components: { mListBox, mDatasource, mLocalParams }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
