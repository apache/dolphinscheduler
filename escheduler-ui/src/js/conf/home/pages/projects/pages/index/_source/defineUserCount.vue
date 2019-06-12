<template>
  <div class="define-user-count-model">
    <div v-if="!msg">
      <div class="data-area" v-spin="isSpin">
        <div id="process-definition-bar" style="height:500px"></div>
      </div>
    </div>
    <div v-if="msg">
      <m-no-data :msg="msg" v-if="msg" :height="530"></m-no-data>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { bar } from './chartConfig'
  import Chart from '~/@analysys/ana-charts'
  import mNoData from '@/module/components/noData/noData'
  export default {
    name: 'define-user-count',
    data () {
      return {
        isSpin: true,
        msg: ''
      }
    },
    props: {
      projectId: Number
    },
    methods: {
      ...mapActions('projects', ['getDefineUserCount']),
      _handleDefineUser (res) {
        let data = res.data.userList || []
        this.defineUserList = _.map(data, v => {
          return {
            key: v.userName + ',' + v.userId + ',' + v.count,
            value: v.count
          }
        })
        const myChart = Chart.bar('#process-definition-bar', this.defineUserList, {})
        myChart.echart.setOption(bar)
        // 首页不允许跳转
        if (this.projectId) {
          myChart.echart.on('click', e => {
            this.$router.push({
              name: 'projects-definition-list',
              query: {
                userId: e.name.split(',')[1]
              }
            })
          })
        }
      }
    },
    created () {
      this.isSpin = true
      this.getDefineUserCount(this.projectId).then(res => {
        this.defineUserList = []
        this._handleDefineUser(res)
        this.isSpin = false
      }).catch(e => {
        this.isSpin = false
      })
    },
    mounted () {
    },
    components: { mNoData }
  }
</script>
