<template>
  <m-list :config="config"></m-list>
</template>
<script>
  import mList from '@/conf/home/pages/projects/pages/_source/taskRecordList'
  export default {
    name: 'task-record',
    data () {
      return {
        config: {
          title: `${this.$t('Task record')}`,
          apiFn: 'getTaskRecordList'
        }
      }
    },
    components: { mList }
  }
</script>
