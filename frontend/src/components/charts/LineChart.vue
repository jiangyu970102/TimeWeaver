<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, TooltipComponent, GridComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
  smooth: { type: Boolean, default: true },
  color: { type: String, default: '#409eff' },
  showArea: { type: Boolean, default: true },
  dateFormat: { type: String, default: 'MM-DD' },
})

let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value || !props.data.length) return
  if (!chart) chart = echarts.init(chartRef.value)

  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => `${params[0].name}<br/>${params[0].value}min` },
    grid: { left: 40, right: 16, top: 16, bottom: 28 },
    xAxis: {
      type: 'category',
      data: props.data.map(d => {
        if (d.date) return d.date.slice(5)
        if (d.label) return d.label
        return ''
      }),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', name: '分钟', nameTextStyle: { fontSize: 11 } },
    series: [{
      type: 'line',
      data: props.data.map(d => d.minutes ?? d.value ?? 0),
      smooth: props.smooth,
      lineStyle: { color: props.color, width: 2 },
      areaStyle: props.showArea ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: props.color + '4d' },
          { offset: 1, color: props.color + '0d' },
        ]),
      } : undefined,
      symbol: 'circle',
      symbolSize: 6,
    }],
  }
  chart.setOption(option)
  chart.resize()
}

function handleResize() { chart?.resize() }

watch(() => props.data, () => nextTick(render), { deep: true })

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>
