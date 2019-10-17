import PopperJS from 'popper.js'
import merge from 'deepmerge'

export default {
  data () {
    return {
      // 是否显示
      visible: false,
      // 对应的 Popper 实例
      popperInstance: null,
      // 组件内直接指定参考元素
      referenceEl: null
    }
  },
  props: {
    // 方向
    placement: String,
    // 基于哪个元素进行定位
    reference: HTMLElement,
    // 与参考元素距离，单位为 px
    distance: {
      type: Number,
      default: 8
    },
    // 是否将弹出元素插入到 body
    appendToBody: {
      type: Boolean,
      default: false
    },
    positionFixed: {
      type: Boolean,
      default: false
    },
    viewport: {
      type: Boolean,
      default: false
    },
    popperOptions: Object
  },
  watch: {
    visible (val) {
      val ? this.updateElementHandler() : this.hideElementHandler()
    }
  },
  methods: {
    bindPopper () {
      if (!this.popperInstance) {
        const { distance, $refs, appendToBody } = this
        let referenceEl = this.referenceEl || this.reference || $refs.reference
        let popperEl = this.popperEl = $refs.popper || this.$el

        if (!referenceEl || !popperEl) return

        if (appendToBody) document.body.appendChild(popperEl)

        // 控制偏移的函数
        const addDistance = (data) => {
          const { arrowStyles, offsets, placement, styles } = data
          const { popper, reference } = offsets
          // 设置弹出层与参考元素距离
          let vertical
          if (placement.startsWith('top')) {
            vertical = true
            styles.top -= distance
          } else if (placement.startsWith('bottom')) {
            vertical = true
            styles.top += distance
          } else if (placement.startsWith('left')) {
            vertical = false
            styles.left -= distance
          } else {
            vertical = false
            styles.left += distance
          }
          // 存在箭头元素
          if ($refs.arrow) {
            // 设置箭头偏移
            if (vertical) {
              const arrowWidth = $refs.arrow.offsetWidth
              if (placement.includes('start')) {
                const target = popper.width < reference.width ? popper : reference
                arrowStyles.left = (target.width - arrowWidth) * 0.5
              } else if (placement.includes('end')) {
                if (popper.width < reference.width) {
                  arrowStyles.left = (popper.width - arrowWidth) * 0.5
                } else {
                  arrowStyles.right = (reference.width - arrowWidth) * 0.5
                }
              }
            } else {
              const arrowHeight = $refs.arrow.offsetHeight
              if (placement.includes('start')) {
                const target = popper.height < reference.height ? popper : reference
                arrowStyles.top = (target.height - arrowHeight) * 0.5
              } else if (placement.includes('end')) {
                if (popper.height < reference.height) {
                  arrowStyles.top = (popper.height - arrowHeight) * 0.5
                } else {
                  arrowStyles.bottom = (reference.height - arrowHeight) * 0.5
                }
              }
            }
          }
          return data
        }

        const options = {
          placement: this.placement,
          modifiers: {
            computeStyle: {},
            preventOverflow: {},
            hide: {}
          }
        }

        const merged = merge(options, this.popperOptions || {})

        // 关闭 GPU 加速，防止字体模糊
        merged.modifiers.computeStyle.gpuAcceleration = false
        merged.modifiers.computeStyle.fn = (data, options) => {
          data = PopperJS.Defaults.modifiers.computeStyle.fn(data, options)
          return addDistance(data)
        }

        if (this.positionFixed) {
          merged.positionFixed = true
        }
        if (this.viewport) {
          merged.modifiers.preventOverflow.boundariesElement = 'viewport'
        }
        // fix popper.js warning
        if (merged.modifiers.preventOverflow.hasOwnProperty('enabled') &&
          !merged.modifiers.preventOverflow.enabled) {
          merged.modifiers.hide.enabled = false
        }

        this.popperInstance = new PopperJS(referenceEl, popperEl, merged)
      }
    },
    hideElementHandler () {
      if (this.popperInstance) {
        this.popperInstance.disableEventListeners()
      }
    },
    updateElementHandler () {
      if (!this.popperInstance) {
        this.bindPopper()
      }
      this.popperInstance.enableEventListeners()
      this.$nextTick(() => {
        this.popperInstance.scheduleUpdate()
      })
    },
    destroyPopper () {
      if (this.popperInstance) {
        this.popperInstance.destroy()
        this.popperInstance = null
      }
    }
  },
  beforeDestroy () {
    this.destroyPopper()
    if (this.popperEl && this.popperEl.parentNode === document.body) {
      document.body.removeChild(this.popperEl)
    }
  }
}
