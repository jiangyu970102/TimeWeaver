<template>
  <div class="stats-page">
    <!-- Period Selector -->
    <el-card class="mb-20">
      <div class="period-nav">
        <el-radio-group v-model="period" @change="loadAll">
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
        </el-radio-group>
        <el-button v-if="period === 'week'" @click="shiftWeek(-1)" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="period-label">{{ periodLabel }}</span>
        <el-button v-if="period === 'week'" @click="shiftWeek(1)" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button @click="resetPeriod" size="small" class="ml-4">本周</el-button>
      </div>
    </el-card>

    <!-- Summary Cards -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="6" v-for="card in summaryCards" :key="card.label">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">{{ card.label }}</div>
          <div class="summary-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 1: Pie + Line -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="12">
        <el-card>
          <template #header><span>分类占比</span></template>
          <PieChart :data="pieData" height="320px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>每日趋势</span></template>
          <LineChart :data="trendData" height="320px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 2: Sankey + Radar -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="12">
        <el-card>
          <template #header><span>时间流向</span></template>
          <SankeyChart :data="pieData" height="360px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>效率评估</span></template>
          <RadarChart :data="radarData" :indicator="radarIndicators" height="320px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 3: Comparison Bar -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="24">
        <el-card>
          <template #header><span>本周 vs 上周 分类耗时对比</span></template>
          <BarChart
            :categories="comparisonCategories"
            :current="comparisonCurrent"
            :previous="comparisonPrevious"
            height="320px"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 4: Heatmap -->
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>月度热力图</span>
              <el-date-picker
                v-model="heatmapMonth"
                type="month"
                format="YYYY-MM"
                value-format="YYYY-MM"
                @change="loadHeatmap"
                size="small"
                style="width: 140px"
              />
            </div>
          </template>
          <HeatMap :data="heatmapChartData" :year="heatmapYear" :month="heatmapMonthNum" height="280px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import PieChart from '@/components/charts/PieChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import HeatMap from '@/components/charts/HeatMap.vue'
import SankeyChart from '@/components/charts/SankeyChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import { getTrendData, getComparisonData, getCategoryStats, getHeatmapData } from '@/api/stats'

const period = ref('week')
const weekOffset = ref(0)
const heatmapMonth = ref(new Date().toISOString().slice(0, 7))

const pieData = ref([])
const trendData = ref([])
const heatmapChartData = ref([])
const radarData = ref([])
const radarIndicators = ref([])
const comparisonCategories = ref([])
const comparisonCurrent = ref([])
const comparisonPrevious = ref([])
const summaryData = ref({ totalMinutes: 0, recordCount: 0, avgDaily: 0, prevTotal: 0, changePercent: 0 })

const heatmapYear = computed(() => parseInt(heatmapMonth.value.split('-')[0]))
const heatmapMonthNum = computed(() => parseInt(heatmapMonth.value.split('-')[1]))

function getWeekRange(offset) {
  const d = new Date()
  d.setDate(d.getDate() + offset * 7)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  const mon = new Date(d.setDate(diff))
  const sun = new Date(mon.getTime() + 6 * 86400000)
  return {
    start: mon.toISOString().slice(0, 10),
    end: sun.toISOString().slice(0, 10),
  }
}

function getMonthRange(ym) {
  const [y, m] = ym.split('-').map(Number)
  const last = new Date(y, m, 0).getDate()
  return {
    start: `${ym}-01`,
    end: `${ym}-${String(last).padStart(2, '0')}`,
  }
}

const range = computed(() => {
  if (period.value === 'week') return getWeekRange(weekOffset.value)
  return getMonthRange(heatmapMonth.value)
})

const periodLabel = computed(() => {
  const { start, end } = range.value
  return `${start} ~ ${end}`
})

const summaryCards = computed(() => [
  { label: '总时长', value: formatDuration(summaryData.value.totalMinutes), color: '#409eff' },
  { label: '记录数', value: `${summaryData.value.recordCount}`, color: '#67c23a' },
  { label: '日均', value: formatDuration(summaryData.value.avgDaily), color: '#e6a23c' },
  {
    label: '环比变化',
    value: summaryData.value.changePercent > 0
      ? `+${summaryData.value.changePercent}%`
      : `${summaryData.value.changePercent}%`,
    color: summaryData.value.changePercent >= 0 ? '#67c23a' : '#f56c6c',
  },
])

function formatDuration(min) {
  if (!min && min !== 0) return '—'
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h > 0) return m > 0 ? `${h}h${m}m` : `${h}h`
  return `${m}m`
}

function shiftWeek(delta) { weekOffset.value += delta }

function resetPeriod() {
  weekOffset.value = 0
  period.value = 'week'
  heatmapMonth.value = new Date().toISOString().slice(0, 7)
}

// ---- API calls ----
async function loadAll() { await Promise.all([loadSummary(), loadCategoryPie(), loadTrendLine(), loadComparison(), loadRadar()]) }

async function loadSummary() {
  try {
    const { start, end } = range.value
    const [trendRes, compRes] = await Promise.all([
      getTrendData({ start, end }),
      getComparisonData({ currentStart: start, currentEnd: end }),
    ])
    if (trendRes.code === 200 && compRes.code === 200) {
      const data = trendRes.data
      const total = data.reduce((s, d) => s + d.minutes, 0)
      const days = data.filter(d => d.minutes > 0).length || 1
      summaryData.value = {
        totalMinutes: total,
        recordCount: data.filter(d => d.minutes > 0).length,
        avgDaily: Math.round(total / days),
        prevTotal: compRes.data.previousTotal,
        changePercent: compRes.data.changePercent,
      }
    }
  } catch {}
}

async function loadCategoryPie() {
  try {
    const { start, end } = range.value
    const res = await getCategoryStats({ start, end })
    if (res.code === 200 && res.data.breakdown) {
      pieData.value = res.data.breakdown
    }
  } catch {}
}

async function loadTrendLine() {
  try {
    const { start, end } = range.value
    const res = await getTrendData({ start, end })
    if (res.code === 200) trendData.value = res.data
  } catch {}
}

async function loadHeatmap() {
  try {
    const [y, m] = heatmapMonth.value.split('-')
    const res = await getHeatmapData({ year: y, month: m })
    if (res.code === 200) heatmapChartData.value = res.data
  } catch {}
}

async function loadComparison() {
  try {
    // Get current period stats and previous period for bar comparison
    const { start, end } = range.value
    const startDate = new Date(start)
    const endDate = new Date(end)
    const duration = endDate.getTime() - startDate.getTime()
    const prevStart = new Date(startDate.getTime() - duration - 86400000).toISOString().slice(0, 10)
    const prevEnd = new Date(startDate.getTime() - 86400000).toISOString().slice(0, 10)

    const [currentRes, previousRes] = await Promise.all([
      getCategoryStats({ start, end }),
      getCategoryStats({ start: prevStart, end: prevEnd }),
    ])

    if (currentRes.code === 200 && currentRes.data.breakdown) {
      const current = currentRes.data.breakdown
      const previous = previousRes?.code === 200 && previousRes.data?.breakdown ? previousRes.data.breakdown : []

      comparisonCategories.value = current.map(d => d.name)
      comparisonCurrent.value = current.map(d => d.minutes)
      comparisonPrevious.value = comparisonCategories.value.map(cat => {
        const prev = previous.find(p => p.name === cat)
        return prev ? prev.minutes : 0
      })
    }
  } catch {}
}

async function loadRadar() {
  try {
    const { start, end } = range.value
    const res = await getTrendData({ start, end })
    if (res.code === 200 && res.data.length) {
      const data = res.data
      const total = data.reduce((s, d) => s + d.minutes, 0)
      const days = data.filter(d => d.minutes > 0).length
      const avg = days > 0 ? Math.round(total / days) : 0
      const maxDay = Math.max(...data.map(d => d.minutes), 1)
      const stability = data.length > 0
        ? Math.round((data.filter(d => d.minutes >= avg * 0.5).length / data.length) * 100)
        : 0
      const balance = data.length > 0
        ? Math.round((days / data.length) * 100)
        : 0

      radarIndicators.value = [
        { name: '日均时长', max: Math.max(avg * 2, 240) },
        { name: '活跃度', max: 100 },
        { name: '稳定性', max: 100 },
        { name: '均衡性', max: 100 },
        { name: '峰值', max: Math.max(maxDay, 240) },
      ]

      radarData.value = [{
        name: '当前周期',
        日均时长: Math.min(avg, radarIndicators.value[0].max),
        活跃度: balance,
        稳定性: stability,
        均衡性: balance,
        峰值: Math.min(maxDay, radarIndicators.value[4].max),
      }]
    }
  } catch {}
}

onMounted(async () => {
  await nextTick()
  await loadAll()
  await loadHeatmap()
})

onUnmounted(() => {})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.ml-4 { margin-left: 8px; }

.period-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.period-label {
  font-size: 14px;
  color: #606266;
  min-width: 200px;
  text-align: center;
}

.summary-card {
  text-align: center;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
