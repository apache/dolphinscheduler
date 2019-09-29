<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('Task Name')}}</span>
          </th>
          <th width="66">
            <span>{{$t('Task Date')}}</span>
          </th>
          <th width="150">
            <span>{{$t('Start Time')}}</span>
          </th>
          <th width="150">
            <span>{{$t('End Time')}}</span>
          </th>
          <th width="134">
            <span>{{$t('Duration')}}(s)</span>
          </th>
          <th>
            <span>{{$t('Source Table')}}</span>
          </th>
          <th width="100">
            <span>{{$t('Record Number')}}</span>
          </th>
          <th>
            <span>{{$t('Target Table')}}</span>
          </th>
          <th width="100">
            <span>{{$t('Record Number')}}</span>
          </th>
          <th width="88">
            <span>{{$t('State')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span class="ellipsis"  data-toggle="tooltip" data-container="body" :title="_rtTooltip(item.procName)" data-html="true">{{item.procName}}</span>
          </td>
          <td><span>{{item.procDate}}</span></td>
          <td>
            <span v-if="item.startTime">{{item.startTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.endTime">{{item.endTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td><span>{{item.duration}}</span></td>
          <td><span class="ellipsis" data-toggle="tooltip" data-container="body" :title="_rtTooltip(item.sourceTab)" data-html="true">{{item.sourceTab}}</span></td>
          <td>
            <span>{{item.sourceRowCount}}</span>
          </td>
          <td><span class="ellipsis" data-toggle="tooltip" data-container="body" :title="_rtTooltip(item.targetTab)" data-html="true">{{item.targetTab}}</span></td>
          <td><span>{{item.targetRowCount}}</span></td>
          <td><span>{{item.note}}</span></td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list',
    data () {
      return {
        list: [],
        backfillItem: {}
      }
    },
    props: {
      taskRecordList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      _rtTooltip (name) {
        return `<div style="word-wrap:break-word;text-align: left;">${name}</div>`
      }
    },
    watch: {
      taskRecordList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.taskRecordList
    },
    components: { }
  }
</script>