<template>
  <div class="calendar-view">
    <!-- Navigation -->
    <el-card shadow="hover" class="mb-20">
      <div class="calendar-nav">
        <el-button @click="shiftMonth(-1)" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h3 class="calendar-title">{{ year }} 年 {{ month }} 月</h3>
        <el-button @click="shiftMonth(1)" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button @click="resetToToday" size="small" class="ml-4">今天</el-button>
      </div>
    </el-card>

    <!-- Calendar Grid -->
    <el-card shadow="hover" class="mb-20">
      <div class="calendar-grid">
        <div class="calendar-weekdays">
          <div v-for="d in weekdays" :key="d" class="weekday-cell">{{ d }}</div>
        </div>
        <div class="calendar-days">
          <div
            v-for="(day, idx) in calendarDays"
            :key="idx"
            :class="[
              'calendar-day',
              { 'is-empty': !day, 'is-today': day === todayDate, 'is-selected': day === selectedDay },
            ]"
            @click="day && selectDay(day)"
          >
            <template v-if="day">
              <div class="day-num">{{ day }}</div>
              <div v-if="getDayData(day)" class="day-bar" :style="{ height: getDayBarHeight(day) + 'px', background: getDayColor(day) }" />
              <div v-if="getDayData(day)" class="day-min">{{ getDayData(day).totalMinutes }}min</div>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Selected Day Records -->
    <el-card shadow="hover" v-if="selectedDay">
      <template #header>
        <div class="card-header">
          <span>{{ year }} 年 {{ month }} 月 {{ selectedDay }} 日 记录</span>
          <span class="day-total" v-if="selectedDayData">
            总计：{{ selectedDayData.totalMinutes }} min / {{ selectedDayData.records.length }} 条
          </span>
        </div>
      </template>
      <el-table :data="selectedDayData?.records || []" v-loading="detailLoading" empty-text="暂无记录" size="small" stripe>
        <el-table-column label="时间" width="120">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="140">
          <template #default="{ row }">
            <el-tag :color="getColor(row.categoryId)" size="small" effect="dark" style="color: #fff; border: none">
              {{ getName(row.categoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="时长" width="80">
          <template #default="{ row }">
            {{ row.durationMin || 0 }}min
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getRecords } from '@/api/records'
import { getCategories } from '@/api/categories'
import { getTrendData } from '@/api/stats'
import { formatTime } from '@/utils/format'

const categories = ref([])
const records = ref([])
const monthTrend = ref([]) // [{date, minutes}]
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)
const selectedDay = ref(null)
const detailLoading = ref(false)

const weekdays = ['一', '二', '三', '四', '五', '六', '日']
const todayDate = new Date().getDate()

const calendarDays = computed(() => {
  const firstDay = new Date(year.value, month.value - 1, 1)
  let startWeekday = firstDay.getDay()
  startWeekday = startWeekday === 0 ? 6 : startWeekday - 1 // 周一=0

  const daysInMonth = new Date(year.value, month.value, 0).getDate()
  const days = []
  for (let i = 0; i < startWeekday; i++) days.push(null)
  for (let d = 1; d <= daysInMonth; d++) days.push(d)
  return days
})

const trendMap = computed(() => {
  const map = {}
  for (const item of monthTrend.value) {
    const d = parseInt(item.date.split('-')[2])
    map[d] = item.minutes
  }
  return map
})

const selectedDayData = ref(null)

function getDayData(day) {
  const minutes = trendMap.value[day]
  return minutes ? { totalMinutes: minutes } : null
}

function getDayBarHeight(day) {
  const data = getDayData(day)
  if (!data) return 0
  return Math.min(Math.max(data.totalMinutes / 10, 4), 40)
}

function getDayColor(day) {
  const data = getDayData(day)
  if (!data) return 'transparent'
  const m = data.totalMinutes
  if (m >= 480) return '#1a7d36'
  if (m >= 240) return '#2da44e'
  if (m >= 60) return '#4caf50'
  return '#81c784'
}

function getColor(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.color || '#909399'
}

function getName(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.name || '未知'
}

function shiftMonth(delta) {
  month.value += delta
  if (month.value < 1) { month.value = 12; year.value-- }
  if (month.value > 12) { month.value = 1; year.value++ }
  selectedDay.value = null
  selectedDayData.value = null
  loadTrend()
}

function resetToToday() {
  const now = new Date()
  year.value = now.getFullYear()
  month.value = now.getMonth() + 1
  selectedDay.value = now.getDate()
  loadTrend()
  loadDayDetail()
}

function selectDay(day) {
  selectedDay.value = day
  loadDayDetail()
}

async function loadTrend() {
  const daysInMonth = new Date(year.value, month.value, 0).getDate()
  const start = `${year.value}-${String(month.value).padStart(2, '0')}-01`
  const end = `${year.value}-${String(month.value).padStart(2, '0')}-${String(daysInMonth).padStart(2, '0')}`
  try {
    const res = await getTrendData({ start, end })
    if (res.code === 200) monthTrend.value = res.data || []
  } catch { monthTrend.value = [] }
}

async function loadDayDetail() {
  if (!selectedDay.value) return
  const dateStr = `${year.value}-${String(month.value).padStart(2, '0')}-${String(selectedDay.value).padStart(2, '0')}`
  detailLoading.value = true
  try {
    const res = await getRecords({ date: dateStr })
    const dayRecords = res.code === 200 ? (res.data || []) : []
    const total = dayRecords.reduce((s, r) => s + (r.durationMin || 0), 0)
    selectedDayData.value = { totalMinutes: total, records: dayRecords }
  } catch { selectedDayData.value = null }
  finally { detailLoading.value = false }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {}
}

onMounted(() => {
  loadCategories()
  loadTrend()
})
</script>

<style scoped>
.calendar-view { max-width: 1000px; }
.mb-20 { margin-bottom: 20px; }
.ml-4 { margin-left: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.day-total { font-size: 13px; color: #909399; }

.calendar-nav { display: flex; align-items: center; justify-content: center; gap: 16px; }
.calendar-title { margin: 0; font-size: 18px; }

.calendar-grid { user-select: none; }
.calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; margin-bottom: 4px; }
.weekday-cell { font-size: 12px; color: #909399; padding: 4px 0; font-weight: 600; }

.calendar-days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; }
.calendar-day {
  aspect-ratio: 1;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border-radius: 8px; cursor: pointer; transition: all 0.15s; padding: 4px; min-height: 70px;
  border: 2px solid transparent; position: relative;
}
.calendar-day:hover { background: #f0f5ff; }
.calendar-day.is-empty { cursor: default; }
.calendar-day.is-empty:hover { background: transparent; }
.calendar-day.is-today { border-color: #409eff; }
.calendar-day.is-selected { background: #e6f4ff; border-color: #409eff; }
.day-num { font-size: 14px; font-weight: 500; color: #303133; position: absolute; top: 4px; left: 8px; }
.day-bar { width: 6px; border-radius: 3px; margin-top: 8px; min-height: 4px; }
.day-min { font-size: 10px; color: #909399; margin-top: 2px; white-space: nowrap; }
</style>
