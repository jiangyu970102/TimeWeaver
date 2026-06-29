<template>
  <div :ref="el => chartRef = el" :style="{ width: '100%', height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { SankeyChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([SankeyChart, TooltipComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '360px' },
})

let chart = null
const chartRef = ref(null)

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  // Build sankey nodes and links from category breakdown
  // data format: [{ type, name, minutes }]
  const types = [...new Set(props.data.map(d => d.type || '其他'))]
  const nodes = [
    ...types.map(t => ({ name: t, itemStyle: { color: typeColor(t) } })),
    ...props.data.map(d => ({ name: d.name, itemStyle: { color: d.color || '#409eff' } })),
  ]

  const links = props.data.map(d => ({
    source: d.type || '其他',
    target: d.name,
    value: d.minutes,
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => `${params.source} → ${params.target}: ${params.value}min`,
    },
    series: [{
      type: 'sankey',
      layout: 'none',
      emphasis: { focus: 'adjacency' },
      nodeAlign: 'left',
      nodeWidth: 20,
      nodeGap: 12,
      data: nodes,
      links,
      label: { fontSize: 12 },
    }],
  }
  chart.setOption(option)
  chart.resize()
}

function typeColor(type) {
  const map = { work: '#e74c3c', study: '#3498db', entertain: '#f39c12', rest: '#2ecc71', social: '#9b59b6', other: '#95a5a6' }
  return map[type] || '#95a5a6'
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
