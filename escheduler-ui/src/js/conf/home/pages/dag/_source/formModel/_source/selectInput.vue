<template>
  <x-select
          style="width: 157px;"
          :disabled="isDetails"
          @on-change="_onChange"
          v-model="value">
      <x-input
              ref="input"
              slot="trigger"
              v-if="isInput"
              :disabled="isDetails"
              slot-scope="{ selectedModel }"
              maxlength="4"
              @on-blur="_onBlur"
              :placeholder="$t('Please choose')"
              :value="selectedModel === null ? '0' : selectedModel.value"
              style="width: 100%;"
              @on-click-icon.stop="_ckIcon">
        <i slot="suffix" class="fa fa-times-circle" style="font-size: 15px;cursor: pointer;" v-show="!isIconState"></i>
        <i slot="suffix" class="ans-icon-arrow-down" style="font-size: 12px;" v-show="isIconState"></i>
      </x-input>
    <x-option
            v-for="city in list"
            :key="city"
            :value="city"
            :label="city">
    </x-option>
  </x-select>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-select-input',
    data () {
      return {
        isIconState: false,
        isInput: true
      }
    },
    mixins: [disabledState],
    props: {
      value: String,
      list: Array
    },
    model: {
      prop: 'value',
      event: 'valueEvent'
    },
    methods: {
      _onChange (o) {
        this.$emit('valueEvent', +o.value)
        this._setIconState(+o.value)
      },
      _setIconState (value) {
        // Whether there is a list
        this.isIconState = _.includes(this.list, parseInt(value))
      },
      _ckIcon () {
        if (this.isDetails) {
          return
        }
        this.isInput = false
        this.$emit('valueEvent', +this.list[0])
        this.isIconState = true
        // Refresh instance
        setTimeout(() => {
          this.isInput = true
        }, 1)
      },
      _onBlur () {
        let val = $(this.$refs['input'].$el).find('input')[0].value
        if (this._validation(val)) {
          this.$emit('valueEvent', val)
          this._setIconState(val)
        }
      },
      _validation (val) {
        if (val === '0') return true

        if (!(/(^[0-9]*[1-9][0-9]*$)/.test(val))) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer')}`)
          // init
          this._ckIcon()
          return false
        }
        return true
      }
    },
    watch: {
    },
    created () {
      this._setIconState(this.value)
    },
    mounted () {
    },
    components: {}
  }
</script>