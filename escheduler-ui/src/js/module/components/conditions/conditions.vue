<template>
  <div class="conditions-model">
    <div class="left">
      <slot name="button-group"></slot>
    </div>
    <div class="right">
      <div class="from-box">
        <slot name="search-group" v-if="isShow"></slot>
        <template v-if="!isShow">
          <div class="list">
            <x-button type="ghost" size="small" @click="_ckQuery" icon="fa fa-search"></x-button>
          </div>
          <div class="list">
            <x-input v-model="searchVal"
                     @on-enterkey="_ckQuery"
                     size="small"
                     :placeholder="$t('Please enter keyword')"
                     type="text"
                     style="width:180px;">
            </x-input>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  export default {
    name: 'conditions',
    data () {
      return {
        // search value
        searchVal: ''
      }
    },
    props: {
      operation: Array
    },
    methods: {
      /**
       * emit Query parameter
       */
      _ckQuery () {
        this.$emit('on-conditions', {
          searchVal: _.trim(this.searchVal)
        })
      }
    },
    computed: {
      // Whether the slot comes in
      isShow () {
        return this.$slots['search-group']
      }
    },
    created () {
      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.searchVal = this.$route.query.searchVal || ''
      }
    },
    components: {}
  }
</script>
