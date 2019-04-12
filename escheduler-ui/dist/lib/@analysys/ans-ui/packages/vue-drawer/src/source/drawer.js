import { xModal } from '../../../vue-box/src'
import { LIB_NAME } from '../../../../src/util/constants'

const prefixCls = `${LIB_NAME}-drawer`
const animationName = `${prefixCls}-animation`

/**
 * @desc  推荐用 render 函数去创建 dom 在打开和关闭的 生命钩子函数由使用者自己通过事件的方式去实现
 * @desc  关闭的方法同 box
 * @param direction {String}  ' left right top bottom | right'
 * @param className
 * @param showMask
 * @param maskClosable
 * @param escClose
 * @param render
 * */
let defaultOpt = {
  transitionName: animationName,
  className: prefixCls,
  closable: false,
  direction: 'right',
  showMask: true,
  maskClosable: true,
  escClose: true
}

export default function (opt) {
  let options = Object.assign({}, defaultOpt, opt)
  let direction = options.direction
  options.className = opt.className ? `${defaultOpt.className} ${prefixCls}-${direction} ${opt.className}` : `${defaultOpt.className} ${prefixCls}-${direction}`
  options.transitionName = `${options.transitionName}-${options.direction}`
  return xModal.dialog(options)
}
