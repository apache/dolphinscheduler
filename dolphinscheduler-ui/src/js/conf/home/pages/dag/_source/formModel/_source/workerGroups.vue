<template>
  <x-select
          :disabled="isDetails"
          @on-change="_onChange"
          v-model="value"
          style="width: 180px">
    <x-option
            v-for="item in workerGroupsList"
            :key="item.id"
            :value="item.id"
            :label="item.name">
    </x-option>
  </x-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-worker-group',
    data () {
      return {
        workerGroupsList: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: Number,
        default: -1
      }
    },
    model: {
      prop: 'value',
      event: 'workerGroupsEvent'
    },
    methods: {
      _onChange (o) {
        this.value = o.value
        this.$emit('workerGroupsEvent', o.value)
      }
    },
    watch: {
    },
    created () {
      let stateWorkerGroupsList = this.store.state.security.workerGroupsListAll || []
      if (stateWorkerGroupsList.length) {
        this.workerGroupsList = stateWorkerGroupsList
      } else {
        this.store.dispatch('security/getWorkerGroupsAll').then(res => {
          this.$nextTick(() => {
            this.workerGroupsList = res
          })
        })
      }
    }
  }
</script>
