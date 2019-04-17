<template>
  <m-conditions>
    <template slot="search-group">
      <div class="list">
        <x-button type="ghost" size="small" @click="_ckQuery" icon="fa fa-search"></x-button>
      </div>
      <div class="list">
        <x-datepicker
                ref="datepicker"
                @on-change="_onChangeStartStop"
                type="daterange"
                format="YYYY-MM-DD HH:mm:ss"
                placement="bottom-end"
                v-model="datepicker"
                :panelNum="2">
          <x-input slot="input" readonly slot-scope="{value}" :value="value" style="width: 310px;" size="small" :placeholder="$t('Select date range')">
            <i slot="suffix"
               @click.stop="_dateEmpty()"
               class="ans-icon-fail-solid"
               v-show="value"
               style="font-size: 13px;cursor: pointer;margin-top: 1px;">
            </i>
          </x-input>
        </x-datepicker>
      </div>
      <div class="list">
        <x-select style="width: 160px;" @on-change="_onChangeState" :value="stateType" >
          <x-input slot="trigger" readonly :value="selectedModel ? selectedModel.label : ''" slot-scope="{ selectedModel }" style="width: 160px;" size="small" :placeholder="$t('State')" suffix-icon="ans-icon-arrow-down">
          </x-input>
          <x-option
                  v-for="city in stateTypeList"
                  :key="city.label"
                  :value="city.code"
                  :label="city.label">
          </x-option>
        </x-select>
      </div>
      <div class="list">
        <x-input v-model="host" style="width: 140px;" size="small" :placeholder="$t('host')"></x-input>
      </div>
      <div class="list">
        <x-input v-model="searchVal" style="width: 200px;" size="small" :placeholder="$t('name')"></x-input>
      </div>
    </template>
  </m-conditions>
</template>
<script>
  import _ from 'lodash'
  import { stateType } from './common'
  import { setUrlParams } from '@/module/util/routerUtil'
  import mConditions from '@/module/components/conditions/conditions'
  export default {
    name: 'conditions',
    data () {
      return {
        // state(list)
        stateTypeList: stateType,
        // state
        stateType: '',
        // start date
        startDate: '',
        // end date
        endDate: '',
        // search value
        searchVal: '',
        // host
        host: '',
        // datepicker plugin
        datepicker: []
      }
    },
    props: {},
    methods: {
      _ckQuery () {
        setUrlParams({ pageNo: 1 })
        this.$emit('on-query', {
          startDate: this.startDate || '',
          endDate: this.endDate || '',
          stateType: this.stateType || '',
          host: _.trim(this.host) || '',
          searchVal: _.trim(this.searchVal) || ''
        })
      },
      /**
       * change times
       */
      _onChangeStartStop (val) {
        this.startDate = val[0]
        this.endDate = val[1]
        // set url params
        setUrlParams({
          startDate: this.startDate,
          endDate: this.endDate
        })
      },
      /**
       * change state
       */
      _onChangeState (val) {
        this.stateType = val.value
        // set url params
        setUrlParams({
          stateType: this.stateType
        })
      },
      /**
       * empty date
       */
      _dateEmpty () {
        this.startDate = ''
        this.endDate = ''
        this.$refs.datepicker.empty()
        // set url params
        setUrlParams({
          startDate: '',
          endDate: ''
        })
      }
    },
    watch: {
      searchVal (val) {
        setUrlParams({
          searchVal: _.trim(val)
        })
      }
    },
    created () {
      let query = this.$route.query
      if (!_.isEmpty(query)) {
        this.searchVal = query.searchVal
        this.startDate = query.startDate
        this.endDate = query.endDate
        this.stateType = query.stateType
        this.datepicker = (!this.startDate && !this.endDate) ? [] : [this.startDate, this.endDate]
      }
    },
    mounted () {
    },
    computed: {
    },
    components: { mConditions }
  }
</script>
