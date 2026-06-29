<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <el-col :span="10">
        <el-card>
          <template #header><span>个人信息</span></template>
          <el-form label-width="80px">
            <el-form-item label="用户名">
              <span>{{ profile.username }}</span>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header><span>偏好设置</span></template>
          <el-form label-width="140px">
            <el-form-item label="每日目标时长">
              <el-input-number v-model="preferences.dailyGoalMin" :min="60" :max="720" :step="30" /> 分钟
            </el-form-item>
            <el-form-item label="每周目标时长">
              <el-input-number v-model="preferences.weeklyGoalMin" :min="300" :max="5040" :step="60" /> 分钟
            </el-form-item>
            <el-form-item label="番茄钟时长">
              <el-input-number v-model="preferences.pomodoroDuration" :min="5" :max="60" :step="5" /> 分钟
            </el-form-item>
            <el-form-item label="休息时长">
              <el-input-number v-model="preferences.breakDuration" :min="1" :max="30" :step="1" /> 分钟
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePreferences" :loading="savingPrefs">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updatePreferences } from '@/api/user'

const profile = reactive({ username: '', nickname: '', email: '' })
const preferences = reactive({
  dailyGoalMin: 240,
  weeklyGoalMin: 1200,
  pomodoroDuration: 25,
  breakDuration: 5,
})
const saving = ref(false)
const savingPrefs = ref(false)

async function loadProfile() {
  try {
    const res = await getProfile()
    if (res.code === 200) {
      const d = res.data
      profile.username = d.username || ''
      profile.nickname = d.nickname || ''
      profile.email = d.email || ''
      if (d.dailyGoalMin) preferences.dailyGoalMin = d.dailyGoalMin
      if (d.weeklyGoalMin) preferences.weeklyGoalMin = d.weeklyGoalMin
      if (d.pomodoroDuration) preferences.pomodoroDuration = d.pomodoroDuration
      if (d.breakDuration) preferences.breakDuration = d.breakDuration
    }
  } catch {}
}

async function saveProfile() {
  saving.value = true
  try {
    const res = await updateProfile({ nickname: profile.nickname, email: profile.email })
    if (res.code === 200) ElMessage.success('已保存')
    else ElMessage.error(res.message || '保存失败')
  } finally { saving.value = false }
}

async function savePreferences() {
  savingPrefs.value = true
  try {
    const res = await updatePreferences({ ...preferences })
    if (res.code === 200) ElMessage.success('已保存')
    else ElMessage.error(res.message || '保存失败')
  } finally { savingPrefs.value = false }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
}
</style>
