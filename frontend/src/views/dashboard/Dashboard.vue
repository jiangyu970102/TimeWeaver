<template>
  <div class="dashboard">
    <!-- Slacking Alert -->
    <el-alert
      v-if="slackingAlert"
      :title="slackingAlert"
      type="warning"
      show-icon
      :closable="true"
      class="mb-20"
    />

    <el-row :gutter="20">
      <!-- Today Overview -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日概览</span>
              <el-tag :type="isOngoing ? 'danger' : 'info'" size="small">
                {{ isOngoing ? '计时中' : '空闲' }}
              </el-tag>
            </div>
          </template>
          <div class="overview-stats">
            <div class="stat-item">
              <div class="stat-label">今日记录</div>
              <div class="stat-value">{{ todayTotal }} min</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">记录条数</div>
              <div class="stat-value">{{ recordCount }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">目标进度</div>
              <div class="stat-value" style="width: 100%">
                <el-progress :percentage="goalProgress" :stroke-width="10" :color="goalColor" />
              </div>
            </div>
            <el-divider />
            <div class="ongoing-task" v-if="ongoingRecord">
              <span class="ongoing-label">当前：</span>
              <span class="ongoing-desc">{{ ongoingRecord.description || '无描述' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Quick Record -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>快速记录</span>
          </template>
          <div class="quick-record">
            <el-select v-model="selectedCategory" placeholder="选择分类" style="width: 100%; margin-bottom: 12px" filterable>
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              >
                <span :style="{ color: cat.color }">{{ cat.icon }} {{ cat.name }}</span>
              </el-option>
            </el-select>
            <el-input v-model="recordDesc" placeholder="描述（选填）" style="margin-bottom: 12px" maxlength="200" show-word-limit />
            <el-button
              :type="isOngoing ? 'danger' : 'primary'"
              :loading="recordingLoading"
              style="width: 100%"
              @click="toggleRecord"
            >
              <template v-if="isOngoing">
                ■ 停止计时（{{ elapsed }}）
              </template>
              <template v-else>
                ▶ 开始计时
              </template>
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- Today Timeline -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>今日时间轴</span>
          </template>
          <div class="timeline" v-if="todayRecords.length > 0">
            <div v-for="record in todayRecords" :key="record.id" class="timeline-item">
              <div class="timeline-time">{{ formatTime(record.startTime) }} - {{ formatTime(record.endTime) }}</div>
              <div class="timeline-content">
                <el-tag :color="getCategoryColor(record.categoryId)" size="small" effect="dark" style="color:#fff; border: none">
                  {{ getCategoryName(record.categoryId) }}
                </el-tag>
                <span class="timeline-desc">{{ record.description || '—' }}</span>
                <span class="timeline-duration">{{ formatDuration(record.durationMin) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="今天还没记录" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatTime, formatDuration } from '@/utils/format'
import { getRecords, createRecord, stopRecord, getCurrentRecord } from '@/api/records'
import { getDailyStats } from '@/api/stats'
import { getCategories } from '@/api/categories'

const categories = ref([])
const selectedCategory = ref(null)
const recordDesc = ref('')
const todayRecords = ref([])
const ongoingRecord = ref(null)
const isOngoing = computed(() => ongoingRecord.value !== null && ongoingRecord.value !== undefined)
const recordingLoading = ref(false)
const elapsed = ref('00:00')
const slackingAlert = ref('')
let timer = null

const todayTotal = computed(() =>
  todayRecords.value.reduce((sum, r) => sum + (r.durationMin || 0), 0))
const recordCount = computed(() => todayRecords.value.length)
const goalProgress = computed(() => {
  const pct = todayTotal.value >= 240 ? 100 : Math.round((todayTotal.value / 240) * 100)
  return pct
})
const goalColor = computed(() => {
  if (goalProgress.value >= 80) return '#67C23A'
  if (goalProgress.value >= 50) return '#E6A23C'
  return '#909399'
})

function getCategoryColor(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.color || '#909399'
}
function getCategoryName(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.name || '未知'
}

function startElapsedTimer(startTime) {
  const start = new Date(startTime).getTime()
  clearInterval(timer)
  timer = setInterval(() => {
    const diff = Math.floor((Date.now() - start) / 1000)
    const m = String(Math.floor(diff / 60)).padStart(2, '0')
    const s = String(diff % 60).padStart(2, '0')
    elapsed.value = `${m}:${s}`
  }, 1000)
}

async function toggleRecord() {
  if (isOngoing.value) {
    recordingLoading.value = true
    try {
      await stopRecord(ongoingRecord.value.id)
      clearInterval(timer)
      elapsed.value = '00:00'
      ongoingRecord.value = null
      ElMessage.success('已停止计时')
      await loadData()
    } catch { /* handled */ }
    finally { recordingLoading.value = false }
  } else {
    if (!selectedCategory.value) {
      ElMessage.warning('请选择分类')
      return
    }
    recordingLoading.value = true
    try {
      const res = await createRecord({
        categoryId: selectedCategory.value,
        description: recordDesc.value || undefined,
        source: 'manual',
      })
      ongoingRecord.value = res.data
      startElapsedTimer(res.data.startTime)
      ElMessage.success('开始计时')
    } catch (e) {
      if (e.message?.includes('已有进行中的记录')) {
        await checkOngoing()
      }
    }
    finally { recordingLoading.value = false }
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {}
}

async function loadData() {
  try {
    const res = await getRecords({ date: new Date().toISOString().slice(0, 10) })
    todayRecords.value = res.data || []
    checkSlacking()
  } catch {}
}

function checkSlacking() {
  slackingAlert.value = ''
  const thresholdMin = 60 // 娱乐超过 60 分钟提醒

  // Sum minutes for entertainment categories
  let entertainMin = 0
  let entertainName = ''

  for (const record of todayRecords.value) {
    const cat = categories.value.find(c => c.id === record.categoryId)
    if (cat && (cat.name.includes('娱乐') || cat.name.includes('摸鱼') || cat.name.includes('休闲'))) {
      entertainMin += record.durationMin || 0
      entertainName = cat.name
    }
  }

  if (entertainMin > thresholdMin) {
    slackingAlert.value = `⚠️ 今天在「${entertainName}」上已花 ${entertainMin} 分钟，超过建议值 ${thresholdMin} 分钟，注意效率！`
  }
}

async function checkOngoing() {
  try {
    const res = await getCurrentRecord()
    if (res.data) {
      ongoingRecord.value = res.data
      startElapsedTimer(res.data.startTime)
    }
  } catch {}
}

onMounted(() => {
  loadCategories()
  loadData()
  checkOngoing()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.overview-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label { color: #909399; font-size: 14px; }
.stat-value { font-size: 18px; font-weight: 600; color: #303133; }
.ongoing-task { font-size: 13px; color: #606266; }
.ongoing-label { font-weight: 500; }
.quick-record { padding: 4px 0; }
.timeline { max-height: 340px; overflow-y: auto; }
.timeline-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.timeline-time { font-size: 12px; color: #909399; margin-bottom: 4px; }
.timeline-content { display: flex; align-items: center; gap: 8px; }
.timeline-desc { flex: 1; font-size: 14px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.timeline-duration { font-size: 12px; color: #909399; white-space: nowrap; }
</style>
