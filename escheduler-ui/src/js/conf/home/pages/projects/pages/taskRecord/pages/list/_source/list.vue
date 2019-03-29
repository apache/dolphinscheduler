<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('任务名称')}}</span>
          </th>
          <th width="66">
            <span>{{$t('任务日期')}}</span>
          </th>
          <th width="150">
            <span>{{$t('开始时间')}}</span>
          </th>
          <th width="150">
            <span>{{$t('结束时间')}}</span>
          </th>
          <th width="134">
            <span>{{$t('运行时长')}}({{$t('秒')}})</span>
          </th>
          <th>
            <span>{{$t('源表')}}</span>
          </th>
          <th width="100">
            <span>{{$t('记录数')}}</span>
          </th>
          <th>
            <span>{{$t('目标表')}}</span>
          </th>
          <th width="100">
            <span>{{$t('记录数')}}</span>
          </th>
          <th width="88">
            <span>{{$t('状态')}}</span>
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
          <td><span>{{item.startTime | formatDate}}</span></td>
          <td><span>{{item.endTime | formatDate}}</span></td>
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
  import '@/module/filter/formatDate'

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