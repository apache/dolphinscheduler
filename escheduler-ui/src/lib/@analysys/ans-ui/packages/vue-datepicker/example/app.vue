<template>
  <div>
    <section class="demo-section">
      <h4>基础用法</h4>
      <div>
        <x-datepicker clearable @on-change="change"></x-datepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>选择日期带时分秒</h4>
      <div>
        <x-datepicker placeholder="时分秒" format="YYYY-MM-DD HH:mm:ss"></x-datepicker>
        <x-datepicker placeholder="时分" format="YYYY-MM-DD HH:mm"></x-datepicker>
        <x-datepicker placeholder="时" format="YYYY-MM-DD HH"></x-datepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>选择年月</h4>
      <div>
        <x-datepicker placeholder="month" type="month" format="YYYY-MM"></x-datepicker>
        <x-datepicker placeholder="year" type="year" format="YYYY"></x-datepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>日期区间</h4>
      <div>
        <x-datepicker type="daterange"></x-datepicker>
      </div>
    </section>


    <section class="demo-section">
      <h4>日期区间带时分秒</h4>
      <div>
        <x-datepicker type="daterange" placeholder="时分秒" format="YYYY-MM-DD HH:mm:ss"></x-datepicker>
        <x-datepicker type="daterange" placeholder="时分" format="YYYY-MM-DD HH:mm"></x-datepicker>
        <x-datepicker type="daterange" placeholder="时" format="YYYY-MM-DD HH"></x-datepicker>
      </div>
    </section>


    <section class="demo-section">
      <h4>快捷选择</h4>
      <div>
        <x-datepicker type="daterange" :options-width="90" :options="options" placeholder="快捷选择" style="width:400px"></x-datepicker>
      </div>
    </section>


    <section class="demo-section">
      <h4>日期区间多选</h4>
      <div>
        <x-datepicker type="daterange" :multiple="0" placeholder="区间多选"  style="width:600px"></x-datepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>多个日期面板</h4>
      <div>
        <x-datepicker type="daterange" :panel-num="5" placeholder="5"></x-datepicker>
      </div>
    </section>
  </div>
</template>

<script>
import { xDatepicker, dateUtil } from '../src/index'

export default {
  name: 'app',
  components: { xDatepicker },
  data () {
    return {
      options: [
        {
          text: '今日',
          type: 'text',
          value () {
            return [dateUtil().format('YYYY/MM/DD'), dateUtil().format('YYYY/MM/DD')]
          },
          click: () => {}
        }, {
          text: '近29日',
          value () {
            return [dateUtil().subtract(29, 'days').format('YYYY/MM/DD'), dateUtil().format('YYYY/MM/DD')]
          },
          click: () => {}
        }
      ]
    }
  },
  methods: {
    change (d) {
      console.log(d)
    }
  }
}
</script>

<style lang="scss"></style>
