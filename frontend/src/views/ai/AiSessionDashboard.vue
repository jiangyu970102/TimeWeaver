<template>
  <div class="ai-session-dashboard">
    <div class="page-header">
      <h2>AI 会话分析</h2>
      <div class="header-actions">
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inline-prompt
          style="margin-right: 8px"
        />
        <el-tag v-if="autoRefresh" type="success" size="small" effect="plain" class="refresh-indicator">
          每 {{ refreshInterval / 1000 }}s 刷新
        </el-tag>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadData"
        />
        <el-button type="primary" @click="showCreateDialog = true">
          新增记录
        </el-button>
      </div>
    </div>

    <!-- Summary Cards -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">总会话数</div>
            <div class="stat-value">{{ summary.totalSessions ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">总使用时长</div>
            <div class="stat-value">{{ totalDurationText }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">日均会话</div>
            <div class="stat-value">{{ avgDaily }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts with tabs -->
    <el-card shadow="hover" class="chart-card">
      <el-tabs v-model="activeChartTab">
        <el-tab-pane label="工具分布" name="tools">
          <el-row :gutter="16">
            <el-col :span="12">
              <div ref="toolChartRef" class="chart-box chart-sm"></div>
            </el-col>
            <el-col :span="12">
              <div ref="modelChartRef" class="chart-box chart-sm"></div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="每日趋势" name="trend">
          <div ref="trendChartRef" class="chart-box"></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Session List with Pagination -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="list-header">
          <span>会话记录</span>
          <span class="list-count">共 {{ sessions.length }} 条</span>
        </div>
      </template>
      <el-table :data="paginatedSessions" v-loading="loading" empty-text="暂无AI会话记录" stripe style="width:100%">
        <el-table-column prop="toolName" label="工具" width="130">
          <template #default="{ row }">
            <el-tag :type="toolTagType(row.toolName)">{{ row.toolName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="170" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="durationSec" label="使用时长" width="110" align="center">
          <template #default="{ row }">
            <span class="duration-value">{{ row.durationSec ? formatDuration(row.durationSec) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="70" align="center" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="sessions.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showCreateDialog" :title="editingSession ? '编辑会话' : '新增AI会话'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="AI 工具" required>
          <el-select v-model="form.toolName" placeholder="选择工具" style="width:100%">
            <el-option label="Claude Code" value="claude-code" />
            <el-option label="Cursor" value="cursor" />
            <el-option label="Codex" value="codex" />
            <el-option label="ChatGPT" value="chatgpt" />
            <el-option label="Kimi" value="kimi" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="大模型" required>
          <el-select v-model="form.modelName" placeholder="选择模型" style="width:100%">
            <el-option label="DeepSeek V4 Flash" value="deepseek-v4-flash" />
            <el-option label="GPT-4o" value="gpt-4o" />
            <el-option label="Claude Sonnet" value="claude-sonnet" />
            <el-option label="Kimi K2" value="kimi-k2" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { getAiSessions, getAiSessionStats, createAiSession, updateAiSession, deleteAiSession } from '@/api/ai-sessions'
import * as echarts from 'echarts'

const dateRange = ref([])
const sessions = ref([])
const summary = ref({})
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingSession = ref(null)
const form = ref(initForm())
const autoRefresh = ref(true)
const refreshInterval = 30000 // 30s
const activeChartTab = ref('tools')
const currentPage = ref(1)
const pageSize = ref(10)
let refreshTimer = null

const toolChartRef = ref(null)
const modelChartRef = ref(null)
const trendChartRef = ref(null)
let toolChart = null
let modelChart = null
let trendChart = null

function initForm() {
  return {
    toolName: '',
    modelName: '',
    startTime: '',
    endTime: '',
  }
}

const avgDaily = computed(() => {
  if (!dateRange.value || dateRange.value.length < 2) return '0'
  const days = Math.ceil((new Date(dateRange.value[1]) - new Date(dateRange.value[0])) / (1000 * 86400)) + 1
  return ((summary.value.totalSessions || 0) / days).toFixed(1)
})

const paginatedSessions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sessions.value.slice(start, start + pageSize.value)
})

const totalDurationText = computed(() => {
  const total = sessions.value.reduce((sum, s) => sum + (s.durationSec || 0), 0)
  if (!total) return '0'
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  return h > 0 ? `${h}时${m}分` : `${m}分`
})

function toolTagType(tool) {
  const map = { 'claude-code': 'success', 'cursor': 'primary', 'codex': 'warning', 'chatgpt': 'info', 'kimi': 'danger' }
  return map[tool] || ''
}

function formatNumber(n) {
  if (!n) return '0'
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return String(n)
}

function formatDuration(sec) {
  if (!sec) return '-'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

async function loadData() {
  loading.value = true
  try {
    let startDate, endDate
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = dateRange.value[0]
      endDate = dateRange.value[1]
    } else {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      startDate = start.toISOString().slice(0, 10)
      endDate = end.toISOString().slice(0, 10)
      dateRange.value = [startDate, endDate]
    }
    const [sessionsRes, statsRes] = await Promise.all([
      getAiSessions({ startDate, endDate }),
      getAiSessionStats({ startDate, endDate }),
    ])
    sessions.value = sessionsRes.data || []
    summary.value = statsRes.data?.summary || {}
    await nextTick()
    renderCharts(statsRes.data)
  } catch (e) {
    console.error('Failed to load AI session data', e)
  } finally {
    loading.value = false
  }
}

function renderCharts(data) {
  if (!data) return
  // Cleanup
  toolChart?.dispose()
  modelChart?.dispose()
  trendChart?.dispose()

  const isDark = localStorage.getItem('timeweaver-dark-mode') === 'true'
  const labelColor = isDark ? '#c0c0c0' : '#606266'
  const axisColor = isDark ? '#555' : '#ddd'

  const textStyle = { color: labelColor }
  const commonOpts = {
    textStyle,
    tooltip: { textStyle: { color: '#333' } },
  }

  // Tool pie
  if (toolChartRef.value) {
    toolChart = echarts.init(toolChartRef.value)
    toolChart.setOption({
      ...commonOpts,
      tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: (data.toolStats || []).map(t => ({
          name: t.name,
          value: t.count,
        })),
        label: { show: true, formatter: '{b}: {c}', color: labelColor },
      }],
    })
  }

  // Model pie
  if (modelChartRef.value) {
    modelChart = echarts.init(modelChartRef.value)
    modelChart.setOption({
      ...commonOpts,
      tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: (data.modelStats || []).map(m => ({
          name: m.name,
          value: m.count,
        })),
        label: { show: true, formatter: '{b}: {c}', color: labelColor },
      }],
    })
  }

  // Daily trend
  if (trendChartRef.value && data.dailyTrend) {
    const dates = [...new Set(data.dailyTrend.map(d => d.date))].sort()
    const tools = [...new Set(data.dailyTrend.map(d => d.tool_name))]
    const colorMap = { 'claude-code': '#10a37f', 'cursor': '#6c5ce7', 'codex': '#1a73e8', 'chatgpt': '#74aa9c' }
    const series = tools.map(tool => ({
      name: tool,
      type: 'bar',
      stack: 'total',
      data: dates.map(date => {
        const item = data.dailyTrend.find(d => d.date === date && d.tool_name === tool)
        return item ? item.count : 0
      }),
      itemStyle: { color: colorMap[tool] || '#999' },
    }))

    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      ...commonOpts,
      tooltip: { trigger: 'axis' },
      legend: { data: tools, textStyle },
      xAxis: { type: 'category', data: dates, axisLabel: { color: labelColor }, axisLine: { lineStyle: { color: axisColor } } },
      yAxis: { type: 'value', minInterval: 1, axisLabel: { color: labelColor }, splitLine: { lineStyle: { color: axisColor } } },
      series,
    })
  }
}

function handleEdit(row) {
  editingSession.value = row
  form.value = {
    toolName: row.toolName,
    modelName: row.modelName,
    startTime: row.startTime,
    endTime: row.endTime,
  }
  showCreateDialog.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editingSession.value) {
      await updateAiSession(editingSession.value.id, form.value)
    } else {
      await createAiSession(form.value)
    }
    showCreateDialog.value = false
    editingSession.value = null
    form.value = initForm()
    await loadData()
  } catch (e) {
    console.error('Failed to save AI session', e)
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteAiSession(id)
    await loadData()
  } catch (e) {
    console.error('Failed to delete AI session', e)
  }
}

function startAutoRefresh() {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    if (autoRefresh.value && !loading.value) {
      loadDataSilent()
    }
  }, refreshInterval)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// Silent reload without showing loading spinner
async function loadDataSilent() {
  try {
    let startDate, endDate
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = dateRange.value[0]
      endDate = dateRange.value[1]
    } else {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      startDate = start.toISOString().slice(0, 10)
      endDate = end.toISOString().slice(0, 10)
      dateRange.value = [startDate, endDate]
    }
    const [sessionsRes, statsRes] = await Promise.all([
      getAiSessions({ startDate, endDate }),
      getAiSessionStats({ startDate, endDate }),
    ])
    sessions.value = sessionsRes.data || []
    summary.value = statsRes.data?.summary || {}
    renderCharts(statsRes.data)
  } catch (e) {
    // silent fail on auto-refresh
  }
}

function handleResize() {
  toolChart?.resize()
  modelChart?.resize()
  trendChart?.resize()
}

// Watch autoRefresh toggle
watch(autoRefresh, (val) => {
  if (val) startAutoRefresh()
  else stopAutoRefresh()
})

// Reset to page 1 when data reloads
watch(sessions, () => {
  currentPage.value = 1
})

// Resize chart when switching tab
watch(activeChartTab, () => {
  nextTick(() => trendChart?.resize())
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  window.removeEventListener('resize', handleResize)
  toolChart?.dispose()
  modelChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.ai-session-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}
.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.summary-row {
  margin-bottom: 16px;
}
.stat-card {
  text-align: center;
  padding: 8px 0;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.chart-card {
  margin-bottom: 16px;
}
.chart-box {
  height: 280px;
}
.chart-sm {
  height: 240px;
}
.list-card {
  margin-top: 0;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-count {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.duration-value {
  font-weight: 500;
  color: #409eff;
}
.refresh-indicator {
  margin-right: 4px;
}
</style>
