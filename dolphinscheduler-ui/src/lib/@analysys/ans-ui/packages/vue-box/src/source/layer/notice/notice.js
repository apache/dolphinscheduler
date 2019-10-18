import { LIB_NAME, ANIMATION_PREFIX } from '../../../../../../src/util'
import BoxManager from '../../base/index'

const prefixCls = `${LIB_NAME}-notice`
const prefixKey = `${LIB_NAME}_notice_key_`

let messageInstance
let name = 1

let defaultConfig = {
  // 设置全局的自动关闭时间，为0时不自动消失
  duration: 1.5,
  // 设置出现的位置在浏览器顶部的距离
  top: 60,
  right: 20,
  transitionName: `${ANIMATION_PREFIX}move-right`,
  list: true
}

let iconTypes = {
  'info': 'ans-icon-notice-solid',
  'success': 'ans-icon-success-solid',
  'warning': 'ans-icon-warn-solid',
  'error': 'ans-icon-fail-solid',
  'loading': 'ans-icon-spinner'
}

function getMessageInstance () {
  messageInstance = messageInstance || BoxManager.newInstance({
    prefixCls: prefixCls,
    styles: {
      top: defaultConfig.top + 'px',
      right: defaultConfig.right + 'px'
    },
    className: defaultConfig.list ? `${prefixCls}-list` : ''
  })
  return messageInstance
}

function notice (title = '', content = '', duration = defaultConfig.duration, type, onClose = function () {}, closable = false) {
  let instance = getMessageInstance()

  instance.notice({
    name: `${prefixKey}${name}`,
    duration: duration,
    transitionName: defaultConfig.transitionName,
    styles: {},
    content:
      `
        <div class="${prefixCls}-custom-content">
            <i class="${iconTypes[type]} ${type} ${prefixCls}__icon"></i>
            <span class="${prefixCls}__title">${title}</span>
            <div class="${prefixCls}__content">${content}</div>
        </div>
      `,
    onClose: onClose,
    closable: closable,
    type: 'notice'
  })

  name++
}

function formatOptions (options) {
  const type = typeof options
  if (type === 'string') {
    options = {
      content: options
    }
  }
  return options
}

export default {
  name: 'Notice',
  info (options) {
    options = formatOptions(options)
    return notice(options.title, options.content, options.duration, 'info', options.onClose, options.closable)
  },
  success (options) {
    options = formatOptions(options)
    return notice(options.title, options.content, options.duration, 'success', options.onClose, options.closable)
  },
  warning (options) {
    options = formatOptions(options)
    return notice(options.title, options.content, options.duration, 'warning', options.onClose, options.closable)
  },
  error (options) {
    options = formatOptions(options)
    return notice(options.title, options.content, options.duration, 'error', options.onClose, options.closable)
  },
  loading (options) {
    options = formatOptions(options)
    return notice(options.title, options.content, options.duration, 'loading', options.onClose, options.closable)
  },
  config (cfg = {}) {
    defaultConfig = Object.assign(defaultConfig, cfg)
  },
  destroy () {
    let instance = getMessageInstance()
    messageInstance = null
    instance.destroy(prefixCls)
  }
}
