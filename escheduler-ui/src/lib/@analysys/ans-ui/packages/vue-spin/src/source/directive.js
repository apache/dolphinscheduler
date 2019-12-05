import { addClass, removeClass, getStyle } from '../../../../src/util'
import Vue from 'vue'
import xSpin from './Spin.vue'
import { defaults } from './service'

const SpinConstructor = Vue.extend(xSpin)

export default {
  bind (el, binding) {
    const text = el.getAttribute('spin-text') || defaults.text
    const background = el.getAttribute('spin-background') || defaults.background
    const iconClass = el.getAttribute('spin-icon-class') || defaults.iconClass
    const customClass = el.getAttribute('spin-custom-class') || defaults.customClass
    const spin = new SpinConstructor({
      el: document.createElement('div'),
      data: {
        text,
        background,
        iconClass,
        customClass,
        fullscreen: !!binding.modifiers.fullscreen,
        onAfterLeave () {
          el.domVisible = false
          const target = binding.modifiers.fullscreen || binding.modifiers.body
            ? document.body
            : el
          removeClass(target, 'spin-relative-parent')
          removeClass(target, 'spin-lock-overflow')
        }
      }
    })
    el.spin = spin

    binding.value && toggle(el, binding)
  },

  update (el, binding) {
    let text = el.getAttribute('spin-text')
    if (text === null) {
      text = defaults.text
    }
    el.spin.setText(text)
    if (binding.oldValue !== binding.value) {
      toggle(el, binding)
    }
  },

  unbind (el, binding) {
    if (el.domAppended) {
      if (el.spin && el.spin.$el.parentNode) {
        el.spin.$el.parentNode.removeChild(el.spin.$el)
      }
      toggle(el, { value: false, modifiers: binding.modifiers })
    }
  }
}

function toggle (el, binding) {
  if (binding.value) {
    if (binding.modifiers.fullscreen) {
      el.originalPosition = getStyle(document.body, 'position')
      appendDom(document.body, el, binding)
    } else {
      if (binding.modifiers.body) {
        el.originalPosition = getStyle(document.body, 'position')
        const spinStyle = el.spin.$el.style
        ;['top', 'left'].forEach(property => {
          const scroll = property === 'top' ? 'scrollTop' : 'scrollLeft'
          spinStyle[property] = el.getBoundingClientRect()[property] +
              document.body[scroll] +
              document.documentElement[scroll] -
              parseInt(getStyle(document.body, `margin-${property}`), 10) + 'px'
        })
        ;['height', 'width'].forEach(property => {
          spinStyle[property] = el.getBoundingClientRect()[property] + 'px'
        })
        appendDom(document.body, el, binding)
      } else {
        el.originalPosition = getStyle(el, 'position')
        appendDom(el, el, binding)
      }
    }
    el.spin.visible = true
  } else {
    el.spin.visible = false
  }
}

function appendDom (parent, el, binding) {
  if (!el.domVisible && getStyle(el, 'display') !== 'none' && getStyle(el, 'visibility') !== 'hidden') {
    if (el.originalPosition !== 'absolute' && el.originalPosition !== 'fixed') {
      addClass(parent, 'spin-relative-parent')
    }
    if (binding.modifiers.fullscreen && binding.modifiers.lock) {
      addClass(parent, 'spin-lock-overflow')
    }
    el.domVisible = true
    parent.appendChild(el.spin.$el)
    el.domAppended = true
  }
}
