/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Base from '../base'
import { checkKeyInModel, init } from '../../common'

const TYPE = 'line'

/**
 * 折线图
 */
export default class Line extends Base {
  /**
   * 单独导出时调用的初始化方法
   * @param {*} el 选择器或者 DOM 对象
   * @param {*} data 数据源
   * @param {*} options 可选项
   */
  static init (el, data, options) {
    return init(Line, el, data, options)
  }

  /**
   * 将用户配置转换为符合 ECharts API 格式的配置格式
   */
  transform () {
    const { data = [] } = this.settings

    if (data.length === 0) {
      throw new Error('数据源为空！')
    }

    if (Object.keys(data[0]).length > 2) {
      return this.setMultipleLines()
    } else {
      this.simple = true
      return this.setSingleLine()
    }
  }

  /**
   * 单条折线
   */
  setSingleLine () {
    const {
      // 数据
      data = [],
      // 属性字典
      keyMap = {
        xAxisKey: 'key',
        dataKey: 'value'
      },
      // 图表标题
      title = '单条折线图'
    } = this.settings

    // x 轴对应属性名，数据值对应的属性名
    const { xAxisKey, dataKey } = keyMap
    checkKeyInModel(data[0], xAxisKey, dataKey)

    const series = [{
      type: TYPE,
      data: []
    }]
    const xAxis = {
      type: 'category',
      data: []
    }

    for (let i = 0; i < data.length; i++) {
      xAxis.data.push(data[i][xAxisKey])
      series[0].data.push(data[i][dataKey])
    }

    return { title, xAxis, series }
  }

  /**
   * 多条折线
   */
  setMultipleLines () {
    const {
      // 数据
      data = [],
      // 属性字典
      keyMap = {
        xAxisKey: 'key',
        legendKey: 'typeName',
        dataKey: 'value'
      },
      // 图表标题
      title = '多条折线图'
    } = this.settings

    // x 轴对应属性名，图例对应的属性名，数据值对应的属性名
    const { xAxisKey, legendKey, dataKey } = keyMap
    checkKeyInModel(data[0], xAxisKey, legendKey, dataKey)

    const legendData = []
    const series = []
    const xAxis = {
      type: 'category',
      data: []
    }

    for (let i = 0; i < data.length; i++) {
      const legendItem = data[i][legendKey]
      const xAxisItem = data[i][xAxisKey]
      const dataItem = data[i][dataKey]

      // 图例
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // x 轴
      if (!xAxis.data.includes(xAxisItem)) {
        xAxis.data.push(xAxisItem)
      }

      // 系列
      let targetSeries = series.find(s => s.name === legendItem)
      if (!targetSeries) {
        targetSeries = {
          name: legendItem,
          type: TYPE,
          data: []
        }
        series.push(targetSeries)
      }
      targetSeries.data.push(dataItem)
    }

    return { title, xAxis, series, legendData }
  }

  /**
   * 绘制图表
   */
  apply () {
    const { title, xAxis, series, legendData = [] } = this.options
    const {
      // 是否为横向图
      reverseAxis = false,
      // 自定义 y 轴
      yAxis,
      // 注入配置到 series
      insertSeries
    } = this.settings
    const valueAxis = { type: 'value' }
    let yAxisModel = reverseAxis ? xAxis : valueAxis
    let xAxisModel = reverseAxis ? valueAxis : xAxis
    // 使用自定义 y 轴覆盖
    if (yAxis) {
      yAxisModel = yAxis
    }
    // 简单图表标题为空时，图表垂直居中
    const top = !title && this.simple ? '3%' : 60

    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    let opts = {
      title: {
        text: title
      },
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top,
        containLabel: true
      },
      legend: {
        data: legendData
      },
      xAxis: xAxisModel,
      yAxis: yAxisModel,
      series: _series
    }

    this.echart.setOption(opts, true)
    this.echart.clear()
    this.echart.setOption(opts, true)
  }
}
