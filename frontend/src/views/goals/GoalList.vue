<template>
  <div class="goals-page">
    <div class="page-header">
      <el-button type="primary" @click="showCreate = true">
        <el-icon><Plus /></el-icon>新建目标
      </el-button>
    </div>

    <!-- Summary Cards -->
    <el-row :gutter="16" class="mb-20">
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">进行中</div>
          <div class="summary-value" style="color:#409eff">{{ summary.activeCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">已达标</div>
          <div class="summary-value" style="color:#67c23a">{{ summary.completedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">完成率</div>
          <div class="summary-value" style="color:#e6a23c">
            {{ summary.activeCount > 0 ? Math.round(summary.completedCount / summary.activeCount * 100) : 0 }}%
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Goal Cards -->
    <el-row :gutter="16">
      <el-col v-for="goal in goals" :key="goal.id" :span="8" class="mb-20">
        <el-card shadow="hover" class="goal-card"
          :class="{ 'goal-done': goal.status === 1, 'goal-abandoned': goal.status === 2 }">
          <div class="goal-header">
            <div class="goal-title">
              <el-tag :type="goalTypeTag(goal.goalType)" size="small" effect="plain">
                {{ goalTypeLabel(goal.goalType) }}
              </el-tag>
              <span class="goal-name">{{ goal.name }}</span>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, goal)">
              <el-button text circle size="small"><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="goal.status !== 0">编辑</el-dropdown-item>
                  <el-dropdown-item command="recalculate" :disabled="goal.status !== 0">刷新进度</el-dropdown-item>
                  <el-dropdown-item command="complete" :disabled="goal.status !== 0">标记完成</el-dropdown-item>
                  <el-dropdown-item command="abandon" :disabled="goal.status !== 0">放弃目标</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="goal-body">
            <div class="goal-progress-info">
              <span class="goal-current">{{ goal.currentValue || 0 }}</span>
              <span class="goal-sep">/</span>
              <span class="goal-target">{{ goal.targetValue || '∞' }}</span>
              <span class="goal-unit">{{ goal.unit || '分钟' }}</span>
            </div>
            <el-progress
              :percentage="progressPercent(goal)"
              :status="goal.status === 1 ? 'success' : goal.status === 2 ? 'exception' : undefined"
              :stroke-width="12"
            />
            <div class="goal-dates" v-if="goal.startDate">
              {{ goal.startDate }} ~ {{ goal.endDate || '长期' }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="goals.length === 0" description="还没有目标，点击上方按钮创建一个" :image-size="120" />

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="editingGoal ? '编辑目标' : '新建目标'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="目标名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：本月编程 30 小时" maxlength="50" />
        </el-form-item>
        <el-form-item label="目标类型" prop="goalType">
          <el-radio-group v-model="form.goalType">
            <el-radio value="daily">每日</el-radio>
            <el-radio value="weekly">每周</el-radio>
            <el-radio value="monthly">每月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联分类">
          <el-select v-model="form.categoryId" clearable placeholder="不限分类" style="width:100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            >
              <span :style="{ color: cat.color }">●</span>
              {{ cat.name }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="目标值" prop="targetValue">
          <el-input-number v-model="form.targetValue" :min="1" :max="99999" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="分钟" maxlength="10" style="width:120px" />
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期（可选）"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ editingGoal ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { getGoals as fetchGoals, createGoal, updateGoal, deleteGoal as removeGoal, getGoalSummary, updateGoalStatus, recalculateGoal } from '@/api/goals'
import { getCategories } from '@/api/categories'

const goals = ref([])
const categories = ref([])
const summary = reactive({ activeCount: 0, completedCount: 0 })
const showDialog = ref(false)
const showCreate = ref(false)
const editingGoal = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  goalType: 'monthly',
  categoryId: null,
  targetValue: 1200,
  unit: '分钟',
})

const dateRange = ref([])

const rules = {
  name: [{ required: true, message: '请输入目标名称', trigger: 'blur' }],
  goalType: [{ required: true, message: '请选择目标类型', trigger: 'change' }],
  targetValue: [{ required: true, message: '请输入目标值', trigger: 'blur' }],
}

function goalTypeLabel(type) {
  return { daily: '每日', weekly: '每周', monthly: '每月' }[type] || type
}

function goalTypeTag(type) {
  return { daily: 'success', weekly: 'warning', monthly: 'primary' }[type] || 'info'
}

function progressPercent(goal) {
  if (!goal.targetValue || goal.targetValue === 0) return 0
  return Math.min(100, Math.round((goal.currentValue || 0) / goal.targetValue * 100))
}

async function loadGoals() {
  try {
    const res = await fetchGoals()
    if (res.code === 200) goals.value = res.data
  } catch {}
}

async function loadSummary() {
  try {
    const res = await getGoalSummary()
    if (res.code === 200) Object.assign(summary, res.data)
  } catch {}
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data
  } catch {}
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const body = {
      name: form.name,
      goalType: form.goalType,
      categoryId: form.categoryId || null,
      targetValue: form.targetValue,
      unit: form.unit || '分钟',
      startDate: dateRange.value ? dateRange.value[0] : null,
      endDate: dateRange.value ? dateRange.value[1] : null,
    }

    let res
    if (editingGoal.value) {
      res = await updateGoal(editingGoal.value.id, body)
    } else {
      res = await createGoal(body)
    }

    ElMessage.success(editingGoal.value ? '已更新' : '已创建')
    showDialog.value = false
    showCreate.value = false
    await loadGoals()
    await loadSummary()
  } finally {
    submitting.value = false
  }
}

function handleCommand(cmd, goal) {
  if (cmd === 'edit') openEdit(goal)
  else if (cmd === 'delete') confirmDelete(goal)
  else if (cmd === 'complete') confirmStatus(goal, 1, '标记完成')
  else if (cmd === 'abandon') confirmStatus(goal, 2, '放弃目标')
  else if (cmd === 'recalculate') recalculate(goal)
}

function openCreate() {
  editingGoal.value = null
  form.name = ''
  form.goalType = 'monthly'
  form.categoryId = null
  form.targetValue = 1200
  form.unit = '分钟'
  dateRange.value = []
  showDialog.value = true
}

function openEdit(goal) {
  editingGoal.value = goal
  form.name = goal.name
  form.goalType = goal.goalType
  form.categoryId = goal.categoryId
  form.targetValue = goal.targetValue
  form.unit = goal.unit || '分钟'
  dateRange.value = goal.startDate ? [goal.startDate, goal.endDate || ''] : []
  showDialog.value = true
}

async function confirmDelete(goal) {
  try {
    await ElMessageBox.confirm(`确定删除目标「${goal.name}」？`, '提示', { type: 'warning' })
    await removeGoal(goal.id)
    ElMessage.success('已删除')
    await loadGoals()
    await loadSummary()
  } catch {}
}

async function confirmStatus(goal, status, label) {
  try {
    await ElMessageBox.confirm(`确定${label}「${goal.name}」？`, '提示', { type: 'warning' })
    await updateGoalStatus(goal.id, { status })
    ElMessage.success(`已${label}`)
    await loadGoals()
    await loadSummary()
  } catch {}
}

async function recalculate(goal) {
  try {
    await recalculateGoal(goal.id)
    ElMessage.success('进度已刷新')
    await loadGoals()
  } catch {}
}

onMounted(async () => {
  await Promise.all([loadGoals(), loadSummary(), loadCategories()])
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }

.page-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
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
  font-size: 32px;
  font-weight: 600;
}

.goal-card {
  transition: all 0.3s;
}

.goal-done {
  opacity: 0.7;
}

.goal-abandoned {
  opacity: 0.5;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.goal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.goal-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goal-body {
  padding: 0 4px;
}

.goal-progress-info {
  text-align: center;
  margin-bottom: 12px;
}

.goal-current {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}

.goal-sep {
  font-size: 16px;
  color: #dcdfe6;
  margin: 0 4px;
}

.goal-target {
  font-size: 20px;
  font-weight: 500;
  color: #606266;
}

.goal-unit {
  font-size: 13px;
  color: #909399;
  margin-left: 4px;
}

.goal-dates {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
  text-align: center;
}
</style>
