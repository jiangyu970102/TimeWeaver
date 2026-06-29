<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const props = defineProps({
  categories: { type: Array, default: () => [] },
  current: { type: Array, default: () => [] },
  previous: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
})

let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value || !props.categories.length) return
  if (!chart) chart = echarts.init(chartRef.value)

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0 },
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    xAxis: {
      type: 'category',
      data: props.categories,
      axisLabel: { rotate: 30, fontSize: 11 },
    },
    yAxis: { type: 'value', name: '分钟', nameTextStyle: { fontSize: 11 } },
    series: [
      {
        name: '本周',
        type: 'bar',
        data: props.categories.map((cat, i) => props.current[i] || 0),
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '上周',
        type: 'bar',
        data: props.categories.map((cat, i) => props.previous[i] || 0),
        itemStyle: { color: '#a0cfff', borderRadius: [4, 4, 0, 0] },
      },
    ],
  }
  chart.setOption(option)
  chart.resize()
}

function handleResize() { chart?.resize() }

watch(() => [props.categories, props.current, props.previous], () => nextTick(render), { deep: true })

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>
