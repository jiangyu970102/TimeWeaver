<template>
  <div class="pomo-page">
    <!-- Timer Card -->
    <div class="pomo-card" :class="cardClass">
      <div class="pomo-inner">
        <!-- Status Badge -->
        <div class="pomo-status">{{ statusText }}</div>

        <!-- Timer with Ring -->
        <div class="pomo-ring-wrap">
          <svg class="pomo-ring" viewBox="0 0 200 200">
            <circle class="ring-bg" cx="100" cy="100" r="88" />
            <circle
              class="ring-fill"
              cx="100" cy="100" r="88"
              :style="{ strokeDashoffset: ringOffset }"
              :class="ringClass"
            />
          </svg>
          <div class="pomo-time">{{ displayTime }}</div>
        </div>

        <!-- Task Name -->
        <div class="pomo-task">
          <input
            v-if="!isRunning && !isBreak"
            v-model="taskName"
            placeholder="当前任务（选填）"
            class="task-input"
            maxlength="50"
            @keyup.enter="startTimer"
          />
          <span v-else class="task-label">{{ taskName || '专注中...' }}</span>
        </div>

        <!-- Session Count -->
        <div class="pomo-session">番茄 #{{ sessionCount + 1 }}</div>

        <!-- Actions -->
        <div class="pomo-actions">
          <!-- Idle state -->
          <button v-if="!isRunning && !isBreak" class="pomo-btn primary" @click="startTimer">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
            开始专注
          </button>

          <!-- Running state -->
          <template v-if="isRunning">
            <button class="pomo-btn primary pulse" @click="completeTimer">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
              完成
            </button>
            <button class="pomo-btn ghost" @click="cancelTimer">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
              放弃
            </button>
          </template>

          <!-- Break state -->
          <button v-if="isBreak" class="pomo-btn primary" @click="startBreakTimer">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
            跳过休息
          </button>
        </div>

        <!-- Settings (compact) -->
        <div class="pomo-settings">
          <label class="setting-item">
            <span>专注</span>
            <select v-model.number="pomodoroDuration">
              <option v-for="n in [15,20,25,30,35,40,45,50,55,60]" :key="n" :value="n">{{ n }}分</option>
            </select>
          </label>
          <label class="setting-item">
            <span>休息</span>
            <select v-model.number="breakDuration">
              <option v-for="n in [1,3,5,10,15,20,30]" :key="n" :value="n">{{ n }}分</option>
            </select>
          </label>
        </div>
      </div>
    </div>

    <!-- Stats Row -->
    <div class="pomo-stats">
      <div class="stat-chip">
        <span class="stat-num">{{ stats.todayCount }}</span>
        <span class="stat-lbl">今日完成</span>
      </div>
      <div class="stat-chip">
        <span class="stat-num">{{ stats.streak }}</span>
        <span class="stat-lbl">连续天数</span>
      </div>
      <div class="stat-chip">
        <span class="stat-num">{{ stats.totalCount }}</span>
        <span class="stat-lbl">累计</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { startPomodoro, completePomodoro, cancelPomodoro, getPomodoroStats } from '@/api/pomodoro'

const taskName = ref('')
const isRunning = ref(false)
const isBreak = ref(false)
const remainingSeconds = ref(25 * 60)
const totalSeconds = ref(25 * 60)
const pomodoroId = ref(null)
const sessionCount = ref(0)
const pomodoroDuration = ref(25)
const breakDuration = ref(5)
const stats = ref({ todayCount: 0, streak: 0, totalCount: 0 })
let timer = null

const displayTime = computed(() => {
  const m = String(Math.floor(remainingSeconds.value / 60)).padStart(2, '0')
  const s = String(remainingSeconds.value % 60).padStart(2, '0')
  return `${m}:${s}`
})

const progress = computed(() => {
  if (totalSeconds.value === 0) return 0
  return 1 - remainingSeconds.value / totalSeconds.value
})

const ringOffset = computed(() => {
  const circ = 2 * Math.PI * 88
  return circ * (1 - progress.value)
})

const cardClass = computed(() => ({
  'is-active': isRunning.value,
  'is-break': isBreak.value,
}))

const statusText = computed(() => {
  if (isBreak.value) return '休息中'
  if (isRunning.value) return '专注中'
  return '准备就绪'
})

const ringClass = computed(() => ({
  'ring-active': isRunning.value,
  'ring-break': isBreak.value,
}))

function startTimer() {
  isRunning.value = true
  isBreak.value = false
  totalSeconds.value = pomodoroDuration.value * 60
  remainingSeconds.value = totalSeconds.value
  startCountdown()
  callStartApi()
}

async function callStartApi() {
  try {
    const res = await startPomodoro({
      taskName: taskName.value || undefined,
      durationMin: pomodoroDuration.value,
    })
    if (res.code === 201) pomodoroId.value = res.data.id
  } catch {}
}

async function completeTimer() {
  if (!pomodoroId.value) return
  clearInterval(timer)
  isRunning.value = false
  sessionCount.value++

  try {
    const res = await completePomodoro(pomodoroId.value)
    if (res.code === 200) {
      ElMessage.success(`番茄 #${sessionCount.value} 完成！`)
      loadStats()
      startBreak()
    }
  } catch {}
}

async function cancelTimer() {
  if (!pomodoroId.value) return
  clearInterval(timer)
  isRunning.value = false

  try {
    const res = await cancelPomodoro(pomodoroId.value)
    if (res.code === 200) ElMessage.info('已中断')
  } catch {}

  resetTimer()
  loadStats()
}

function startBreak() {
  isBreak.value = true
  totalSeconds.value = breakDuration.value * 60
  remainingSeconds.value = totalSeconds.value
  startCountdown()
}

function startBreakTimer() {
  clearInterval(timer)
  isBreak.value = false
  startTimer()
}

function startCountdown() {
  clearInterval(timer)
  timer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      clearInterval(timer)
      if (isBreak.value) {
        ElMessage.success('休息结束！准备下一个番茄钟')
        isBreak.value = false
        resetTimer()
      } else if (isRunning.value) {
        completeTimer()
      }
    }
  }, 1000)
}

function resetTimer() {
  clearInterval(timer)
  isRunning.value = false
  isBreak.value = false
  pomodoroId.value = null
  totalSeconds.value = pomodoroDuration.value * 60
  remainingSeconds.value = totalSeconds.value
}

async function loadStats() {
  try {
    const res = await getPomodoroStats()
    if (res.code === 200) stats.value = res.data
  } catch {}
}

onMounted(() => {
  totalSeconds.value = pomodoroDuration.value * 60
  remainingSeconds.value = totalSeconds.value
  loadStats()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.pomo-page {
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding-top: 24px;
}

/* ---- Card ---- */
.pomo-card {
  width: 100%;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s, border-color 0.3s;
  border: 2px solid transparent;
}

.pomo-card.is-active {
  border-color: #e74c3c;
  box-shadow: 0 0 24px rgba(231, 76, 60, 0.10);
}

.pomo-card.is-break {
  border-color: #27ae60;
  box-shadow: 0 0 24px rgba(39, 174, 96, 0.10);
}

.pomo-inner {
  padding: 40px 32px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* ---- Status ---- */
.pomo-status {
  font-size: 13px;
  color: #909399;
  letter-spacing: 3px;
  text-transform: uppercase;
}

/* ---- Ring ---- */
.pomo-ring-wrap {
  position: relative;
  width: 200px;
  height: 200px;
}

.pomo-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #f0f2f5;
  stroke-width: 6;
}

.ring-fill {
  fill: none;
  stroke: #409eff;
  stroke-width: 6;
  stroke-linecap: round;
  stroke-dasharray: 553;
  transition: stroke-dashoffset 0.5s ease, stroke 0.3s;
}

.ring-active {
  stroke: #e74c3c;
}

.ring-break {
  stroke: #27ae60;
}

.pomo-time {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  font-weight: 300;
  font-variant-numeric: tabular-nums;
  color: #303133;
  letter-spacing: 4px;
}

/* ---- Task ---- */
.pomo-task {
  width: 100%;
  text-align: center;
}

.task-input {
  width: 220px;
  border: none;
  border-bottom: 1px solid #dcdfe6;
  padding: 6px 0;
  font-size: 15px;
  text-align: center;
  color: #606266;
  outline: none;
  background: transparent;
  transition: border-color 0.2s;
}

.task-input::placeholder { color: #c0c4cc; }
.task-input:focus { border-color: #409eff; }

.task-label {
  font-size: 16px;
  color: #606266;
}

/* ---- Session ---- */
.pomo-session {
  font-size: 13px;
  color: #c0c4cc;
}

/* ---- Actions ---- */
.pomo-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pomo-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  border-radius: 40px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
  padding: 10px 28px;
}

.pomo-btn.primary {
  background: #303133;
  color: #fff;
}

.pomo-btn.primary:hover {
  background: #505255;
}

.pomo-btn.primary.pulse {
  background: #e74c3c;
}

.pomo-btn.primary.pulse:hover {
  background: #c0392b;
}

.pomo-btn.ghost {
  background: transparent;
  color: #909399;
  padding: 10px 16px;
}

.pomo-btn.ghost:hover {
  color: #606266;
  background: #f5f7fa;
}

/* ---- Settings ---- */
.pomo-settings {
  display: flex;
  gap: 24px;
  padding-top: 8px;
  border-top: 1px solid #f0f2f5;
  width: 100%;
  justify-content: center;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
}

.setting-item select {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 13px;
  color: #606266;
  background: #fff;
  outline: none;
  cursor: pointer;
}

.setting-item select:focus {
  border-color: #409eff;
}

/* ---- Stats ---- */
.pomo-stats {
  display: flex;
  gap: 16px;
  width: 100%;
}

.stat-chip {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.stat-num {
  display: block;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-lbl {
  display: block;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}
</style>
