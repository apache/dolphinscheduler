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

import { defineComponent, PropType, ref } from 'vue'
import initChart from '@/components/chart'
import type { Ref } from 'vue'

const props = {
	height: {
		type: [String, Number] as PropType<string | number>,
		default: 400
	},
	width: {
		type: [String, Number] as PropType<string | number>,
		default: '100%'
	},
	tooltipFormat: {
		type: String as PropType<string>,
		default: ''
	},
	legendData: {
		type: Array as PropType<Array<string>>,
		default: () => []
	},
	seriesData: {
		type: Array as PropType<Array<string>>,
		default: () => []
	},
	labelShow: {
		type: Array as PropType<Array<string>>,
		default: () => []
	},
	linksData: {
		type: Array as PropType<Array<string>>,
		default: () => []
	},
	labelFormat: {
		type: String as PropType<string>,
		default: ''
	}
}

const GraphChart = defineComponent({
	name: 'GraphChart',
	props,
	setup(props) {
		const graphChartRef: Ref<HTMLDivElement | null> = ref(null)

		const option = {
			tooltip: {
				trigger: 'item',
				triggerOn: 'mousemove',
				backgroundColor: '#2D303A',
				padding: [8, 12],
				formatter: props.tooltipFormat,
				color: '#2D303A',
				textStyle: {
					rich: {
						a: {
							fontSize: 12,
							color: '#2D303A',
							lineHeight: 12,
							align: 'left',
							padding: [4, 4, 4, 4]
						}
					}
				}
			},
			legend: [{
				orient: 'horizontal',
				top: 6,
				left: 6,
				data: props.legendData
			}],
			series: [{
				type: 'graph',
				layout: 'force',
				nodeScaleRatio: 1.2,
				draggable: true,
				animation: false,
				data: props.seriesData,
				roam: true,
				symbol: 'roundRect',
				symbolSize: 70,
				categories: props.legendData,
				label: {
					show: props.labelShow,
					position: 'inside',
					formatter: props.labelFormat,
					color: '#222222',
					textStyle: {
						rich: {
							a: {
								fontSize: 12,
								color: '#222222',
								lineHeight: 12,
								align: 'left',
								padding: [4, 4, 4, 4]
							}
						}
					}
				},
				edgeSymbol: ['circle', 'arrow'],
				edgeSymbolSize: [4, 12],
				force: {
					repulsion: 1000,
					edgeLength: 300
				},
				links: props.linksData,
				lineStyle: {
					color: '#999999'
				}
			}]
		}

		initChart(graphChartRef, option)

		return { graphChartRef }
	},
	render() {
		const { height, width } = this
		return (
			<div
				ref='graphChartRef'
				style={{
					height: typeof height === 'number' ? height + 'px' : height,
					width: typeof width === 'number' ? width + 'px' : width
				}}
			/>
		)
	}
})

export default GraphChart
