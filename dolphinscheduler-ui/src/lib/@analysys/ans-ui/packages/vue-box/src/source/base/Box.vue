<template>
  <transition :name="transitionName">
    <div :class="classes" :style="styles">
      <template v-if="type === 'message'">
        <div :class="[baseClass + '-content']" ref="content">
          <div :class="[baseClass + '-content-text']" v-html="content"></div>
          <a :class="[baseClass + '-close']" @click="close" v-if="closable">
            <i class="ans-icon-close"></i>
          </a>
        </div>
      </template>
      <template v-if="type === 'modal'">
        <div :class="{msk: className.split(' ').indexOf('mask')!==-1}" ></div>
        <div :class="[baseClass + '-content-wrapper']">
          <div :class="[baseClass + '-content']" ref="content" v-html="content"></div>
          <a :class="[baseClass + '-close']" @click="close" v-if="closable">
            <i class="ans-icon-close"></i>
          </a>
        </div>
      </template>
      <template v-if="type === 'notice'">
        <div :class="[baseClass + '-content']" ref="content">
          <div :class="[baseClass + '-content__inner']" v-html="content"></div>
          <a :class="[baseClass + '-close']" @click="close" v-if="closable">
            <i class="ans-icon-close"></i>
          </a>
        </div>
      </template>
    </div>
  </transition>
</template>

<script>
import { findComponentUpward } from '../../../../../src/util'

export default {
  props: {
    name: {
      type: String,
      required: true
    },
    type: {
      type: String
    },
    prefixCls: {
      type: String,
      default: ''
    },
    transitionName: {
      type: String
    },
    duration: {
      type: Number,
      default: 1.5
    },
    content: {
      type: String,
      default: ''
    },
    styles: {
      type: Object,
      default: function () {
        return {
          right: '50%'
        }
      }
    },
    closable: {
      type: Boolean,
      default: false
    },
    onClose: {
      type: Function,
      default: function () {}
    },
    className: {
      type: String,
      default: ''
    },
    escClose: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    clearCloseTimer () {
      if (this.closeTimer) {
        clearTimeout(this.closeTimer)
        this.closeTimer = null
      }
    },
    close () {
      this.clearCloseTimer()
      this.onClose()
      let $parent = findComponentUpward(this, 'xBoxManager')
      if ($parent) {
        $parent.close(this.name)
      }
    },
    escHandler (event) {
      /* eslint-disable */
      let e = event || window.event || arguments.callee.caller.arguments[0]
      if (e && e.keyCode == 27) { // 按 Esc
        this.close()
      }
    }
  },
  computed: {
    baseClass () {
      return `${this.prefixCls}-box`
    },
    classes () {
      return [
        `${this.baseClass}`,
        `${this.className}`
      ]
    }
  },
  mounted () {
    this.clearCloseTimer()
    if (this.duration !== 0) {
      this.closeTimer = setTimeout(() => {
        this.close()
      }, this.duration * 1000)
    }
    if(this.$listeners.onrender){
      this.$listeners.onrender(this.name)
    }
    // this.$emit('on-mounted', this.name)
    if (this.escClose) {
      document.onkeyup = this.escHandler
    }
  }
}
</script>
