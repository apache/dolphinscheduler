import { addClass, removeClass, getStyle } from '../../../../src/util'
import Vue from 'vue'
import xSpin from './Spin.vue'

const SpinConstructor = Vue.extend(xSpin)

const defaults = {
  body: false,
  fullscreen: true,
  lock: false,
  text: null,
  background: null,
  iconClass: '',
  customClass: ''
}

let fullscreenLoading

SpinConstructor.prototype.originalPosition = ''

SpinConstructor.prototype.close = function () {
  if (this.fullscreen) {
    fullscreenLoading = undefined
  }
  this.visible = false
}

const addStyle = (options, parent, instance) => {
  if (options.fullscreen) {
    instance.originalPosition = getStyle(document.body, 'position')
  } else if (options.body) {
    instance.originalPosition = getStyle(document.body, 'position')
    const spinStyle = instance.$el.style
      ;['top', 'left'].forEach(property => {
      let scroll = property === 'top' ? 'scrollTop' : 'scrollLeft'
      spinStyle[property] = options.target.getBoundingClientRect()[property] +
          document.body[scroll] +
          document.documentElement[scroll] -
          parseInt(getStyle(document.body, `margin-${property}`), 10) + 'px'
    })
    ;['height', 'width'].forEach(property => {
      spinStyle[property] = options.target.getBoundingClientRect()[property] + 'px'
    })
  } else {
    instance.originalPosition = getStyle(parent, 'position')
  }
}

const init = (options = {}) => {
  options = Object.assign({}, defaults, options)
  if (typeof options.target === 'string') {
    options.target = document.querySelector(options.target)
  }
  options.target = options.target || document.body
  if (options.target !== document.body) {
    options.fullscreen = false
  } else {
    options.body = true
  }
  if (options.fullscreen && fullscreenLoading) {
    return fullscreenLoading
  }

  let parent = options.body ? document.body : options.target
  let instance = new SpinConstructor({
    el: document.createElement('div'),
    data: options
  })
  options.onAfterLeave = () => {
    removeClass(parent, 'spin-relative-parent')
    removeClass(parent, 'spin-lock-overflow')
    parent.removeChild(instance.$el)
    instance.$destroy()
  }

  addStyle(options, parent, instance)
  if (instance.originalPosition !== 'absolute' && instance.originalPosition !== 'fixed') {
    addClass(parent, 'spin-relative-parent')
  }
  if (options.fullscreen && options.lock) {
    addClass(parent, 'spin-lock-overflow')
  }
  parent.appendChild(instance.$el)
  Vue.nextTick(() => {
    instance.visible = true
  })
  if (options.fullscreen) {
    fullscreenLoading = instance
  }
  return instance
}

export {
  init,
  defaults
}
