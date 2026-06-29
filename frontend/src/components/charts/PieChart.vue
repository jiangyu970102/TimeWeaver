<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
  colors: {
    type: Array,
    default: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c', '#e74c3c', '#3498db', '#2ecc71'],
  },
  radius: { type: Array, default: () => ['40%', '65%'] },
})

const emit = defineEmits(['chart-click'])
let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value || !props.data.length) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}min ({d}%)' },
    legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: props.radius,
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: props.data.map((d, i) => ({
        value: d.minutes,
        name: d.name,
        itemStyle: { color: props.colors[i % props.colors.length] },
      })),
    }],
  })
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
