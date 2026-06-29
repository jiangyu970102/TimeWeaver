<template>
  <div class="yearly-report">
    <!-- Year Selector -->
    <el-card shadow="hover" class="mb-20">
      <div class="year-nav">
        <el-button @click="shiftYear(-1)" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="year-title">{{ year }} 年时间报告</h2>
        <el-button @click="shiftYear(1)" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </el-card>

    <!-- Summary Cards -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="stat-label">总时长</div>
          <div class="stat-value primary">{{ formatHours(data.totalMinutes) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="stat-label">记录条数</div>
          <div class="stat-value">{{ data.totalRecords }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="stat-label">活跃天数</div>
          <div class="stat-value">{{ data.activeDays }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="stat-label">日均时长</div>
          <div class="stat-value">{{ formatHours(data.avgDailyMinutes) }}/天</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Best Month & Monthly Trend -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>月度趋势</span>
              <el-tag type="success" effect="plain" v-if="data.bestMonth">
                最佳月份：{{ data.bestMonth }} 月（{{ formatHours(data.bestMonthMinutes) }}）
              </el-tag>
            </div>
          </template>
          <div ref="monthlyChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Category Breakdown -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>全年分类统计</span></template>
          <el-table :data="data.categoryBreakdown" empty-text="暂无数据" size="small" stripe>
            <el-table-column prop="name" label="分类" width="160">
              <template #default="{ row }">
                <el-tag :color="getCatColor(row.categoryId)" size="small" effect="dark" style="color: #fff; border: none">
                  {{ row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="minutes" label="时长 (min)" width="120" sortable />
            <el-table-column label="占比" min-width="200">
              <template #default="{ row }">
                <div class="ratio-bar-wrap">
                  <div class="ratio-bar" :style="{ width: getRatio(row.minutes) + '%', background: getCatColor(row.categoryId) }" />
                  <span class="ratio-text">{{ getRatio(row.minutes) }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getCategories } from '@/api/categories'

const year = ref(new Date().getFullYear())
const data = ref({
  totalMinutes: 0, totalRecords: 0, activeDays: 0, avgDailyMinutes: 0,
  bestMonth: 0, bestMonthMinutes: 0,
  monthlyBreakdown: [], categoryBreakdown: [],
})
const categories = ref([])
const monthlyChartRef = ref(null)
let chart = null

const monthNames = ['', '1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

function formatHours(minutes) {
  if (!minutes && minutes !== 0) return '—'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h > 0 ? `${h}h${m}min` : `${m}min`
}

function getCatColor(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.color || '#909399'
}

function getRatio(minutes) {
  const total = data.value.totalMinutes
  if (!total) return 0
  return Math.round((minutes / total) * 100)
}

function shiftYear(delta) {
  year.value += delta
  loadData()
}

function renderChart() {
  if (!monthlyChartRef.value || !data.value.monthlyBreakdown?.length) return
  chart?.dispose()
  chart = echarts.init(monthlyChartRef.value)

  const months = data.value.monthlyBreakdown.map(m => monthNames[m.month])
  const minutes = data.value.monthlyBreakdown.map(m => m.totalMinutes)
  const activeDays = data.value.monthlyBreakdown.map(m => m.activeDays)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const m = params[0]
        const idx = months.indexOf(m.name)
        const d = data.value.monthlyBreakdown[idx]
        return `${m.name}<br/>时长：${formatHours(d.totalMinutes)}<br/>记录数：${d.totalRecords}<br/>活跃天数：${d.activeDays}`
      },
    },
    xAxis: { type: 'category', data: months, axisLabel: { interval: 0 } },
    yAxis: [
      { type: 'value', name: '时长 (min)' },
      { type: 'value', name: '活跃天数', min: 0, max: 31 },
    ],
    series: [
      {
        name: '时长',
        type: 'bar',
        data: minutes,
        itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '活跃天数',
        type: 'line',
        yAxisIndex: 1,
        data: activeDays,
        lineStyle: { color: '#67c23a' },
        itemStyle: { color: '#67c23a' },
      },
    ],
  })
}

async function loadData() {
  const { getDailyStats } = await import('@/api/stats')
  try {
    const url = `/api/stats/yearly?year=${year.value}`
    const request = (await import('@/utils/request')).default
    const res = await request.get(url)
    if (res.code === 200 && res.data) {
      data.value = res.data
      await nextTick()
      renderChart()
    }
  } catch { /* ignore */ }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {}
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.yearly-report { max-width: 1000px; }
.mb-20 { margin-bottom: 20px; }
.year-nav { display: flex; align-items: center; justify-content: center; gap: 16px; }
.year-title { margin: 0; font-size: 22px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.summary-card { text-align: center; padding: 8px 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 26px; font-weight: 700; color: #303133; }
.stat-value.primary { color: #409eff; }
.ratio-bar-wrap { display: flex; align-items: center; gap: 8px; }
.ratio-bar { height: 20px; border-radius: 4px; min-width: 4px; transition: width 0.3s; }
.ratio-text { font-size: 13px; color: #606266; white-space: nowrap; }
</style>
