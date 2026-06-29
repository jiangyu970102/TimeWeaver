<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([RadarChart, TooltipComponent, LegendComponent, RadarComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
  indicator: { type: Array, default: () => [] },
})

let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, data: props.data.map(d => d.name) },
    radar: {
      indicator: props.indicator.length ? props.indicator :
        props.data.length ? Object.keys(props.data[0]).filter(k => k !== 'name').map(k => ({ name: k, max: 100 })) : [],
      center: ['50%', '45%'],
      radius: '65%',
    },
    series: [{
      type: 'radar',
      data: props.data.map(d => ({
        value: Object.keys(d).filter(k => k !== 'name').map(k => d[k]),
        name: d.name,
      })),
      areaStyle: { opacity: 0.1 },
      lineStyle: { width: 2 },
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
