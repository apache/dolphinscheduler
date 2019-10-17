import normalizeWheel from 'normalize-wheel'
import { hasClass } from '../../util'

const handler = function (event) {
  let scrollDom = null
  let parent = event.target.parentNode
  while (parent) {
    if (hasClass(parent, 'ans-scroller')) {
      scrollDom = parent
      break
    } else {
      parent = parent.parentNode
    }
  }
  if (scrollDom) {
    const callback = scrollDom.__wheel_callback
    const normalized = normalizeWheel(event)
    callback && callback.apply(this, [event, normalized])
  }
}

const eventType = normalizeWheel.getEventType()

const mousewheel = function (element, callback) {
  if (element && element.addEventListener) {
    element.__wheel_callback = callback
    element.addEventListener(eventType, handler)
  }
}

export default {
  bind (el, binding) {
    mousewheel(el, binding.value)
  },
  unbind (el) {
    el.removeEventListener(eventType, el.__wheel_callback)
  }
}
