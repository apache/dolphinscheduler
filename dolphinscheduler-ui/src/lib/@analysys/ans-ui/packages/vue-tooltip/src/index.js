import xTooltip from './source/Tooltip.vue'
import directive from './source/directive'

xTooltip.install = (Vue, options = {}) => {
  const { directiveName = 'tooltip' } = options
  Vue.directive(directiveName, directive)
}

export {
  xTooltip
}
