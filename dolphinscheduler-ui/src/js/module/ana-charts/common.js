/**
 * 根据参数找到容器并初始化图表，然后返回一个或者一组图表实例
 * @param {*} Target 图表组件类
 * @param {*} el 选择器或者 DOM 对象
 * @param {*} data 数据源
 * @param {*} options 可选项
 */
export const init = (Target, el, data, options) => {
  const list = getChartContainers(el)
  const settings = Object.assign({}, { data }, options)
  const charts = list.map(element => {
    return new Target(element, settings)
  })
  return charts.length === 1 ? charts[0] : charts
}

/**
 * 统一图表容器为 DOM 元素数组
 * @param {*} el 选择器或者 DOM 对象
 */
function getChartContainers (el) {
  // 未传参数，直接返回
  if (!el) {
    return
  }
  if (typeof el === 'string') {
    if (el.startsWith('#')) {
      el = document.getElementById(el.slice(1))
    } else if (el.startsWith('.')) {
      el = document.getElementsByClassName(el.slice(1))
    } else {
      return
    }
  }
  if (!el) {
    throw new Error('找不到对应的dom对象！')
  }
  let list
  if (HTMLElement.prototype.isPrototypeOf(el)) {
    list = new Array(el)
  } else {
    list = Array.from(el)
  }
  if (!list) {
    throw new Error('未找到对应的dom对象！')
  }
  return list
}

/**
 * 检测在指定对象中是否存在指定的属性名
 * @param {Object} model 待检测模型
 * @param  {...any} params 待检测属性名
 */
export const checkKeyInModel = (model, ...params) => {
  for (const key of params) {
    if (!model.hasOwnProperty(key)) {
      throw new Error('数据格式错误！未找到指定属性：' + key)
    }
  }
}
