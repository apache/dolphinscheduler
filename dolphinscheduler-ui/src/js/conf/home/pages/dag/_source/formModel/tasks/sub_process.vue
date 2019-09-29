<template>
  <div class="sub_process-model">
    <div class="clearfix list">
      <div class="text-box">
        <span>{{$t('Child Node')}}</span>
      </div>
      <div class="cont-box">
        <div class="label-box">
          <x-select
                  style="width: 100%;"
                  filterable
                  v-model="wdiCurr"
                  :disabled="isDetails"
                  @on-change="_handleWdiChanged">
            <x-option
                    v-for="city in processDefinitionList"
                    :key="city.code"
                    :value="city.id"
                    :label="city.code">
            </x-option>
          </x-select>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'sub_process',
    data () {
      return {
        // Process definition(List)
        processDefinitionList: [],
        // Process definition
        wdiCurr: null
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      /**
       * Node unified authentication parameters
       */
      _verification () {
        if (!this.wdiCurr) {
          this.$message.warning(`${i18n.$t('Please select a sub-Process')}`)
          return false
        }
        this.$emit('on-params', {
          processDefinitionId: this.wdiCurr
        })
        return true
      },
      /**
       * The selected process defines the upper component name padding
       */
      _handleWdiChanged (o) {
        this.$emit('on-set-process-name', this._handleName(o.value))
      },
      /**
       * Return the name according to the process definition id
       */
      _handleName (id) {
        return _.filter(this.processDefinitionList, v => id === v.id)[0].code
      }
    },
    watch: {},
    created () {
      let processListS = _.cloneDeep(this.store.state.dag.processListS)
      let id = this.router.history.current.params.id || null
      this.processDefinitionList = (() => {
        let a = _.map(processListS, v => {
          return {
            id: v.id,
            code: v.name,
            disabled: false
          }
        })
        return _.filter(a, v => +v.id !== +id)
      })()

      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.wdiCurr = o.params.processDefinitionId
      } else {
        if (this.processDefinitionList.length) {
          this.wdiCurr = this.processDefinitionList[0]['id']
          this.$emit('on-set-process-name', this._handleName(this.wdiCurr))
        }
      }
    },
    mounted () {
    }
  }
</script>