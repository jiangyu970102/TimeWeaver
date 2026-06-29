<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { HeatmapChart } from 'echarts/charts'
import { TooltipComponent, GridComponent, VisualMapComponent, CalendarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([HeatmapChart, TooltipComponent, GridComponent, VisualMapComponent, CalendarComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '280px' },
  year: { type: Number, default: () => new Date().getFullYear() },
  month: { type: Number, default: () => new Date().getMonth() + 1 },
})

let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const daysInMonth = new Date(props.year, props.month, 0).getDate()
  const firstDay = new Date(props.year, props.month - 1, 1).getDay()

  const heatData = props.data.map(d => {
    const day = parseInt(d.date.slice(8))
    const weekIdx = Math.floor((day + firstDay - 1) / 7)
    return [weekIdx, (day + firstDay - 1) % 7, d.minutes]
  })

  const weeks = Math.ceil((daysInMonth + firstDay) / 7)

  const option = {
    tooltip: {
      formatter: (params) => {
        const day = params.value[1] - firstDay + 1 + params.value[0] * 7
        const actual = props.data.find(d => parseInt(d.date.slice(8)) === day)
        return actual ? `${actual.date}<br/>${actual.minutes}min` : ''
      },
    },
    grid: { left: 40, right: 16, top: 10, bottom: 10 },
    xAxis: {
      type: 'category',
      data: Array.from({ length: weeks }, (_, i) => `第${i + 1}周`),
      axisLabel: { fontSize: 11 },
    },
    yAxis: {
      type: 'category',
      data: ['日', '一', '二', '三', '四', '五', '六'],
      axisLabel: { fontSize: 11 },
    },
    visualMap: {
      min: 0,
      max: Math.max(240, ...props.data.map(d => d.minutes)),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#f0f9ff', '#bae0ff', '#69b1ff', '#409eff', '#1677ff', '#0958d9'] },
    },
    series: [{
      type: 'heatmap',
      data: heatData,
      label: { show: heatData.some(d => d[2] > 0), color: '#333', fontSize: 10 },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } },
    }],
  }
  chart.setOption(option)
  chart.resize()
}

function handleResize() { chart?.resize() }

watch(() => [props.data, props.year, props.month], () => nextTick(render), { deep: true })

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>
