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

const TYPE = 'bar'

/**
 * Histogram
 */
export default class Bar extends Base {
  /**
   * Initialization method called on separate export
   * @param {*} el Selector or DOM object
   * @param {*} data data source
   * @param {*} options Optional
   */
  static init (el, data, options) {
    return init(Bar, el, data, options)
  }

  /**
   * Convert user configuration to a configuration format that conforms to the format of echarts API
   */
  transform () {
    const { data = [] } = this.settings

    if (data.length === 0) {
      throw new Error('Data source is empty！')
    }

    if (Object.keys(data[0]).length > 2) {
      return this.setMultipleBars()
    } else {
      this.simple = true
      return this.setSingleBar()
    }
  }

  /**
   * Single column
   */
  setSingleBar () {
    const {
      // data
      data = [],
      // Attribute dictionary
      keyMap = {
        xAxisKey: 'key',
        dataKey: 'value'
      },
      // Chart title
      title = 'Single bar histogram'
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
   * Multiple columns
   */
  setMultipleBars () {
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
      title = 'Multiple histogram',
      // The specified index of polyline data when the column is mixed
      lineTypes
    } = this.settings

    // Attribute name corresponding to X axis, legend and data value
    const { xAxisKey, legendKey, dataKey } = keyMap
    // Use timeline data or not
    const timeline = Object.keys(data[0]).length === 4
    const timelineKey = keyMap.timelineKey || 'timeline'
    if (timeline) {
      checkKeyInModel(data[0], xAxisKey, legendKey, dataKey, timelineKey)
    } else {
      checkKeyInModel(data[0], xAxisKey, legendKey, dataKey)
    }

    // Standard mixed index of folded columns
    let lineTypeList = []
    if (lineTypes) {
      if (!Array.isArray(lineTypes)) {
        lineTypeList = [lineTypes]
      } else {
        lineTypeList = lineTypes
      }
    }

    // Timeline default configuration
    const timelineOptions = {
      timeline: {
        axisType: 'category',
        autoPlay: true,
        playInterval: 1000,
        data: []
      },
      options: []
    }

    // Initial value
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

      // x axis
      if (!xAxis.data.includes(xAxisItem)) {
        xAxis.data.push(xAxisItem)
      }

      // time axis
      if (timeline) {
        const timelineItem = data[i][timelineKey]
        // Set timeline label
        if (!timelineOptions.timeline.data.includes(timelineItem)) {
          timelineOptions.timeline.data.push(timelineItem)
        }
        // Universal family configuration
        if (!series.some(s => s.name === legendItem)) {
          let seriesType = TYPE
          if (lineTypeList.length !== 0 && lineTypeList.includes(legendItem)) {
            seriesType = 'line'
          }
          series.push({
            name: legendItem,
            type: seriesType
          })
        }
        // Series data
        let targetOptions = timelineOptions.options.find(o => o._helpName === timelineItem)
        if (!targetOptions) {
          // Initialization option
          targetOptions = {
            _helpName: timelineItem,
            title: { text: title.replace('$timeline', timelineItem) },
            series: []
          }
          timelineOptions.options.push(targetOptions)
        }
        let targetSeries = targetOptions.series.find(d => d._helpName === legendItem)
        if (!targetSeries) {
          // Initialize series data
          targetSeries = {
            _helpName: legendItem,
            data: []
          }
          targetOptions.series.push(targetSeries)
        }
        targetSeries.data.push(dataItem)
      } else {
        // Non timeline data processing
        let targetSeries = series.find(s => s.name === legendItem)
        if (!targetSeries) {
          let seriesType = TYPE
          if (lineTypeList.length !== 0 && lineTypeList.includes(legendItem)) {
            seriesType = 'line'
          }
          targetSeries = {
            name: legendItem,
            type: seriesType,
            data: []
          }
          series.push(targetSeries)
        }
        targetSeries.data.push(dataItem)
      }
    }

    if (timeline) {
      return { title, xAxis, series, legendData, timelineOptions }
    }
    return { title, xAxis, series, legendData }
  }

  /**
   * Drawing charts
   */
  apply () {
    const { title, xAxis, series, legendData, timelineOptions } = this.options
    const {
      // Whether it is a horizontal drawing
      reverseAxis = false,
      // Custom Y axis
      yAxis,
      // Is it a stacking diagram
      stack = false,
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
    // Set up stack chart
    if (stack) {
      series.forEach(set => {
        set.stack = '总量'
        set.label = {
          normal: {
            show: true,
            position: reverseAxis ? 'insideRight' : 'insideTop'
          }
        }
      })
    }

    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    // time axis
    if (timelineOptions) {
      const opts = {
        baseOption: {
          timeline: timelineOptions.timeline,
          tooltip: {
            trigger: 'axis'
          },
          grid: {
            top: 80,
            bottom: 100,
            containLabel: true
          },
          legend: {
            x: 'right',
            data: legendData
          },
          xAxis: xAxisModel,
          yAxis: yAxisModel,
          series: _series
        },
        options: timelineOptions.options
      }

      this.echart.setOption(opts, true)
      this.echart.clear()
      this.echart.setOption(opts, true)
    } else {
      // When the simple chart title is empty, the chart is vertically centered
      const top = !title && this.simple ? '3%' : 60

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
}
