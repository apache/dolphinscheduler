<template>
	<div class="x-date-packer-panel">
		<years :types="type" ref="years" style="position:inherit;" @change="change"></years>
	</div>
</template>

<script>

import years from '../base/years.vue'

export default {
  components: { years },
  data () {
    return {

    }
  },

  props: {
    type: {
      type: String,
      default: 'year'
    },

    value: Array
  },

  mounted () {
    let t = this.type === 'year' ? 'y' : 'm'
    let d = this.value && this.value.length ? this.value[0] : new Date()
    this.$refs.years.init(d, t)
  },

  methods: {
    change (date) {
      this.$emit('change', [date])
    }
  }
}

</script>