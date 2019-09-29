<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
import { LIB_NAME, hasClass, addClass, removeClass, emitter } from '../../../../src/util'
import vButton from './Button.vue'

const prefixCls = `${LIB_NAME}-btn-group`

export default {
  name: 'xButtonGroup',

  data () {
    return {
      buttons: [],
      checkedList: [],
      activeClass: 'active'
    }
  },

  mixins: [emitter],

  props: {
    size: {
      type: String,
      default: 'default',
      validator (v) {
        return ['xsmall', 'small', 'large', 'default'].includes(v)
      }
    },

    shape: {
      type: String,
      validator (v) {
        return ['circle', ''].includes(v)
      }
    },

    vertical: Boolean,

    // 用于双向绑定
    value: {
      default: ''
    }
  },

  components: { vButton },

  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-${this.size}`]: !!this.size,
          [`${prefixCls}-${this.shape}`]: !!this.shape,
          [`${prefixCls}-vertical`]: this.vertical
        }
      ]
    },

    firstVisibleChild () {
      return this.buttons.find(b => b.visible)
    },

    lastVisibleChild () {
      for (let i = this.buttons.length - 1; i > 0; i--) {
        const b = this.buttons[i]
        if (b.visible) return b
      }
      return null
    }
  },

  methods: {
    initComponents () {
      this.buttons = this.$children.filter(v => v.$options.name === 'xButton')
      // sort by rendering index
      this.buttons.sort((a, b) => {
        const aIndex = Array.prototype.indexOf.call(a.$el.parentNode.children, a.$el)
        const bIndex = Array.prototype.indexOf.call(b.$el.parentNode.children, b.$el)
        return aIndex - bIndex
      })
      this.handleButtonVisible()

      this.buttons.forEach(btn => {
        if (!btn._$bind) {
          btn._$bind = true
          btn.$on('click', this.handleChange.bind(this, btn))
        }
        btn.checked = btn.value !== undefined && btn.value !== '' && btn.value === this.value
      })

      this.$nextTick(() => {
        this.updateModel()
      })
    },

    updateModel () {
      let activeClass = this.activeClass
      this.buttons.forEach(child => {
        let has = hasClass(child.$el, activeClass)
        if (child.checked) {
          if (!has) addClass(child.$el, activeClass)
        } else {
          if (has) removeClass(child.$el, activeClass)
        }
      })
    },

    handleChange (child, ...args) {
      if ((this.buttons.length === 1 && this.buttons[0].checked) || child.checked) {
        return
      }

      let prev = this.buttons.find(v => v.checked && v !== child)
      if (prev) {
        prev.checked = false
      }

      child.checked = true

      this.checkedList = this.buttons.filter(o => o.checked).map(o => o.value)
      this.$emit('input', this.checkedList[0], () => this.updateModel())
      this.updateModel()
    },

    handleButtonVisible () {
      if (!this.buttons.length) return

      this.broadcast('xButton', 'checkPosition', {
        first: this.firstVisibleChild,
        last: this.lastVisibleChild
      })
    }
  },

  created () {
    this.checkedList = [this.value]
    this.$on('buttonVisibleChanged', this.handleButtonVisible)
  },

  mounted () {
    this.initComponents()
  },

  updated () {
    this.initComponents()
  }
}
</script>
