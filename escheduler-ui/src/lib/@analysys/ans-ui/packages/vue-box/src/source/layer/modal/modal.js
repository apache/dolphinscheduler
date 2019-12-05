import Vue from 'vue'
import BoxManager from '../../base/index'
import { on, hasClass } from '../../../../../../src/util'
import { xButton } from '../../../../../vue-button/src'
import { LIB_NAME, ANIMATION_PREFIX } from '../../../../../../src/util/constants'
import { t } from '../../../../../../src/locale'

const prefixCls = `${LIB_NAME}-modal`

const prefixKey = `${LIB_NAME}_modal_key_`

let messageInstance
let name = 1

/* eslint-disable no-unused-vars */
let customModal
let defaultConfig = {
  // 设置全局的自动关闭时间，为0时不自动消失
  duration: 0,
  transitionName: `${ANIMATION_PREFIX}modal-down`
}

function getMessageInstance () {
  messageInstance = messageInstance || BoxManager.newInstance({
    prefixCls: prefixCls,
    styles: {}
  })
  return messageInstance
}

/**
 * @params options {object} 生成modal的配置参数
 *
 * transitionName {String} 弹框动画
 * className {String} 弹窗的自定义样式名称
 * content {String} 内容（支持dom字符串）
 * onClose {Function} 点击关闭图标的回调
 * closable {Boolean} 是否显示关闭图标
 * width {Number} 设置弹框宽度
 * title {String} 设置标题
 * ok {Object} {show [Boolean], text [String], handle [Function] }
 * cancel 同 ok
 * render {vue[render]函数} 当需要自定义显示内用时 (content, title, ok, cancel 失效)
 * showMask 是否显示遮罩
 * maskClosable
 */
function notice (options) {
  let instance = getMessageInstance()
  let keyName = `${prefixKey}${name}`


  let onRender = function (boxName) {
    if (keyName !== boxName) {
      return
    }
    let comp = instance.component.$children.find(o => o.name === keyName)
    if (options.render) {
      customModal = new Vue({
        name: 'customModal',
        render: options.render,
        mounted () {
          on(this.$el, 'click', function (e) {
            // e.stopPropagation()
          })
          if (options.maskClosable) {
            on(comp.$el.children[0], 'click', function (e) {
              instance.remove(boxName)
              if (hasClass(e.target, 'msk')) {
                options.onClose && options.onClose()
              }
            })
          }
        }
      }).$mount(comp.$refs.content)
    } else {
      customModal = new Vue({
        name: 'defaultModal',
        data: {
          width: options.width ? options.width : '520',
          content: options.content || '',
          title: options.title || '',
          ok: Object.assign({
            show: true,
            text: t('ans.modal.confirm'),
            handle: function () {
            }
          }, options.ok),
          cancel: Object.assign({
            show: true,
            text: t('ans.modal.cancel'),
            handle: function () {
            }
          }, options.cancel)
        },
        render () {
          const { width, content, title, ok, cancel } = this
          return (
            <div class={`${prefixCls}-box-content`} style={{ width: width + 'px' }}>
              {
                title ? (
                  <div class={`${prefixCls}-content-header`}>
                    <div class={`${prefixCls}-header-inner`} domPropsInnerHTML={title} />
                  </div>) : null
              }
              {
                content ? (
                  <div class={`${prefixCls}-content-body`} domPropsInnerHTML={content} />
                ) : null
              }
              {
                (ok.show || cancel.show) ? (
                  <div class={`${prefixCls}-content-footer`}>
                    {
                      cancel.show ? (
                        <x-button type='text' shape={cancel.shape || ''} class={cancel.className || 'x-btn-cancel'} onClick={this.cancelClick}>{cancel.text}</x-button>
                      ) : null
                    }
                    {
                      ok.show ? (
                        <x-button type='primary' shape={ok.shape || ''} class={ok.className || 'x-btn-submit'} onClick={this.okClick}>{ok.text}</x-button>
                      ) : null
                    }
                  </div>
                ) : null
              }
            </div>
          )
        },
        components: { xButton },
        props: {},
        methods: {
          cancelClick (e) {
            this.cancel.handle(e)
            instance.remove(boxName)
          },
          okClick (e) {
            this.ok.handle(e)
            instance.remove(boxName)
          }
        },
        computed: {
        },
        mounted () {
          on(this.$el, 'click', function (e) {
            e.stopPropagation()
          })
          if (options.maskClosable) {
            on(comp.$el, 'click', function (e) {
              instance.remove(boxName)
              if (hasClass(e.target, 'msk')) {
                options.onClose && options.onClose()
              }
            })
          }
        }
      }).$mount(comp.$refs.content)
    }
  }

  instance.notice({
    $onRender: onRender,
    name: keyName,
    duration: defaultConfig.duration,
    // 弹框动画样式
    transitionName: options.transitionName || defaultConfig.transitionName,
    // 弹框的样式
    styles: {},
    // 弹框的内容
    content: '',
    // 弹框的关闭回调函数
    onClose: options.onClose || (() => { }),
    // 弹框是否显示关闭按钮(右上角的关闭)
    escClose: (typeof options.escClose) === 'boolean' ? options.escClose : false,
    closable: (typeof options.closable) === 'boolean' ? options.closable : true,
    className: `${options.className} ${options.showMask ? 'mask' : ''} `,
    // 弹框的类型
    type: 'modal'
  })


  return (function () {
    let target = name++
    return {
      remove: () => {
        instance.remove(`${prefixKey}${target}`)
      }
    }
  })()
}

export default {
  dialog (options) {
    return notice(options)
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
