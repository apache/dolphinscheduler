<template>
  <div :class="classes" :style="styles">
    <box
      v-for="box in boxs"
      :key="box.name"
      :name="box.name"
      :type="box.type"
      :prefix-cls="prefixCls"
      :transition-name="box.transitionName"
      :duration="box.duration"
      :content="box.content"
      :styles="box.styles"
      :closable="box.closable"
      :on-close="box.onClose"
      :class-name="box.className||''"
      :esc-close="box.escClose"
      @onrender="box.$onRender"
      @on-mounted="_onMounted">
    </box>
  </div>
</template>

<script>
import { LIB_NAME } from '../../../../../src/util'
import Box from './Box.vue'

const prefixCls = `${LIB_NAME}-layer`

let seed = 0
const now = Date.now()

function getUuid () {
  return `${LIB_NAME}-Box_${now}_${seed++}`
}

export default {
  name: 'xBoxManager',
  components: { Box },
  props: {
    prefixCls: {
      type: String,
      default: prefixCls
    },
    styles: {
      type: Object,
      default: function () {
        return {
          top: '65px',
          left: '50%'
        }
      }
    },
    className: {
      type: String
    }
  },
  data () {
    return {
      boxs: []
    }
  },
  computed: {
    classes () {
      return [
        `${this.prefixCls}`,
        `${this.prefixCls}-wrapper`,
        {
          [`${this.className}`]: !!this.className
        }
      ]
    }
  },
  methods: {
    onrender(){},
    add (notice) {
      const name = notice.name || getUuid()
      let _notice = Object.assign({
        styles: {
          right: '50%'
        },
        content: '',
        duration: 1.5,
        closable: false,
        name: name
      }, notice)
      _notice.$onRender = _notice.$onRender?_notice.$onRender: () => {}
      this.boxs.push(_notice)
    },
    close (name) {
      const boxs = this.boxs
      for (let i = 0; i < boxs.length; i++) {
        if (boxs[i].name === name) {
          this.boxs.splice(i, 1)
          break
        }
      }
    },
    closeAll () {
      this.boxs = []
    },
    _onMounted (boxName) {
      this.$emit('on-mounted', boxName)
    }
  }
}
</script>
