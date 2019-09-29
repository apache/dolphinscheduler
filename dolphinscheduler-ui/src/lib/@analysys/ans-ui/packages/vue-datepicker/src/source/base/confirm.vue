<template>
  <div class="x-date-packer-confirm">
    <div class="confirm-slot">
      <span class="ck-act date-time-text disabled" v-if="ishms()" v-show="!isTime">{{text}}</span>
      <x-poptip popper-class="date-poptip" placement="top-start" v-if="ishms()" :popper-options = "{boundariesElement:'viewport'}" @on-show="tipShow" ref="poptipTime">
        <span slot="reference" class="ck-act date-time-text" v-show="isTime">{{text}}</span>
        <times ref="times" @change="timeChange" :format="format" :type="type"></times>
      </x-poptip>
      <slot name="confirm"></slot>
    </div>
    <div class="confirm-btn">
      <span class="ck-act" @click="cancel">{{t('ans.datepicker.cancel')}}</span>
      <button @click="$emit('confirm-btn')">{{t('ans.datepicker.confirm')}}</button>
    </div>
  </div>
</template>

<script>

import ishms from '../util/ishms.js'
import { xPoptip } from '../../../../vue-poptip/src/index'
import times from '../base/time.vue'
import isValid from '../util/isValid.js'
import { Locale } from '../../../../../src/util'
import { t } from '../../../../../src/locale'

export default {
  components: { xPoptip, confirm, times },
  mixins: [Locale],
  data () {
    return {
      text: t('ans.datepicker.selectTime'),
      isTime: false
    }
  },
  props: {
    format: String,
    type: String
  },
  methods: {
    ishms () {
      return ishms(this.format)
    },

    timsInit (date, date1) {
      if (date && isValid(date)) {
        this.isTime = true
        this.$refs.times.init(date, date1)
      } else {
        this.$refs.poptipTime.doClose()
        this.isTime = false
      }
    },
    timeChange (date) {
      this.$emit('time-change', date)
    },

    cancel () {
      this.isTime = false
      this.$emit('cancel')
    },

    tipShow () {
      this.$refs.times.setScrollTop()
    }
  }
}

</script>
