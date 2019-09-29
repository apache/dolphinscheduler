<template>
	<div class="x-date-panel">
    <div class="x-date-panel-day">
      <!-- <template v-if="isTimes()">
        <times v-show="isTimesShow" ref="times" @change="timeChange"></times>
      </template> -->
      <day :selected-date="selectedDate" @change="dayChange" :format="format" :disabled-date="disabledDate" ref="vDate"></day>
    </div>
		<confirm v-if="isConfirm()" 
            :format="format" 
            @confirm-btn="$emit('confirm')" 
            @cancel="$emit('cancel')" 
            type="date"
            ref="confirm"
            @time-change="timeChange"
            ></confirm>
	</div>
</template>

<script>

import day from '../base/day.vue'
import confirm from '../base/confirm.vue'
import ishms from '../util/ishms.js'
import isValid from '../util/isValid.js'

export default {
  name: 'panelDate',
  components: { day, confirm },
  data () {
    return {
      selectedDate: null,

      // 是否展示times
      isTimesShow: false
    }
  },
  props: {

    // 初始值
    value: {
      type: [Array],
      default () {
        return null
      }
    },

    // 展示的日期格式
    format: String,

    // 不可选日期
    disabledDate: {
      type: Function,
      default: null
    },

    // confirm
    confirm: {
      type: [Number, Boolean],
      default: 0
    },

    // 选项
    options: {
      type: [Array, Object],
      default: null
    }
  },

  mounted () {
    this.init()
  },

  methods: {
    init (date) {
      let dateValue = date || this.value
      if (dateValue && dateValue.length) {
        this.selectedDate = dateValue
        if (isValid(dateValue[0])) this.$refs.vDate.toDate = dateValue[0]
      } else {
        this.selectedDate = []
      }
      this.$refs.vDate.init(this.selectedDate)
      this.timeInit()
    },

    timeInit () {
      if (this.isTimes()) {
        this.$refs.confirm.timsInit(this.selectedDate[this.selectedDate.length - 1])
      }
    },

    dayChange (date) {
      this.selectedDate = [date]
      this.timeInit()
      this.$emit('change', this.selectedDate)
    },

    timeChange (date) {
      this.selectedDate = [date]
      this.$emit('change', this.selectedDate)
    },

    isTimes () {
      return ishms(this.format)
    },

    isConfirm () {
      if (this.confirm || this.isTimes()) return true
      return false
    }
  }
}
</script>