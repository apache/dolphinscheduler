<template>
  <div>
    <section class="demo-section">
      <h4>基础用法</h4>
      <div>
        <x-timepicker @on-change="timeChange" :readonly="false" format="HH:mm"></x-timepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>选择时分</h4>
      <div>
        <x-timepicker placeholder="时分" format="HH:mm" :confirm="true"></x-timepicker>
        <x-timepicker placeholder="时" format="HH点"></x-timepicker>
      </div>
    </section>

    <section class="demo-section">
      <h4>时间区间</h4>
      <div>
        <x-timepicker @on-change="timeChange" type="timerange" format="HH:mm:ss" :step="[1, 5]" :readonly="false" :value="['2018-01-01 05:00:00', '2018-01-01 06:00:00']"></x-timepicker>
      </div>
    </section>

  </div>
</template>

<script>
import { xTimepicker } from '../src/index'


export default {
  name: 'app',
  components: { xTimepicker },
  methods: {
    timeChange (d, d1) {
      console.log(d, d1)
    }
  }
}
</script>
