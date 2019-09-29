import Vue from 'vue'
import Tooltip from './Tooltip.vue'
import { LIB_NAME } from '../../../../src/util'

// 组件构造器
const TooltipConstructor = Vue.extend(Tooltip)

export default (options = {}) => {
  const {
    containerID = `${LIB_NAME}-tooltip-container`,
    text = '',
    placement,
    theme,
    maxWidth,
    el,
    positionFixed,
    viewport
  } = options
  const instance = new TooltipConstructor({
    propsData: {
      content: text,
      placement,
      theme,
      maxWidth,
      reference: el,
      positionFixed: positionFixed,
      viewport: viewport
    }
  }).$mount()
  const selector = `#${containerID}`
  let target = document.querySelector(selector)
  if (!target) {
    target = document.createElement('div')
    target.id = containerID
    document.body.appendChild(target)
  }
  target.appendChild(instance.$el)
  return instance
}
