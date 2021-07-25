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
 * Broken line diagram
 */
export default class Line extends Base {
  /**
   * Initialization method called on separate export
   * @param {*} el Selector or DOM object
   * @param {*} data data source
   * @param {*} options Optional
   */
  static init (el, data, options) {
    return init(Line, el, data, options)
  }

  /**
   * Convert user configuration to a configuration format that conforms to the format of echarts API
   */
  transform () {
    const { data = [] } = this.settings

    if (data.length === 0) {
      throw new Error('Data source is empty!')
    }

    if (Object.keys(data[0]).length > 2) {
      return this.setMultipleLines()
    } else {
      this.simple = true
      return this.setSingleLine()
    }
  }

  /**
   * Single broken line
   */
  setSingleLine () {
    const {
      // data
      data = [],
      // Attribute dictionary
      keyMap = {
        xAxisKey: 'key',
        dataKey: 'value'
      },
      // Chart title
      title = 'Single line chart'
    } = this.settings

    // X axis corresponds to attribute name, data value corresponds to attribute name
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
   * Multiple broken lines
   */
  setMultipleLines () {
    const {
      // data
      data = [],
      // Attribute dictionary
      keyMap = {
        xAxisKey: 'key',
        legendKey: 'typeName',
        dataKey: 'value'
      },
      // Chart title
      title = 'Multiple line chart'
    } = this.settings

    // Attribute name corresponding to X axis, legend and data value
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

      // Legend
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // X axis
      if (!xAxis.data.includes(xAxisItem)) {
        xAxis.data.push(xAxisItem)
      }

      // series
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
   * Drawing charts
   */
  apply () {
    const { title, xAxis, series, legendData = [] } = this.options
    const {
      // Whether it is a horizontal drawing
      reverseAxis = false,
      // Custom Y axis
      yAxis,
      // Injection configuration to series
      insertSeries
    } = this.settings
    const valueAxis = { type: 'value' }
    let yAxisModel = reverseAxis ? xAxis : valueAxis
    const xAxisModel = reverseAxis ? valueAxis : xAxis
    // Use custom Y-axis overlay
    if (yAxis) {
      yAxisModel = yAxis
    }
    // When the simple chart title is empty, the chart is vertically centered
    const top = !title && this.simple ? '3%' : 60

    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    const opts = {
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
