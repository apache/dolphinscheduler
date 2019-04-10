import xSpin from './source/Spin.vue'
import directive from './source/directive'
import { init, defaults } from './source/service'

xSpin.init = init

xSpin.install = (Vue, options = {}) => {
  const { directiveName = 'spin', text, background, iconClass, customClass } = options
  defaults.text = text
  defaults.background = background
  defaults.iconClass = iconClass
  defaults.customClass = customClass
  Vue.directive(directiveName, directive)
  Vue.prototype.$spin = xSpin.init
}

export { xSpin }
