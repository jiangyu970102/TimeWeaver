<template>
  <div class="report-page">
    <!-- Week Selector -->
    <el-card class="mb-20">
      <div class="week-nav">
        <el-button @click="shiftWeek(-1)" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="week-label">{{ weekLabel }}</span>
        <el-button @click="shiftWeek(1)" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button @click="resetWeek" size="small" class="ml-4">本周</el-button>
        <el-button type="primary" size="small" @click="generateReport" :loading="generating" class="ml-4">
          生成周报
        </el-button>
      </div>
    </el-card>

    <!-- Loading -->
    <el-skeleton :loading="loading" animated :count="3" v-if="loading" />

    <!-- Report Content -->
    <template v-if="!loading && report">
      <el-card class="mb-20 report-summary-card">
        <template #header>
          <div class="card-header">
            <span>📊 本周概览</span>
            <el-tag type="success" effect="dark" v-if="report.reportStatus === 'completed'">
              已生成
            </el-tag>
            <el-tag type="warning" v-else-if="report.reportStatus === 'pending'">
              生成中
            </el-tag>
            <el-tag type="danger" v-else>
              生成失败
            </el-tag>
          </div>
        </template>
        <div class="stats-grid">
          <div class="stat-item" v-if="statsSnapshot">
            <div class="stat-value">{{ formatDuration(statsSnapshot.totalMinutes) }}</div>
            <div class="stat-label">总时长</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ activeDays }}</div>
            <div class="stat-label">活跃天数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ formatDuration(avgDaily) }}</div>
            <div class="stat-label">日均时长</div>
          </div>
          <div class="stat-item">
            <div class="stat-value" :class="changeClass">{{ changePercentText }}</div>
            <div class="stat-label">环比变化</div>
          </div>
        </div>
        <el-divider />
        <p class="report-summary">{{ report.summary }}</p>
      </el-card>

      <!-- AI Insights -->
      <el-row :gutter="16" class="mb-20">
        <el-col :span="12">
          <el-card class="insight-card insight-strengths">
            <template #header><span>💪 优点</span></template>
            <ul v-if="strengths.length">
              <li v-for="(s, i) in strengths" :key="i">{{ s }}</li>
            </ul>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="insight-card insight-weaknesses">
            <template #header><span>🎯 待改进</span></template>
            <ul v-if="weaknesses.length">
              <li v-for="(w, i) in weaknesses" :key="i">{{ w }}</li>
            </ul>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <!-- Suggestions -->
      <el-card class="mb-20">
        <template #header><span>🚀 改进建议</span></template>
        <el-timeline v-if="suggestions.length">
          <el-timeline-item
            v-for="(s, i) in suggestions"
            :key="i"
            :timestamp="`建议 #${i + 1}`"
          >
            {{ s }}
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无建议" :image-size="60" />
      </el-card>

      <!-- Trend Mini Chart -->
      <el-card v-if="statsSnapshot && statsSnapshot.trend && statsSnapshot.trend.length" class="mb-20">
        <template #header><span>📈 每日趋势</span></template>
        <div ref="miniChartRef" class="mini-chart"></div>
      </el-card>

      <!-- Generation Info -->
      <div class="report-footer" v-if="report.createdAt">
        <span>生成时间：{{ report.createdAt }}</span>
      </div>
    </template>

    <el-empty v-else-if="!loading && !report" description="暂无周报，点击「生成周报」创建" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getWeeklyReport, generateReport as apiGenerateReport } from '@/api/reports'

echarts.use([LineChart, TooltipComponent, GridComponent, CanvasRenderer])

const weekOffset = ref(0)
const report = ref(null)
const loading = ref(false)
const generating = ref(false)
const miniChartRef = ref(null)
let miniChart = null

function getWeekInfo(offset) {
  const d = new Date()
  d.setDate(d.getDate() + offset * 7)
  const year = d.getFullYear()
  const start = new Date(d)
  start.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1))
  const weekNum = Math.ceil(((start - new Date(year, 0, 1)) / 86400000 + new Date(year, 0, 1).getDay() + 1) / 7)
  return { year, week: weekNum }
}

const weekInfo = computed(() => getWeekInfo(weekOffset.value))

const weekLabel = computed(() => {
  const { year, week } = weekInfo.value
  return `${year} 年第 ${week} 周`
})

const statsSnapshot = computed(() => {
  if (!report.value?.statsSnapshot) return null
  try {
    return JSON.parse(report.value.statsSnapshot)
  } catch { return null }
})

const totalMinutes = computed(() => {
  return statsSnapshot.value?.trend?.reduce((s, d) => s + (d.minutes || 0), 0) || 0
})

const activeDays = computed(() => {
  return statsSnapshot.value?.trend?.filter(d => d.minutes > 0).length || 0
})

const avgDaily = computed(() => {
  return activeDays.value > 0 ? Math.round(totalMinutes.value / activeDays.value) : 0
})

const changePercentText = computed(() => {
  const cp = statsSnapshot.value?.comparison?.changePercent
  if (cp === undefined || cp === null) return '—'
  return cp > 0 ? `+${cp}%` : `${cp}%`
})

const changeClass = computed(() => {
  const cp = statsSnapshot.value?.comparison?.changePercent
  if (cp > 0) return 'change-up'
  if (cp < 0) return 'change-down'
  return ''
})

const strengths = computed(() => {
  if (!report.value?.strengths) return []
  try { return JSON.parse(report.value.strengths) } catch { return [report.value.strengths] }
})

const weaknesses = computed(() => {
  if (!report.value?.weaknesses) return []
  try { return JSON.parse(report.value.weaknesses) } catch { return [report.value.weaknesses] }
})

const suggestions = computed(() => {
  if (!report.value?.suggestions) return []
  try { return JSON.parse(report.value.suggestions) } catch { return [report.value.suggestions] }
})

function formatDuration(min) {
  if (!min && min !== 0) return '—'
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h > 0) return m > 0 ? `${h}h${m}m` : `${h}h`
  return `${m}m`
}

function shiftWeek(delta) { weekOffset.value += delta }

function resetWeek() { weekOffset.value = 0 }

async function loadReport() {
  loading.value = true
  try {
    const { year, week } = weekInfo.value
    const res = await getWeeklyReport({ year, week })
    if (res.code === 200) {
      report.value = res.data
      await nextTick()
      renderMiniChart()
    }
  } catch {} finally {
    loading.value = false
  }
}

async function generateReport() {
  generating.value = true
  try {
    const { year, week } = weekInfo.value
    const res = await apiGenerateReport({ year, week })
    if (res.code === 200) {
      report.value = res.data
      ElMessage.success('周报已生成')
      await nextTick()
      renderMiniChart()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

function renderMiniChart() {
  if (!miniChartRef.value || !statsSnapshot.value?.trend) return
  if (!miniChart) miniChart = echarts.init(miniChartRef.value)

  const data = statsSnapshot.value.trend
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 28 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date.slice(5)),
      axisLabel: { fontSize: 11 },
    },
    yAxis: { type: 'value', name: '分钟', nameTextStyle: { fontSize: 11 } },
    series: [{
      type: 'line',
      data: data.map(d => d.minutes),
      smooth: true,
      lineStyle: { color: '#409eff', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.3)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' },
        ]),
      },
      symbol: 'circle',
      symbolSize: 6,
    }],
  }
  miniChart.setOption(option)
  miniChart.resize()
}

function resizeChart() {
  miniChart?.resize()
}

watch(weekInfo, () => { loadReport() })

onMounted(() => {
  loadReport()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  miniChart?.dispose()
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.ml-4 { margin-left: 8px; }

.week-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.week-label {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  min-width: 140px;
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.change-up { color: #67c23a; }
.change-down { color: #f56c6c; }

.report-summary {
  font-size: 16px;
  color: #303133;
  line-height: 1.8;
  padding: 8px 0;
}

.insight-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.insight-card li {
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  font-size: 14px;
}

.insight-strengths li {
  background: #f0f9eb;
  color: #67c23a;
}

.insight-weaknesses li {
  background: #fef0f0;
  color: #f56c6c;
}

.mini-chart {
  width: 100%;
  height: 250px;
}

.report-footer {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  padding: 16px 0;
}

.report-summary-card {
  border-left: 4px solid #409eff;
}
</style>
