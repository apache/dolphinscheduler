import factory from './factory'

const PLACEMENTS = ['top', 'bottom', 'left', 'right']
const POSITIONS = ['start', 'end']

export default {
  bind (el, binding) {
    // 移除事件
    removeListeners(el)
    // 是否点击触发，是否浅色主题
    const { click, light, fixed, viewport } = binding.modifiers
    // 位置使用第一个修饰符，不传则居中
    const positionModifiers = POSITIONS.filter(position => binding.modifiers[position])
    const position = positionModifiers.length ? positionModifiers[0] : ''
    // 方向使用第一个修饰符，不传则默认为 top
    const placementModifiers = PLACEMENTS.filter(placement => binding.modifiers[placement])
    let placement = placementModifiers.length ? placementModifiers[0] : 'top'
    placement = position ? `${placement}-${position}` : placement
    // 默认配置
    const defaultOptions = {
      placement,
      triggerEvent: click ? 'click' : 'mouseenter',
      theme: light ? 'light' : 'dark',
      maxWidth: '200px',
      positionFixed: !!fixed,
      viewport: !!viewport
    }
    // 可以绑定配置对象
    let options = typeof binding.value === 'object' ? binding.value : { text: binding.value }
    options = Object.assign({}, defaultOptions, options)
    options.el = el
    el._ttOptions = options
    // 绑定事件监听
    el._appearHandler = function appearHandler () {
      if (!this._ttInstance) {
        // 确保使用最新的配置
        this._ttInstance = factory(el._ttOptions)
      }
      this._ttInstance.show()
    }
    el._vanishHandler = function vanishHandler () {
      if (this._ttInstance) {
        this._ttInstance.hide()
      }
    }
    el.addEventListener(options.triggerEvent, el._appearHandler)
    el.addEventListener('mouseleave', el._vanishHandler)
  },

  update (el, binding) {
    // 刷新配置
    let options = typeof binding.value === 'object' ? binding.value : { text: binding.value }
    options = Object.assign({}, el._ttOptions, options)
    options.el = el
    el._ttOptions = options
    if (el._ttInstance) {
      el._ttInstance.update(options)
    }
  },

  unbind (el) {
    const instance = el._ttInstance
    if (instance && instance.destroy) {
      instance.destroy()
    }
    removeListeners(el)
    delete el._appearHandler
    delete el._vanishHandler
    delete el._ttInstance
    delete el._ttOptions
  }
}

function removeListeners (el) {
  if (el._appearHandler) {
    el.removeEventListener('click', el._appearHandler)
    el.removeEventListener('mouseenter', el._appearHandler)
  }
  if (el._vanishHandler) {
    el.removeEventListener('mouseleave', el._vanishHandler)
  }
}
