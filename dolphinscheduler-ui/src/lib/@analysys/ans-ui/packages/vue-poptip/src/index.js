import xPoptip from './source/Poptip.vue'
import directive from './source/directive'

xPoptip.directive = directive

/* istanbul ignore next */
xPoptip.install = Vue => {
  Vue.directive('poptip', directive)
}

export {
  xPoptip
}
